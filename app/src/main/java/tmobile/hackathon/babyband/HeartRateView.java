package tmobile.hackathon.babyband;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by William Zulueta on 10/10/16.
 */

public class HeartRateView extends View
{
    private ArrayList<Point> points;
    private Point currentPoint;
    private Profile profile;
    private Paint drawPaint;
    private Paint backgroundPaint;

    public void setProfile(Profile p)
    {
        profile = p;
    }

    public HeartRateView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);

        points = new ArrayList<>();
        drawPaint = new Paint();
        backgroundPaint = new Paint();
        drawPaint.setColor(Color.GREEN);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(10);
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.background_grey));
//        backgroundPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawRect(15, 15, getWidth() - 15, getHeight() - 15, backgroundPaint);
        currentPoint = new Point((getWidth() / 2) - 200, getHeight() / 2 + profile.getHeartRate() + 15);
        Path path = new Path();
        path.moveTo(currentPoint.x, currentPoint.y);

        for (int i = points.size() - 1; i > 0; --i)
        {
            points.get(i).set(points.get(i).x - 10, points.get(i).y);
            if (points.get(i).x < 15)
                points.remove(i);

            path.lineTo(points.get(i).x, points.get(i).y);
        }

        canvas.drawPath(path, drawPaint);
        points.add(currentPoint);
    }
}
