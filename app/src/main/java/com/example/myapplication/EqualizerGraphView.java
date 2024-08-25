package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class EqualizerGraphView extends View {

    private Paint paint;
    private float[] amplitudes; // Array to hold the amplitude values for different frequencies
    private static final int NUM_BANDS = 5; // Number of frequency bands
    private float maxAmplitude = 500f; // Updated maximum amplitude value for scaling
    private float offsetX = 85f; // Offset to move the starting point to the right

    public EqualizerGraphView(Context context) {
        super(context);
        init();
    }

    public EqualizerGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EqualizerGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        amplitudes = new float[NUM_BANDS];
    }

    public void setAmplitudes(float[] amplitudes) {
        if (amplitudes.length == NUM_BANDS) {
            this.amplitudes = amplitudes;
            invalidate(); // Redraw the view
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (amplitudes == null || amplitudes.length == 0) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float barWidth = width / (NUM_BANDS - 1); // Distance between points
        float scale = height / (2 * maxAmplitude); // Scale factor to fit amplitude values in the view

        Path path = new Path();
        // Start drawing the path with the offset
        path.moveTo(offsetX, height / 2 - (amplitudes[0] * scale));

        for (int i = 1; i < amplitudes.length; i++) {
            float x = offsetX + i * barWidth; // Apply the offset to the x-coordinate
            float y = height / 2 - (amplitudes[i] * scale);

            // Create smooth curves using quadTo
            float prevX = offsetX + (i - 1) * barWidth;
            float prevY = height / 2 - (amplitudes[i - 1] * scale);
            path.quadTo(prevX, prevY, (prevX + x) / 2, (prevY + y) / 2);
        }

        // Draw the path
        canvas.drawPath(path, paint);
    }
}
