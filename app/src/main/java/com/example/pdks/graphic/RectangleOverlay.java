package com.example.pdks.graphic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.example.pdks.utils.CameraUtils;
import com.google.mlkit.vision.face.Face;

public class RectangleOverlay extends GraphicOverlay.Graphic {

    private GraphicOverlay graphicOverlay;
    private Face face;
    private Rect rect;
    private Paint boxPaint;

    public RectangleOverlay(GraphicOverlay graphicOverlay, Face face, Rect rect) {
        super(graphicOverlay);
        this.graphicOverlay = graphicOverlay;
        this.face = face;
        this.rect = rect;

        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(3.0f);
    }

    @Override
    public void draw(Canvas canvas) {
        RectF mappedRect = CameraUtils.calculateRect(graphicOverlay,
                rect.height(),
                rect.width(),
                face.getBoundingBox());

        canvas.drawRect(mappedRect, boxPaint);
    }
}
