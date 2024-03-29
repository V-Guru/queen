package com.wozart.aura.ui.customview.spotlight;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.wozart.aura.R;
import com.wozart.aura.ui.customview.spotlight.target.Target;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;


public class Spotlight {

    @ColorRes
    private static final int DEFAULT_OVERLAY_COLOR = R.color.transparent_black;
    private static final long DEFAULT_DURATION = 10L;
    private static final TimeInterpolator DEFAULT_ANIMATION = new DecelerateInterpolator(1f);

    private static WeakReference<SpotlightView> spotlightViewWeakReference;
    private static WeakReference<Activity> contextWeakReference;
    private ArrayList<? extends Target> targets;
    private long duration = DEFAULT_DURATION;
    private TimeInterpolator animation = DEFAULT_ANIMATION;
    private OnSpotlightStateChangedListener spotlightListener;
    private int overlayColor = DEFAULT_OVERLAY_COLOR;
    private boolean isClosedOnTouchedOutside = true;
    private boolean showSkip = false;
    private String skipText = "Skip";

    private Spotlight(Activity activity) {
        contextWeakReference = new WeakReference<>(activity);
    }

    public static Spotlight with(@NonNull Activity activity) {
        return new Spotlight(activity);
    }

    private static Context getContext() {
        return contextWeakReference.get();
    }

    @Nullable
    private static SpotlightView getSpotlightView() {
        return spotlightViewWeakReference.get();
    }

    /**
     * sets {@link Target}s to Spotlight
     *
     * @param targets targets to show
     * @return the Spotlight
     */
    @SafeVarargs
    public final <T extends Target> Spotlight setTargets(@NonNull T... targets) {
        this.targets = new ArrayList<>(Arrays.asList(targets));

        return this;
    }

    /**
     * sets {@link Target}s to Spotlight
     *
     * @param targets targets as ArrayList to show
     * @return the Spotlight
     */
    public final <T extends Target> Spotlight setTargets(@NonNull ArrayList<T> targets) {
        this.targets = targets;

        return this;
    }

    /**
     * sets spotlight background color to Spotlight
     *
     * @param overlayColor background color to be used for the spotlight overlay
     * @return the Spotlight
     */
    public Spotlight setOverlayColor(@ColorRes int overlayColor) {
        this.overlayColor = overlayColor;
        return this;
    }


    /**
     * sets duration to {@link Target} Animation
     *
     * @param duration duration of Target Animation
     * @return the Spotlight
     */
    public Spotlight setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public Spotlight setSkipText(String skipText) {
        this.skipText = skipText;
        return this;
    }

    /**
     * sets interpolator to {@link Target} Animation
     *
     * @param animation type of Target Animation
     * @return the Spotlight
     */
    public Spotlight setAnimation(TimeInterpolator animation) {
        this.animation = animation;
        return this;
    }

    /**
     * Sets {@link OnSpotlightStateChangedListener}
     *
     * @param listener OnSpotlightEndedListener of Spotlight
     * @return This Spotlight
     */
    public Spotlight setOnSpotlightStateListener(
            @NonNull final OnSpotlightStateChangedListener listener) {
        spotlightListener = listener;
        return this;
    }

    /**
     * Sets if Spotlight closes Target if touched outside
     *
     * @param isClosedOnTouchedOutside OnSpotlightEndedListener of Spotlight
     * @return This Spotlight
     */
    public Spotlight setClosedOnTouchedOutside(boolean isClosedOnTouchedOutside) {
        this.isClosedOnTouchedOutside = isClosedOnTouchedOutside;
        return this;
    }

    public Spotlight showSkipButton(boolean showSkip) {
        this.showSkip = showSkip;
        return this;
    }

    /**
     * Shows {@link SpotlightView}
     */
    public void start() {
        spotlightView();
    }

    /**
     * close the current {@link Target}
     */
    public void closeCurrentTarget() {
        finishTarget();
    }

    /**
     * close the {@link Spotlight}
     */
    public void closeSpotlight() {
        finishSpotlight();
    }

    /**
     * Creates the spotlight view and starts
     */
    @SuppressWarnings("unchecked")
    private void spotlightView() {
        if (getContext() == null) {
            throw new RuntimeException("context is null");
        }
        final View decorView = ((Activity) getContext()).getWindow().getDecorView();
        SpotlightView spotlightView =
                new SpotlightView(getContext(), overlayColor, () -> {
                    if (isClosedOnTouchedOutside) {
                        finishTarget();
                    }
                }, showSkip);
        if (showSkip) {
            spotlightView.setSkipText(skipText);
            spotlightView.setChangedListener(new OnSpotlightStateChangedListener() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onEnded() {
                  finishSpotlight();
                }
            });
        }
        spotlightViewWeakReference = new WeakReference<>(spotlightView);
        ((ViewGroup) decorView).addView(spotlightView);
        spotlightView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
        startSpotlight();
    }

    /**
     * show Target
     */
    @SuppressWarnings("unchecked")
    private void startTarget() {
        if (targets != null && targets.size() > 0 && getSpotlightView() != null) {
            final Target target = targets.get(0);
            SpotlightView spotlightView = getSpotlightView();
            spotlightView.removeAllViews();
            spotlightView.addView(target.getOverlay());
            spotlightView.turnUp(target, new AbstractAnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (target.getListener() != null) target.getListener().onStarted(target);
                }
            });
        }
    }

    /**
     * show Spotlight
     */
    private void startSpotlight() {
        if (getSpotlightView() == null) return;
        getSpotlightView().startSpotlight(duration, animation, new AbstractAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (spotlightListener != null) spotlightListener.onStarted();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startTarget();
            }
        });
    }

    /**
     * hide Target
     */
    @SuppressWarnings("unchecked")
    private void finishTarget() {
        if (targets != null && targets.size() > 0 && getSpotlightView() != null) {
            getSpotlightView().turnDown(new AbstractAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!targets.isEmpty()) {
                        Target target = targets.remove(0);
                        if (target.getListener() != null) target.getListener().onEnded(target);
                        if (targets.size() > 0) {
                            startTarget();
                        } else {
                            finishSpotlight();
                        }
                    }
                }
            });
        }
    }

    /**
     * hide Spotlight
     */
    private void finishSpotlight() {
        if (getSpotlightView() != null) {
            getSpotlightView().finishSpotlight(duration, animation, new AbstractAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Activity activity = (Activity) getContext();
                    if (activity != null) {
                        final View decorView = activity.getWindow().getDecorView();
                        Animation mainAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
                        mainAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                ((ViewGroup) decorView).removeView(getSpotlightView());
                                if (spotlightListener != null) spotlightListener.onEnded();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        getSpotlightView().startAnimation(mainAnim);
                    }
                }
            });
        }
    }
}
