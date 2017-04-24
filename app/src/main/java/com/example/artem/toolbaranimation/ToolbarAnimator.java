package com.example.artem.toolbaranimation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;

/**
 * Created by artem on 24.04.17.
 */

public class ToolbarAnimator implements CoverViewStateListener{

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private boolean hamburgerOpened = false;
    private boolean hamburgerAnimationEnded = true;
    private ToolbarClickListener toolbarClickListener;

    private static long ANIMATION_DURATION = 500;

    public ToolbarAnimator(Toolbar toolbar, DrawerLayout drawer, ActionBarDrawerToggle toggle) {
        this.toolbar = toolbar;
        this.drawer = drawer;
        this.toggle = toggle;
        this.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateToolbarHamburger();
                toolbarClickListener.onToolbarClick(hamburgerOpened);
            }
        });

        this.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateToolbarHamburger();
                toolbarClickListener.onToolbarClick(hamburgerOpened);
            }
        });
    }

    public void setToolbarClickListener(ToolbarClickListener listener) {
        toolbarClickListener = listener;
    }

    /**
     * set hamburger animation duration
     * default value is 500
     * @param duration
     */
    public void setAnimationDuration(long duration) {
        this.ANIMATION_DURATION = duration;
    }

    public void startToolbarAnimation(boolean start) {
        if (start) {
            animateToolbarHamburger();
        }
    }

    public boolean isHamburgerOpened() {
        return hamburgerOpened;
    }

    private void animateToolbarHamburger() {
        if (hamburgerAnimationEnded) {
            getArrowAnimation().start();
            if (!hamburgerOpened) {
                getRotationAnimation(180, 90).start();
            } else {
                getRotationAnimation(90, 0).start();
            }
        }
    }

    private ValueAnimator getRotationAnimation(float start, float end) {
        ValueAnimator anim = ValueAnimator.ofFloat(start, end);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset =  (Float) valueAnimator.getAnimatedValue();
                for (int i = 0; i < toolbar.getChildCount(); i++) {
                    if (toolbar.getChildAt(i) instanceof ImageButton) {
                        toolbar.getChildAt(i).setRotation(slideOffset);
                    }
                }
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        return anim;
    }

    private ValueAnimator getArrowAnimation() {
        ValueAnimator anim = !hamburgerOpened ? ValueAnimator.ofFloat(0, 1) : ValueAnimator.ofFloat(1, 0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                toggle.onDrawerSlide(drawer, slideOffset);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                hamburgerAnimationEnded = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hamburgerOpened = !hamburgerOpened;
                hamburgerAnimationEnded = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                //do nothing
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //do nothing
            }
        });
        return anim;
    }

    @Override
    public void viewOpened() {
        if (hamburgerOpened) {
            animateToolbarHamburger();
        }
    }

    @Override
    public void viewClosed() {
        if (!hamburgerOpened) {
            animateToolbarHamburger();
        }
    }

    @Override
    public void viewScrolling() {

    }

    public void onPostCreate() {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            if (toolbar.getChildAt(i) instanceof ImageButton) {
                toolbar.getChildAt(i).setRotation(180);
            }
        }
    }

    interface ToolbarClickListener {
        void onToolbarClick(boolean opened);
    }
}
