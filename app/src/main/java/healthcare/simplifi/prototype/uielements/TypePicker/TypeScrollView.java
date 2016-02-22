package healthcare.simplifi.prototype.uielements.TypePicker;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.larvalabs.svgandroid.SVG;

import java.util.LinkedList;

import healthcare.simplifi.prototype.Draw;
import healthcare.simplifi.prototype.R;

/**
 * Created by Viviano on 6/12/2015.
 */
public class TypeScrollView extends View implements GestureDetector.OnGestureListener,
View.OnTouchListener {

    private static int[] SIZES = new int[] { 0, 7, 14, 21 };
    public static int MAX_LEVEL = 3;

    private LinkedList<Type.Tree> types;
    //drawing
    private Paint p = new Paint();
    public float centerX, centerY;
    private float radius;
    private int level, drawSize;
    public int largeRadius;
    //scrolling
    private double currAngle= Math.PI, markAngle= Math.PI, diffAngle;
    private float animFrac = -1;
    private boolean animClockwise = true;
    private boolean moving = false;
    private int index;
    //clicking
    private int pressedIndex = -1;
    private float pressedFrac = 0;

    GestureDetector gestureDetector;
    //listeners
    private TypeScrollEventListener typeScrollEventListener;

    public TypeScrollView(Context context, LinkedList<Type.Tree> typeList, final int lvl) {
        super(context);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        radius = getResources().getDimension(R.dimen.button_rad);
        level = lvl;
        drawSize = SIZES[level];
        types = typeList;

        startAnimation(true);

        gestureDetector = new GestureDetector(context, this);

        setOnTouchListener(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LinkedList &&
                ((LinkedList<Type.Tree>)o).get(0).toString().equals(types.get(0).toString());
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        largeRadius = (int)(radius * (level * 2.4f) + radius);
        setMeasuredDimension(largeRadius * 2, largeRadius * 2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int i=0; i<drawSize; i++) {
            //only draws icons showing for higher efficiency
            int drawIndex = i;
            //puts first half in front and last half in back
            if (i >= drawSize / 2)
                drawIndex = types.size() - drawSize / 2
                        + (drawIndex - drawSize / 2) - (level % 2 == 0 ? 0 : 1);

            //fix subIndex
            drawIndex = drawIndex + index;
            drawIndex = drawIndex % types.size();

            if (drawIndex <0)
                drawIndex = types.size() + drawIndex;

            double angle = (2 * Math.PI) / drawSize;
            angle *= i;
            angle += currAngle;
            //if its animating use new angle
            if (animFrac != -1) {
                double start = animClockwise ? Math.PI * 2 : 0;//TODO: start angle is 0 it may be Math.PI
                angle = normalize(angle);
                angle = start + animFrac * (angle - start);
                angle = normalize(angle);
            }

            float dist = largeRadius - radius;

            Type.Tree tree = types.get(drawIndex);
            String name = tree.data.name;
            Bitmap ic = tree.data.icon;
            SVG svg = tree.data.svgIcon;
            float x = (float) Math.cos(angle) * dist + centerX,
                    y = (float) Math.sin(angle) * -dist + centerY;
            if (svg == null)
                Draw.flatButton(x, y, radius, ic == null ? name : ic, tree.data.color, 0xFFffffff,
                        drawIndex==pressedIndex ? pressedFrac : -1, p, canvas);
            else {
                Draw.svg(svg, x, y, radius * 2, radius * 2, canvas);
            }
        }
    }

    private Type.Tree getType(MotionEvent e) {
        if (moving) {
            double fingAngle = Math.atan2(centerY - e.getY(), e.getX() - centerX);
            fingAngle += Math.PI;
            fingAngle -= (currAngle - markAngle);
            fingAngle += Math.PI / drawSize;//used to make the angle the edge instead of the middle
            if (fingAngle<0)
                fingAngle += 2 * Math.PI;
            int i = (int)(fingAngle / (2*Math.PI/drawSize));//floors the result
            i %= drawSize;

            int drawIndex = i;
            //puts first half in front and last half in back
            if (i >= drawSize / 2)
                drawIndex = types.size() - drawSize / 2
                        + (drawIndex - drawSize / 2) - (level % 2 == 0 ? 0 : 1);

            //fix subIndex
            drawIndex = drawIndex + index;
            drawIndex = drawIndex % types.size();

            if (drawIndex <0)
                drawIndex = types.size() + drawIndex;

            pressedIndex = drawIndex;
            animatePress();
            return types.get(drawIndex);
        }
        return null;
    }

    private void animatePress() {
        final ValueAnimator anim = ValueAnimator.ofFloat(0, radius)
                .setDuration(400);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                pressedFrac = anim.getAnimatedFraction();
                invalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                pressedIndex = -1;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();
    }

    private float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private double normalize(double angle) {
        if (angle < 0)
            return 2 * Math.PI + angle;
        else if (angle > 2 * Math.PI)
            return angle - 2 * Math.PI;
        return angle;
    }

    private void startAnimation(boolean clockwise) {
        ValueAnimator anim = getAnimation(clockwise, false);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                animFrac = -1;
                invalidate();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();
    }

    public void close() {
        ValueAnimator anim = getAnimation(animClockwise, true);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                invalidate();
                typeScrollEventListener.onCloseAnimationDone();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();
    }

    public void switchTypes(final LinkedList<Type.Tree> newTypes) {
        ValueAnimator anim = getAnimation(animClockwise, true);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animFrac = -1;
                invalidate();
                currAngle = Math.PI;
                markAngle = Math.PI;
                index = 0;
                types = newTypes;
                animClockwise = !animClockwise;

                startAnimation(animClockwise);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.start();
    }

    public ValueAnimator getAnimation(boolean clockwise, final boolean closing) {
        animClockwise = !closing ? clockwise : !animClockwise;
        animFrac = -1;
        //animate types
        final ValueAnimator anim = ValueAnimator.ofFloat(0, 100)
                .setDuration(level >= 3 ? 600 : 400);
        anim.setInterpolator(closing ? new AnticipateInterpolator() : new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!closing)
                    animFrac = anim.getAnimatedFraction();
                else
                    animFrac = 1 - anim.getAnimatedFraction();
                invalidate();
            }
        });
        return anim;
    }

    private boolean animating = false;
    public void fling(float vx, float vy) {
        animating = true;
        float vc = (float) Math.sqrt(vx * vx + vy * vy);
        if (vy > 0)
            vc *= -1;

        float distanceAngle = vc / (largeRadius-radius);

        distanceAngle *= .2f;

        diffAngle = 0;
        final ValueAnimator anim = ValueAnimator.ofFloat((float)currAngle, (float)currAngle+distanceAngle)
                .setDuration(800);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animating) {
                    double fingAngle = (double)(float)anim.getAnimatedValue();
                    //updates curr Angle
                    currAngle = normalize(diffAngle+fingAngle);
                    //fixes index while moving
                    double diff = currAngle - markAngle;
                    double expected = (2 * Math.PI) / drawSize;

                    //fix index if needed
                    if (diff <= -expected || diff >= expected) {
                        if (diff <= -expected)//Subtract 1
                            index += 1;
                        else if (diff >= expected) //Add 1
                            index -= 1;

                        //fix underflow
                        if (index < 0)
                            index = types.size() + index;
                        //fix overflow
                        index %= types.size();

                        markAngle = Math.PI;
                        currAngle = Math.PI;
                        diffAngle = currAngle - fingAngle;
                    }
                    invalidate();
                }
                else
                    anim.cancel();
            }
        });
        anim.start();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        float dist = dist(e.getX(), e.getY(), centerX, centerY);
        if (dist < largeRadius && dist > largeRadius - radius * 2) {
            moving = true;
            diffAngle = currAngle - Math.atan2(centerY - e.getY(), e.getX() - centerX);
            if (animating) {
                animating = false;
            }
        }
        return moving;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (moving && typeScrollEventListener != null)
            typeScrollEventListener.onTypeSelected(getType(e));
        onFingUp();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        onFingUp();
        if (Math.abs(velocityX) > 400 || Math.abs(velocityY) > 400 ) {
            fling(-velocityX, -velocityY);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                double fingAngle = Math.atan2(centerY - e.getY(), e.getX() - centerX);
                if (moving && !animating) {
                    //updates curr Angle
                    currAngle = normalize(fingAngle + diffAngle);
                    //fixes index while moving
                    double diff = currAngle - markAngle;
                    double expected = (2 * Math.PI) / drawSize;

                    //fix index if needed
                    if (diff <= -expected || diff >= expected) {
                        if (diff <= -expected)//Subtract 1
                            index += 1;
                        else if (diff >= expected) //Add 1
                            index -= 1;

                        //fix underflow
                        if (index < 0)
                            index = types.size() + index;
                        //fix overflow
                        index %= types.size();

                        markAngle = Math.PI;
                        currAngle = Math.PI;
                        diffAngle = currAngle - fingAngle;
                    }
                }
                invalidate();
                break;
        }
        boolean gd = gestureDetector.onTouchEvent(e);
        if (!gd && e.getAction() == MotionEvent.ACTION_UP) {
            onFingUp();
            return true;
        }
        return gd;
    }

    //Listeners
    public void setOnTypeSelectedListener(TypeScrollEventListener eventListener) {
        typeScrollEventListener = eventListener;
    }

    public interface TypeScrollEventListener {
        void onTypeSelected(Type.Tree type);
        void onCloseAnimationDone();
    }

    private void onFingUp() {
        moving = false;
    }
}
