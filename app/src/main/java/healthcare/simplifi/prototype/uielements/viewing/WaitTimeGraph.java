package healthcare.simplifi.prototype.uielements.viewing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Viviano on 6/1/2015.
 */
public class WaitTimeGraph extends View {

    private Paint p = new Paint();

    private float width, height, paddX, paddY;

    public WaitTimeGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getWidth();
        height = getHeight();
        paddX = width * .1f;
        paddY = height * .1f;
    }

    @Override
    public void onDraw(Canvas canvas) {
//        p.setColor(0xFFb20000);
//        canvas.drawRect(0, 0, getWidth(), getHeight(), p);

        drawGraphLines(p, canvas);
    }

    public void drawGraphLines(Paint p, Canvas canvas) {
        p.setColor(0xFF626262);
        p.setStrokeWidth(5);
        canvas.drawLine(paddX*2, paddY, paddX, height - paddY + 2.5f, p);
        canvas.drawLine(paddX*2, height - paddY, width - paddX, height - paddY, p);
    }

}
