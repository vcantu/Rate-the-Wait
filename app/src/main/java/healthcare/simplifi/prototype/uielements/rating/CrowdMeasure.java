package healthcare.simplifi.prototype.uielements.rating;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CrowdMeasure extends View {

    public static int MAX = 100;

    private Paint p = new Paint();

    protected class Loc {
        float x;
        float y;

        public Loc(float X, float Y) {
            x = X;
            y = Y;
        }
    }

    protected List<Loc> locs = new ArrayList<Loc>();

    protected int progress = 0;

    //temporary radius
    private int CIRCLE_RAD = 20;

    public CrowdMeasure(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawYou(p, canvas);
        //if there is nothing to draw stop
        if (locs.size() == 0)
            return;
        drawOthers(p, canvas);
    }

    //overridden
    protected void drawYou(Paint p, Canvas canvas) {
        p.setColor(0xFF0FDDAF);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, CIRCLE_RAD, p);
    }
    //overridden
    protected void drawOthers(Paint p, Canvas canvas) {
        p.setColor(0xFF0000FF);
        for (int i=0; i<progress && i<locs.size(); i++) {
            Loc l = locs.get(i);
            canvas.drawCircle(l.x, l.y, CIRCLE_RAD, p);
        }
    }
    //overridden
    protected void addNewLoc() {
        Loc test = (new Loc(CIRCLE_RAD + (float)(Math.random() * (getWidth() - 2 * CIRCLE_RAD)),
                CIRCLE_RAD + (float)(Math.random() * (getHeight() - 2 * CIRCLE_RAD))));

        if (dist(test.x, test.y, getWidth() / 2, getHeight() / 2) < CIRCLE_RAD * 2) {
            addNewLoc(); return;
        }
        for (Loc l : locs) {
            if (dist(test.x, test.y, l.x, l.y) < CIRCLE_RAD * 2 ||
                    (progress <= 30 &&
                            dist(getWidth() / 2, getHeight() / 2, test.x, test.y) > getWidth() / 3)) {
                addNewLoc(); return;
            }
        }
        locs.add(test);
    }

    //sets progress to p, and adds new lock the amount of times needed
    public void setProgress(int p) {
        progress = p;
        int diff = progress - locs.size()-1;
        if (diff > 0) {
            for (int i=0; i<diff; i++) {
                addNewLoc();
            }
        }
        invalidate();
    }

    protected float dist(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }
}
