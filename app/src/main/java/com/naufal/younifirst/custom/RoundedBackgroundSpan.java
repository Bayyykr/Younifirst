package com.naufal.younifirst.custom;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

public class RoundedBackgroundSpan extends ReplacementSpan {

    private final int backgroundColor;
    private final int textColor;
    private final int padding;

    public RoundedBackgroundSpan(int backgroundColor, int textColor, int padding) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.padding = padding;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end) + padding * 2);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        float width = paint.measureText(text, start, end);
        float height = bottom - top;

        Paint bgPaint = new Paint(paint);
        bgPaint.setColor(backgroundColor);
        bgPaint.setAntiAlias(true);

        canvas.drawRoundRect(x, top, x + width + padding * 2, bottom, height / 2f, height / 2f, bgPaint);

        paint.setColor(textColor);
        canvas.drawText(text, start, end, x + padding, y, paint);
    }
}
