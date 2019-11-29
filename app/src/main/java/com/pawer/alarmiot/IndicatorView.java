package com.pawer.alarmiot;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;

public class IndicatorView extends View {

    private Paint paint;
    private Paint paintBlur;
    private int iColor;

    public IndicatorView(Context context, int mColor) {
        super(context);
        this.iColor = mColor;
        init();
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(iColor);
        paint.setStrokeWidth(20f);
        paint.setStyle(Paint.Style.FILL);

        paintBlur = new Paint();
        paintBlur.set(paint);
        paintBlur.setColor(iColor);
        paintBlur.setStrokeWidth(30f);
        paintBlur.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.SOLID));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int width = getWidth() - getPaddingLeft() - getPaddingRight();
        final int height = getHeight() - getPaddingTop() - getPaddingBottom();

        final int cx = width / 2;
        final int cy = height / 2;

        final float diameter = Math.min(width, height) - paint.getStrokeWidth();
        final float radius = diameter / 2;

        canvas.drawCircle(cx, cy, radius, paint);
        canvas.drawCircle(cx, cy, radius, paintBlur);
    }

    public int getmColor() {
        return iColor;
    }

    public void setmColor(@ColorInt int iColor) {
        this.iColor = iColor;
        paint.setColor(iColor);
        invalidate();
    }
}