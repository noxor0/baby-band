package tmobile.hackathon.babyband;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
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
        backgroundPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        currentPoint = new Point(getWidth() / 2, getHeight() - profile.getHeartRate() * 2);
        Path path = new Path();
        path.moveTo(currentPoint.x, currentPoint.y);

        for (int i = points.size() - 1; i > 0; --i)
        {
            points.get(i).set(points.get(i).x - 10, points.get(i).y);
            if (points.get(i).x < 2)
                points.remove(i);

            path.lineTo(points.get(i).x, points.get(i).y);
        }

        canvas.drawPath(path, drawPaint);
        points.add(currentPoint);
    }
}
