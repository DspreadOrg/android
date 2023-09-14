package com.dspread.demoui.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationUtil {

    /*Animation类主要包括视图动画(View Animation)，其中视图动画又包括补间动画(Tween Animation)和逐帧动画(Frame Animation)*/

    /*Interpolator*/

    /*
      AccelerateDecelerateInterpolator(@android:anim/accelerate_decelerate_interpolator)
      在动画开始与结束的地方速率改变比较慢，在中间的时候加速
     */

    /*
      AccelerateInterpolator(@android:anim/accelerate_interpolator)
      在动画开始的地方速率改变比较慢，然后开始加速
     */

    /*
      AnticipateInterpolator(@android:anim/anticipate_interpolator)
      开始的时候向后，然后向前甩
     */

    /*
      AnticipateOvershootInterpolator(@android:anim/anticipate_overshoot_interpolator)
      开始的时候向后，然后向前甩一定值后返回最后的值
     */

    /*
      BounceInterpolator(@android:anim/bounce_interpolator)
      动画结束的时候弹起
     */

    /*
      CycleInterpolator(@android:anim/cycle_interpolator)
      动画循环播放特定的次数，速率改变沿着正弦曲线
     */

    /*
      DeceleratorInterpolator(@android:anim/decelerator_interpolator)
      在动画开始的地方速率改变比较快，然后开始减速
     */

    /*
      LinearInterpolator(@android:anim/linear_interpolator)
      以常量速率改变
     */

    /*
      OvershootInterpolator(@android:anim/overshoot_interpolator)
      向前甩一定值后再回到原来位置
     */

    private AnimationUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 获取位移动画
     *
     * @param fromX
     * @param toX
     * @param fromY
     * @param toY
     * @param duration
     * @return
     */
    public static TranslateAnimation getTranslateAnimation(float fromX, float toX, float fromY,
                                                           float toY, long duration) {
        TranslateAnimation translateAnimation = new TranslateAnimation(fromX, toX, fromY, toY);
        translateAnimation.setDuration(duration);
        return translateAnimation;
    }

    /**
     * 获取缩放动画
     *
     * @param fromX
     * @param toX
     * @param fromY
     * @param toY
     * @param duration
     * @return
     */
    public static ScaleAnimation getScaleAnimation(float fromX, float toX, float fromY, float
            toY, long duration) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromX, toX, fromY, toY, Animation
                .RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(duration);
        return scaleAnimation;
    }

    /**
     * 获取旋转动画
     *
     * @param fromDegrees
     * @param toDegrees
     * @param duration
     * @param infinite
     * @return
     */
    public static RotateAnimation getRotateAnimation(float fromDegrees, float toDegrees, long
            duration, boolean infinite) {
        RotateAnimation rotateAnimation = new RotateAnimation(fromDegrees, toDegrees, Animation
                .RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(duration);
        if (infinite) {
            rotateAnimation.setRepeatCount(Animation.INFINITE);//无限循环
        }
        return rotateAnimation;
    }

    /**
     * 获取渐变动画
     *
     * @param fromAlpha
     * @param toAlpha
     * @param duration
     * @return
     */
    public static AlphaAnimation getAlphaAnimation(float fromAlpha, float toAlpha, long duration) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        alphaAnimation.setDuration(duration);
        return alphaAnimation;
    }
}