package com.ckenergy.transitionsview.transitions;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

public class MoveTransfer implements ITransferView {

    private final String TAG = getClass().getSimpleName();

    private long duration;

    AnimatorSet startSet;

    ObjectAnimator translationX;
    ObjectAnimator translationY;
    ObjectAnimator scaleX;
    ObjectAnimator scaleY;

    Animator.AnimatorListener startListener;

    public MoveTransfer() {
        duration = 300;
    }

    @Override
    public void start(MoveInfo bean, View child, final OnShowListener listener) {
        translationX = ObjectAnimator.ofFloat(child, "translationX", bean.translationX, 0);
        translationY = ObjectAnimator.ofFloat(child, "translationY", bean.translationY, 0);
        scaleX = ObjectAnimator.ofFloat(child, "scaleX", bean.scale, 1);
        scaleY = ObjectAnimator.ofFloat(child, "scaleY", bean.scale, 1);
        translationY.setInterpolator(new AccelerateInterpolator());
        List<Animator> animators = new ArrayList<>();
        animators.add(translationX);
        animators.add(translationY);
        animators.add(scaleX);
        animators.add(scaleY);
        startSet = new AnimatorSet();
        startSet.playTogether(animators);
        if (listener != null) {
            startListener = new Animator.AnimatorListener() {
                boolean isCancel;
                @Override public void onAnimationStart(Animator animation) {
                    isCancel = false;
                    listener.onStart();
                }

                @Override public void onAnimationEnd(Animator animation) {
                    if (!isCancel) {
                        listener.onEnd();
                    }
                }

                @Override public void onAnimationCancel(Animator animation) {
                    isCancel = true;
                }
                @Override public void onAnimationRepeat(Animator animation) {}
            };
            startSet.addListener(startListener);
        }
        startSet.setDuration(duration).start();
    }

    public void back(MoveInfo bean, View child, final OnShowListener listener) {
        if (startSet == null) {
            return;
        }
        startSet.cancel();
        Float x = (Float) translationX.getAnimatedValue();
        Float y = (Float) translationY.getAnimatedValue();
        Float scaleXF = (Float) scaleX.getAnimatedValue();
        Float scaleYF = (Float) scaleY.getAnimatedValue();

        translationY.setInterpolator(new DecelerateInterpolator());
        translationX.setFloatValues(x, bean.translationX);
        translationY.setFloatValues(y, bean.translationY);
        scaleX.setFloatValues(scaleXF, bean.scale);
        scaleY.setFloatValues(scaleYF, bean.scale);

        startSet.removeListener(startListener);
        if (listener != null) {
            startSet.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation) {
                    listener.onStart();
                }

                @Override public void onAnimationEnd(Animator animation) {
                    listener.onEnd();
                }
                @Override public void onAnimationCancel(Animator animation) {}
                @Override public void onAnimationRepeat(Animator animation) {}
            });
        }
        long durationEnd =  (long)(duration*translationX.getAnimatedFraction());
        startSet.setDuration(durationEnd).start();
    }

}
