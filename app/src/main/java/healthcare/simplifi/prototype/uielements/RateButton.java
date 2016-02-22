package healthcare.simplifi.prototype.uielements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import healthcare.simplifi.prototype.Draw;
import healthcare.simplifi.prototype.R;

/**
 * Created by Viviano on 5/13/2015.
 */
public class RateButton extends View {

    Paint p = new Paint();
    Bitmap bmRate;

    private float radius, height;

    public boolean pressed = false;

    public RateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        bmRate = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_rate);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);

        radius = context.getResources().getDimension(R.dimen.button_rad);
        height = context.getResources().getDimension(R.dimen.button_height);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        press(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        press(event);
                        break;
                    case MotionEvent.ACTION_UP:
                        pressed = false;
                        invalidate();
                        break;

                }
                return false;
            }
        });
    }
    private void press(MotionEvent event) {
        if (dist(event.getX(), event.getY(), getWidth() / 2, getHeight() / 2)
                < radius) {
            pressed = true;
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Draw.floatingButton(getWidth() / 2, getHeight() / 2, radius, bmRate, 0xFFB20000, 0xFFFFFFFF, height, pressed, p, canvas);
    }

    private float dist(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
}
