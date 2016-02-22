package healthcare.simplifi.prototype.uielements.rating;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import healthcare.simplifi.prototype.Draw;
import healthcare.simplifi.prototype.R;

/**
 * Created by Viviano on 5/28/2015.
 */
public class LineMeasure extends View {
    private Paint p = new Paint();
    private int progress = 1;
    private int index = 0;//originally 0


    private float paddTop, blockW, blockH, offX, offY;
    public boolean following = false;

    public LineMeasure(final Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        int x = index % 20, y = index / 20;
                        if (y % 2 != 0) {
                            x = 19 - x;
                        }
                        float lx = (x - 1) * blockW;
                        if (x == 0)// far left
                            lx += blockW;
                        else if (x == 19)// far right
                            lx -= blockW;

                        if (dist(lx + blockW * 1.5f, y * blockH + blockW, event.getX(), event.getY())
                                <= context.getResources().getDimension(R.dimen.line_measure_follow_rad)) {
                            following = true;
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!following)
                            break;
                        //Set X and Y grid coordinates
                        int X = (int) (event.getX() / blockW);
                        int Y = (int) ((event.getY() - blockW) / blockH);

                        //fix X left
                        if (X < 0)
                            X=0;
                        //fix X right
                        if (X > 19)
                            X=19;
                        //fix Y top
                        if (Y<0)
                            Y=0;
                        //fix Y bottom
                        if (Y>progress/20-1) {
                            Y = progress / 20 - 1;
                            if (Y % 2 != 0) {
                                if (X <= progress % 20 - 1)
                                    Y++;
                            }else {
                                if (X >= 20 - progress % 20)
                                    Y++;
                            }
                        }
                        //fix X if it's in ODD position
                        if (Y % 2 != 0 && Y > 0)
                            X = 19 - X;

                        int temp = 20 * Y + X;
                        if (temp < progress)
                            index = temp;
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        following = false;
                        clearFocus();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        paddTop = getHeight() * .2f;
        blockW = getWidth() / 20;
        blockH = (getHeight() - paddTop) / 5;
        offX = blockW * .1f;
        offY = blockH * .1f;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i=0; i<progress; i++) {
            int x = i % 20, y = i / 20;
            if (y % 2 != 0) {
                x = 19 - x;
            }
            if (i==index) {
                p.setColor(0xFF0FDDAF);//youColor
                float lx = (x-1)*blockW;
                if (x == 0)// far left
                    lx += blockW;
                else if (x == 19)// far right
                    lx -= blockW;
                canvas.drawRect(lx, y * blockH, lx + blockW * 3, y * blockH + blockW * 2, p);
                //draw triangle
                Path path = new Path();
                path.setFillType(Path.FillType.EVEN_ODD);
                path.moveTo(lx, y * blockH + blockW * 2);
                path.lineTo(lx + blockW * 3, y * blockH + blockW * 2);
                path.lineTo(x*blockW+offX+ blockW/2, y*blockH+offY + paddTop);
                path.close();
                canvas.drawPath(path, p);

                p.setColor(0xFFFFFFFF);
                p.setTextSize(40);
                Draw.text("you", lx + blockW * 1.5f, y * blockH + blockW, p, canvas);
                p.setColor(0xFF0FDDAF);//youColor
            }
            else
                p.setColor(0xFF0000FF);//everyone else color

            drawPerson(x*blockW+offX, y*blockH+offY + paddTop, blockW-offX*2, blockH-offY*2, p, canvas);
        }
    }

    private void drawPerson(float x, float y, float width, float height, Paint p, Canvas canvas) {
        //draws head
        canvas.drawCircle(x+width/2, y+width/2, width/2, p);
        //draws body
        canvas.drawRect(x, y + width, x + width, y + height, p);
    }

    public void setProgress(int p) {
        int diff = progress - index;
        index = p-diff;
        if (index<0)
            index = 0;
        progress = p;
        invalidate();
    }

    private float dist(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
}
