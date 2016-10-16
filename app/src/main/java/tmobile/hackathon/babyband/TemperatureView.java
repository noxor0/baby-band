package tmobile.hackathon.babyband;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by William Zulueta on 10/16/16.
 */

public class TemperatureView extends View
{
    private Profile profile;
    private Paint drawPaint;
    private Paint backgroundPaint;

    public void setProfile(Profile p)
    {
        profile = p;
    }

    public TemperatureView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);

        drawPaint = new Paint();
        backgroundPaint = new Paint();
        drawPaint.setColor(Color.RED);
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.background_grey));
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
//        for (int i = 1; i < 6; ++i)
//        {
//            canvas.drawRect(i * 15, getHeight() - 15, 50, getHeight() - 100, drawPaint);
//        }
//        canvas.drawRect(15, getHeight() - 15, getWidth(), getHeight() - 100, drawPaint);
    }
}
