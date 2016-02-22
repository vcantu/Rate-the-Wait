package healthcare.simplifi.prototype;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import com.larvalabs.svgandroid.SVG;

/**
 * Created by Viviano on 5/13/2015.
 */
public class Draw {

    //Draws text centered
    public static void text(String s, float x, float y, Paint p, Canvas canvas) {
        canvas.drawText(s, x - p.measureText(s) / 2, y - (p.ascent() + p.descent()) / 2, p);
    }
    //Draw text with size
    public static void text(String s, float x, float y, float size, Paint p, Canvas canvas) {
        float temp = p.getTextSize();
        p.setTextSize(size);
        text(s, x, y, p, canvas);
        p.setTextSize(temp);
    }

    public static void textOnX(String s, float x, float y, Paint p, Canvas canvas) {
        canvas.drawText(s, x - p.measureText(s) / 2, y, p);
    }

    public static void flatButton(float x, float y, float radius, Object o, int back, int front,
                                  float pressedFrac, Paint p, Canvas canvas) {
        p.setColor(back);
        if (!(o instanceof SVG))
            canvas.drawCircle(x, y, radius, p);
        p.setColor(front);
        if (o instanceof Bitmap) {
            p.setColorFilter(new LightingColorFilter(front, 0));
            bitmap((Bitmap)o, x, y, 1, p, canvas);
            p.setColorFilter(null);
        }
        else if (o instanceof SVG) {
            svg((SVG)o, x, y, radius*2, radius*2, canvas);
        }
        else if (o instanceof String) {
            p.setTextSize(30);
            canvas.drawText((String)o, x - radius, y, p);
        }
        if (pressedFrac >= 0) {
            float r  = (radius * 1.5f) * pressedFrac;
            r = r > radius ? radius : r;
            p.setColor(Color.argb(200 - (int)(200 * pressedFrac), 255, 255, 255));
            canvas.drawCircle(x, y, r, p);
        }
    }

    //TODO: this is broken and VERY inefficient
    public static void floatingButton(float x, float y, float radius, Bitmap icon, int back, int front,
                                      float height, boolean pressed, Paint p, Canvas canvas) {
        //Shadow
        for (int i=0; i<3; i++) {
            p.setShader(new RadialGradient(x, y + height, radius + height / 2,
                    0xFF000000, 0x00000000, Shader.TileMode.CLAMP));
            canvas.drawPaint(p);
        }
        p.setShader(null);

        p.setColor(back);
        if (pressed)
            y += height;
        canvas.drawCircle(x, y, radius, p);

        if (icon != null) {
            p.setColorFilter(new LightingColorFilter(front, 0));
            bitmap(icon, x, y, 1, p, canvas);
            p.setColorFilter(null);
        }
    }

    //centers bitmap and scales it to scale
    public static void bitmap(Bitmap bmp, float x, float y, float scale, Paint p, Canvas canvas) {
        float width = bmp.getWidth() * scale;
        float height = bmp.getHeight() * scale;
        canvas.drawBitmap(bmp, null, new RectF(x - width / 2, y - height / 2, x + width / 2, y + height / 2), p);
    }

    public static void svg(SVG svg, float x, float y, float w, float h, Canvas canvas) {
        canvas.drawPicture(svg.getPicture(), new RectF(x - w / 2, y - h / 2, x + w / 2, y + h / 2));
    }
}
