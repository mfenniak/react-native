/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.views.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Spanned;
import android.widget.TextView;

import com.facebook.csslayout.Spacing;
import com.facebook.react.uimanager.ReactCompoundView;

import javax.annotation.Nullable;

public class ReactTextView extends TextView implements ReactCompoundView {
  public static enum TextDecorationStyle {
    SOLID,
    DOUBLE,
    DASHED,
    DOTTED;

    /*
    public @Nullable PathEffect getPathEffect(float borderWidth) {
      switch (this) {
        case SOLID:
          return null;

        case DASHED:
          return new DashPathEffect(
                  new float[] {borderWidth*3, borderWidth*3, borderWidth*3, borderWidth*3}, 0);

        case DOTTED:
          return new DashPathEffect(
                  new float[] {borderWidth, borderWidth, borderWidth, borderWidth}, 0);

        default:
          return null;
      }
    }
    */
  }

  public static enum TextDecorationLine {
    UNDERLINE,
    LINE_THROUGH,
    UNDERLINE_LINE_THROUGH;
  }

  private @Nullable TextDecorationLine mTextDecorationLine;
  private @Nullable TextDecorationStyle mTextDecorationStyle;
  private @Nullable Integer mTextDecorationColor;

  public ReactTextView(Context context) {
    super(context);
  }

  @Override
  public int reactTagForTouch(float touchX, float touchY) {
    Spanned text = (Spanned) getText();
    int target = getId();

    int x = (int) touchX;
    int y = (int) touchY;

    Layout layout = getLayout();
    int line = layout.getLineForVertical(y);

    int lineStartX = (int) layout.getLineLeft(line);
    int lineEndX = (int) layout.getLineRight(line);

    // TODO(5966918): Consider extending touchable area for text spans by some DP constant
    if (x >= lineStartX && x <= lineEndX) {
      int index = layout.getOffsetForHorizontal(line, x);

      // We choose the most inner span (shortest) containing character at the given index
      // if no such span can be found we will send the textview's react id as a touch handler
      // In case when there are more than one spans with same length we choose the last one
      // from the spans[] array, since it correspond to the most inner react element
      ReactTagSpan[] spans = text.getSpans(index, index, ReactTagSpan.class);

      if (spans != null) {
        int targetSpanTextLength = text.length();
        for (int i = 0; i < spans.length; i++) {
          int spanStart = text.getSpanStart(spans[i]);
          int spanEnd = text.getSpanEnd(spans[i]);
          if (spanEnd > index && (spanEnd - spanStart) <= targetSpanTextLength) {
            target = spans[i].getReactTag();
            targetSpanTextLength = (spanEnd - spanStart);
          }
        }
      }
    }

    return target;
  }

  @Nullable
  public TextDecorationLine getTextDecorationLine() {
    return mTextDecorationLine;
  }

  public void setTextDecorationLine(@Nullable TextDecorationLine textDecorationLine) {
    mTextDecorationLine = textDecorationLine;
  }

  @Nullable
  public TextDecorationStyle getTextDecorationStyle() {
    return mTextDecorationStyle;
  }

  public void setTextDecorationStyle(@Nullable TextDecorationStyle textDecorationStyle) {
    mTextDecorationStyle = textDecorationStyle;
  }

  @Nullable
  public Integer getTextDecorationColor() {
    return mTextDecorationColor;
  }

  public void setTextDecorationColor(@Nullable Integer textDecorationColor) {
    mTextDecorationColor = textDecorationColor;
  }

  private void drawTextDecoration(Canvas canvas) {
    Paint paint = new Paint(); // FIXME: remove allocation during draw
    paint.setStyle(Paint.Style.STROKE);
    paint.setColor(getTextColors().getDefaultColor()); // FIXME: add textDecorationColor
    paint.setStrokeWidth(1f); // FIXME: not sure of a reasonable default

    // FIXME: support textDecorationStyle

    int count = getLineCount();

    Layout layout = getLayout();
    float x_start, x_stop, x_diff;
    int firstCharInLine, lastCharInLine;
    Rect rect = new Rect(); // FIXME: remove allocation during draw
    int strokeWidth = 1;

    for (int i = 0; i < count; i++) {


      int baseline = getLineBounds(i, rect);
      firstCharInLine = layout.getLineStart(i);
      lastCharInLine = layout.getLineEnd(i);

      x_start = layout.getPrimaryHorizontal(firstCharInLine);
      x_diff = layout.getPrimaryHorizontal(firstCharInLine + 1) - x_start;
      x_stop = layout.getPrimaryHorizontal(lastCharInLine - 1) + x_diff;

      canvas.drawLine(x_start, baseline + strokeWidth, x_stop, baseline + strokeWidth, paint);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mTextDecorationLine != null) {
      drawTextDecoration(canvas);
    }
    super.onDraw(canvas);
  }
}
