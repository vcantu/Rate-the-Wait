package healthcare.simplifi.prototype.uielements.rating;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by Viviano on 6/2/2015.
 */
public class ParkingMeasure extends CrowdMeasure {

    public static int MAX = 55;
    private float blockW, blockH, offX, offY;

    public ParkingMeasure(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        blockW = getWidth() / 11;
        blockH = getHeight() / 5;
        offX = blockW * .1f;
        offY = blockH * .1f;
    }

    @Override
    public void drawYou(Paint p, Canvas canvas) {
        p.setColor(0xFF0FDDAF);
        canvas.drawRect(5 * blockW + offX, 2 * blockH + offY,
                6 * blockW - offX, 3 * blockH - offY, p);
    }

    @Override
    public void drawOthers(Paint p, Canvas canvas) {
        p.setColor(0xFF0000FF);
        for (int i=0; i<progress && i<locs.size(); i++) {
            Loc l = locs.get(i);
            canvas.drawRect(l.x * blockW + offX, l.y * blockH + offY,
                    (l.x+1) * blockW - offX, (l.y+1) * blockH - offY, p);
        }
    }

    @Override
    public void addNewLoc() {
        Loc test = (new Loc((int)(Math.random() * 11),
                            (int)(Math.random() * 5)));
        //if in center
        if (test.x == 5 && test.y ==  2) {
            addNewLoc(); return;
        }
        for (Loc l : locs) {
            if (test.x == l.x &&  test.y == l.y) {
                addNewLoc(); return;
            }
        }
        locs.add(test);
    }
}
