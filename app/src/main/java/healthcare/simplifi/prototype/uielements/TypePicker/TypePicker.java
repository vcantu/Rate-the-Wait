package healthcare.simplifi.prototype.uielements.TypePicker;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;

import healthcare.simplifi.prototype.Draw;
import healthcare.simplifi.prototype.MapActivity;
import healthcare.simplifi.prototype.R;

/**
 * Created by Viviano on 6/12/2015.
 */
public class TypePicker extends ViewGroup {

    private Paint p = new Paint();
    private float radius;
    private SVG search, clear, current;
    private float icSize = 0;
    private final float IC_DES_SIZE;

    private Context context;

    public boolean active = false;
    private float pressedFrac = -1;

    public TypePicker(final Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        radius = getResources().getDimension(R.dimen.button_rad);
        //icons here
        IC_DES_SIZE = getResources().getDimension(R.dimen.button_ic_height);
        search = new SVGBuilder().readFromResource(context.getResources(), R.raw.ic_search).build();
        clear = new SVGBuilder().readFromResource(context.getResources(), R.raw.ic_clear).build();
        current = search;
        icSize = IC_DES_SIZE;

        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        Type.loadTypes(context);

        setOnTouchListener(new OnTouchListener() {
            private float diffX, diffY;
            private boolean moving = false;

            private boolean isClick = false;
            private CountDownTimer clickTimer;

            private void startClickTimer() {
                isClick = true;
                clickTimer = new CountDownTimer(200, 200) {
                    @Override
                    public void onTick(long millisUntilFinished) {}
                    @Override
                    public void onFinish() {
                        isClick = false;
                    }
                }.start();
            }
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startClickTimer();
                        if (dist(e.getX(), e.getY(), getWidth()/2, getHeight()/2) < radius
                                && !active) {
                            moving = true;
                            diffX = e.getX();
                            diffY = e.getY();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (moving) {
                            setX(getX() + e.getX() - diffX);
                            setY(getY() + e.getY() - diffY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if ( isClick && dist(e.getX(), e.getY(), getWidth()/2, getHeight()/2) <= radius) {
                            if (getChildCount() == 0)
                                activate();
                            else
                                deactivate();
                            animatePress();
                        }
                        moving = false;
                        break;
                }
                float dist = dist(getWidth()/2, getHeight()/2, e.getX(), e.getY());
                if (!active)
                    return dist <= radius;
                return dist <= getWidth()/2;
            }
        });
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount()>0) {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
            View child = getChildAt(getChildCount() - 1);
            setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
            //fix coordinates after resize
            if (getChildCount() != 1) {
                float add = getLeft() + (radius * ((getChildCount()) * 2.4f) + radius);
                setX(add);//fixes X
            }
        }
        else
            setMeasuredDimension((int) radius * 2, (int) radius * 2);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount()<=0) {
            if (active)
                animDeactivate();
            return;
        }
        int centerX = getMeasuredWidth()/2;
        int centerY = getMeasuredHeight()/2;

        int rad = ((TypeScrollView)getChildAt(getChildCount()-1)).largeRadius;

        for (int i=0; i<getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(centerX - rad, centerY - rad, centerX + rad, centerY + rad);
            ((TypeScrollView)child).centerX = centerX;
            ((TypeScrollView)child).centerY = centerY;
        }
        if (getChildCount() == 1 && !active)
            animActivate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Draw.flatButton(getWidth() / 2, getHeight() / 2, radius, null,
                0xFFFF0000, 0xFFFFFFFF, pressedFrac, p, canvas);
        Draw.svg(current, getWidth() / 2, getHeight() / 2, icSize, icSize, canvas);
    }

    private void activate() {
        addScrollView(Type.TYPE_TREE, 1);
        animIconSwitch(true);
    }

    public void deactivate() {
        int count = getChildCount();
        for (int i=0; i<count; i++) {
            ((TypeScrollView)getChildAt(i)).close();
        }
        animIconSwitch(false);
    }

    private void animActivate() {
        final float startX =  getX() + (radius * 2.4f); // this is where X is after the resize
        final float endX = getLeft() + (radius * ((getChildCount()) * 2.4f) + radius);
        final float startY = getY();
        final float endY = ((View) getParent()).getHeight() / 2 - getHeight() / 2;

        final ValueAnimator animPos = ValueAnimator.ofFloat(0, 100)
        .setDuration(500);
        animPos.setInterpolator(new OvershootInterpolator(4));
        animPos.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float frac = animPos.getAnimatedFraction();
                setX(startX + frac * (endX - startX));
                setY(startY + frac * (endY - startY));
            }
        });
        animPos.start();
        active = true;
    }
    private void animDeactivate() {
        setX(getLeft() + radius);
        final float startX =  getX(); // this is where X is after the resize
        final float endX = ((View)getParent()).getWidth()
                - getResources().getDimension(R.dimen.button_margin_right) / 2
                - (radius * 2);

        final ValueAnimator animPos = ValueAnimator.ofFloat(0, 100)
                .setDuration(500);
        animPos.setInterpolator(new OvershootInterpolator());
        animPos.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float frac = animPos.getAnimatedFraction();
                float newX = startX + frac * (endX - startX);
                setX(newX);
            }
        });
        animPos.start();
        active = false;
    }

    private void addScrollView(final Type.Tree tree, final int level) {
        final TypeScrollView scrollView = new TypeScrollView(context, tree.leaves, level);
        scrollView.setOnTypeSelectedListener(new TypeScrollView.TypeScrollEventListener() {
            @Override
            public void onTypeSelected(Type.Tree type) {
                if (type.size() > 0) {
                    if (getChildCount() <= level)
                        addScrollView(type, level + 1);
                    else {
                        TypeScrollView curr = (TypeScrollView) getChildAt(level);
                        if (!curr.equals(type.leaves))
                            curr.switchTypes(type.leaves);
                        if (level == 1 && getChildCount() == 3)
                            ((TypeScrollView) getChildAt(2)).close();
                    }
                } else {
                    ((MapActivity) context).loadPlacesFromAddon(type.toString());
                    deactivate();
                }
            }

            @Override
            public void onCloseAnimationDone() {
                removeView(scrollView);
            }
        });
        addView(scrollView);
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
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                pressedFrac = -1;
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        anim.start();
    }

    private void animIconSwitch(final boolean activating) {
        ValueAnimator shrink = scaleAnimator(true);
        shrink.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if (activating)
                    current = clear;
                else
                    current = search;
                scaleAnimator(false).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        shrink.start();
    }
    private ValueAnimator scaleAnimator(boolean shrink) {
        final ValueAnimator anim = ValueAnimator.ofFloat(
                shrink ? IC_DES_SIZE : 0,
                shrink ? 0 : IC_DES_SIZE)
                .setDuration(300);
        anim.setInterpolator(shrink ? new AnticipateInterpolator() :
                            new OvershootInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                icSize = (float)anim.getAnimatedValue();
                invalidate();
            }
        });
        return anim;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}
