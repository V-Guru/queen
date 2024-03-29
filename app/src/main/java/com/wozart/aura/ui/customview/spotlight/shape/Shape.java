package com.wozart.aura.ui.customview.spotlight.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;


/**
 * For any shape of target, this Shape class need to be implemented.
 */
public interface Shape {

  /**
   * draw the Shape
   *
   * @param value the animated value from 0 to 1
   */
  void draw(Canvas canvas, PointF point, float value, Paint paint);
}
