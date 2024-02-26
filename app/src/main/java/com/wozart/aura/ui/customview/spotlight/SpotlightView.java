package com.wozart.aura.ui.customview.spotlight;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.wozart.aura.ui.customview.spotlight.target.Target;


/**
 * Spotlight View which holds a current {@link Target} and show it properly.
 **/
@SuppressLint("ViewConstructor")
class SpotlightView extends FrameLayout implements View.OnTouchListener {

    private final Paint paint = new Paint();
    private final Paint spotPaint = new Paint();
    private ValueAnimator animator;
    @ColorRes
    private int overlayColor;
    private Target currentTarget;
    private boolean showSkip;
    private String skipText;
    private Rect result;
    private OnSpotlightStateChangedListener changedListener;

    public SpotlightView(@NonNull Context context, @ColorRes int overlayColor,
                         final OnSpotlightListener listener, boolean showSkip) {
        super(context, null);
        this.showSkip = showSkip;
        this.overlayColor = overlayColor;
        bringToFront();
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        spotPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setOnClickListener(v -> {
            if (animator != null && !animator.isRunning() && (float) animator.getAnimatedValue() > 0) {
                if (listener != null) listener.onSpotlightViewClicked();
            }
        });
        setOnTouchListener(this);
    }

    public void setChangedListener(OnSpotlightStateChangedListener changedListener) {
        this.changedListener = changedListener;
    }

    public void setSkipText(String skipText) {
        this.skipText = skipText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(ContextCompat.getColor(getContext(), overlayColor));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        if (animator != null && currentTarget != null) {
            currentTarget.getShape()
                    .draw(canvas, currentTarget.getPoint(), (float) animator.getAnimatedValue(), spotPaint);
        }
        if (showSkip) {
            drawSkipText(canvas);
        }
    }

    private void drawSkipText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSize(convertDpToPixel(20f));
        paint.measureText(skipText);
        result = new Rect();
        paint.getTextBounds(skipText, 0, skipText.length(), result);
        canvas.drawText(skipText, getWidth() - result.width() - convertDpToPixel(16f), getHeight() - result.height() - convertDpToPixel(16f), paint);
    }

    private Float convertDpToPixel(Float dp) {
        return dp * (getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    void startSpotlight(long duration, TimeInterpolator animation,
                        AbstractAnimatorListener listener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);
        objectAnimator.setDuration(100L);
        objectAnimator.setInterpolator(animation);
        objectAnimator.addListener(listener);
        objectAnimator.start();
    }

    void finishSpotlight(long duration, TimeInterpolator animation,
                         AbstractAnimatorListener listener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        objectAnimator.setDuration(100L);
        objectAnimator.setInterpolator(animation);
        objectAnimator.addListener(listener);
        objectAnimator.start();
    }

    void turnUp(Target target, AbstractAnimatorListener listener) {
        currentTarget = target;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(animation -> SpotlightView.this.invalidate());
        animator.setInterpolator(target.getAnimation());
        animator.setDuration(target.getDuration());
        animator.addListener(listener);
        animator.start();
    }

    void turnDown(AbstractAnimatorListener listener) {
        if (currentTarget == null) {
            return;
        }

        animator = ValueAnimator.ofFloat(1f, 0f);
        animator.addUpdateListener(animation -> SpotlightView.this.invalidate());
        animator.addListener(listener);
        animator.setInterpolator(currentTarget.getAnimation());
        animator.setDuration(currentTarget.getDuration());
        animator.start();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP && result!=null) {
            if (motionEvent.getX()>getWidth() - result.width() - convertDpToPixel(20f)
                    &&motionEvent.getY()>getHeight() - result.height() - convertDpToPixel(20f)) {
                if (changedListener != null) {
                 changedListener.onEnded();
                }
            }
        }
        return false;
    }
}
