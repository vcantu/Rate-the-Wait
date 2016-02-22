package healthcare.simplifi.prototype.uielements.rating;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import healthcare.simplifi.prototype.Draw;

/**
 * Created by Viviano on 6/17/2015.
 */
public class QuickTapQuestionnaire extends View {

    /*
        FLAG_BACK is the direction of the animation
        FLAG_RESET means the animation will reset to previous state
        FLAG_COMPLETE means it is currently scrolling and should complete scroll after fling
     */
    private int FLAG_BACK = 1, FLAG_RESET = 2, FLAG_COMPLETE = 4;
    private float VELOCITY_THRESHOLD = 200;

    private String[] questions;
    private int[] colors;

    private Paint p = new Paint();

    private int paddSides, paddTop, paddBottom;
    private int textHeight, btnHeight, mapHeight;

    private int index = 0;
    private float animX = 0, animMap = 0;
    private boolean animating = false;

    private onSelectListener selectListener;

    public QuickTapQuestionnaire(Context context, AttributeSet attrs) {
        super(context, attrs);

        questions = new String[] { "How is the staff?", "How tasty is the food?",
                "How clean is the place?", "How loud is the place?", "How helpful is the staff?",
                "How is the food presented?", "How fancy is the place?" };
        colors = new int[] { 0xff40E0D0, 0xff7C1484, 0xffFF3300, 0xff33CC33,  0xff40E0D0, 0xff7C1484, 0xffFF3300, };

        final GestureDetector gd = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                getParent().requestDisallowInterceptTouchEvent(true);

                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {}

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                forward();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                setAnimX(animX - distanceX);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {}

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) >= VELOCITY_THRESHOLD) {
                    if (velocityX < 0)
                        skip();
                    else
                        back();
                }
                else
                    reset();
                return true;
            }
        });
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gd.onTouchEvent(event);
            }
        });
    }

    public void setQuestions(String[] questions) {
        this.questions = questions;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        paddSides = Math.max(getPaddingLeft(), getPaddingRight());
        paddBottom = getPaddingBottom();
        paddTop = getPaddingTop();
        btnHeight = (MeasureSpec.getSize(widthMeasureSpec) - paddSides * 2) / 5;
        textHeight = btnHeight/2;
        mapHeight = btnHeight / 4 - paddBottom;
        setMeasuredDimension(widthMeasureSpec, (int) (btnHeight * 2.5f));
    }

    @Override
    public void onDraw(Canvas canvas) {
        p.setFlags(Paint.ANTI_ALIAS_FLAG);

        for (int i=0; i<questions.length; i++) {
            drawQuestion(i, animX + getWidth() * i, canvas);
        }
        drawMap(canvas);
    }

    private void drawQuestion(int index, float x, Canvas canvas) {
        p.setColor(colors[index]);
        canvas.drawRect(x, 0, x + getWidth(), getHeight(), p);

        //set text size in bounds
        p.setColor(0xFFffffff);
        String text = questions[index];
        p.setTextSize(textHeight);
        Rect bounds = new Rect();
        p.getTextBounds(text, 0, text.length(), bounds);
        float textSize = (textHeight) * (getWidth() - paddSides * 8) / bounds.width();
        p.setTextSize(Math.min(textSize, textHeight));
        Draw.textOnX(text, getWidth() / 2 + x, paddTop + textHeight, p, canvas);

        p.setTextSize(btnHeight);
        //options
        for (int i=1; i<=5; i++) {
            Draw.text(i + "", (btnHeight*(i-1) + paddSides + btnHeight/2)+x, btnHeight * 1.5f, p, canvas);
        }
    }

    private void drawMap(Canvas canvas) {
        p.setColor(0x50ffffff);
        int count = questions.length;
        float length = mapHeight * count;
        for (int i=0; i<count; i++) {
            canvas.drawCircle(getWidth() / 2 - length / 2 + (i * mapHeight + mapHeight / 2),
                    getHeight() - paddBottom - mapHeight, mapHeight / 4, p);
        }
        p.setColor(0xFFffffff);
        canvas.drawCircle((getWidth() / 2 - length / 2 + (mapHeight / 2)) + animMap,
                getHeight() - paddBottom - mapHeight, mapHeight / 4, p);
    }

    private void beginAnimation(final int i, boolean complete) {
        animating = true;
        final ValueAnimator anim = ValueAnimator.ofFloat(animX, -i*getWidth())
                .setDuration(400);
        anim.setInterpolator(!complete ? new AnticipateOvershootInterpolator(.5f) :
                                         new OvershootInterpolator(.5f));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setAnimX((float) anim.getAnimatedValue());
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                index = i;
                animating = false;
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

    private void skip() {
        if (index < questions.length-1)
            beginAnimation(index+1, true);
        else
            reset();
    }

    private void back() {
        if (index > 0)
            beginAnimation(index-1, true);
        else
            reset();
    }

    private void forward() {
        if (index < questions.length-1)
            beginAnimation(index+1, false);
    }

    private void reset() {
        beginAnimation(index, false);
    }

    private void setAnimX(float animx) {
        animX = animx;
        animMap = mapHeight * (animx / getWidth());
        animMap *= -1; //map goes opposite of animX
        invalidate();
    }


    public interface onSelectListener {
        void onSelect(int selected, int index);
    }
}
