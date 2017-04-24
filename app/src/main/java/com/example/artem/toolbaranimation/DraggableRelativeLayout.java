package com.example.artem.toolbaranimation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.RelativeLayout;

/**
 * Created by artem on 23.04.17.
 */

public class DraggableRelativeLayout extends RelativeLayout implements ToolbarAnimator.ToolbarClickListener {

    private RelativeLayout rootView;
    private View appbarView;
    private View coverView;
    private View menuView;
    private CoverViewStateListener coverViewListener;
    private ToolbarAnimator toolbarAnimator;

    private static final String TAG = "MIINE";

    private final GestureDetector gestureDetector;

    private static float MENU_VIEW_MAX_Y;
    private static float MENU_VIEW_MIN_Y;
    private static final int FLING_ANIMATION_DURATION = 500;
    private static final int UP_ANIMATION_DURATION = 70;
    private static final int CLICK_ANIMATION_DURATION = 500;
    private static final int SCROLL_THRESHOLD = 70;
    private static final int SWIPE_THRESHOLD = 250;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private static int BOTTOM_Y_THRESHOLD;
    private static int MIDDLE_SWIPE_THRESHOLD;

    private boolean hasTouch = false;

    public DraggableRelativeLayout(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context, new MyGestureDetector());
        initView(context);
    }

    public DraggableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new MyGestureDetector());
        initView(context);
    }

    public DraggableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gestureDetector = new GestureDetector(context, new MyGestureDetector());
        initView(context);
    }

    private void initView(Context context) {
        rootView = (RelativeLayout) inflate(context, R.layout.draggable_layout, this);
        appbarView = rootView.findViewById(R.id.appbar);
        coverView = rootView.findViewById(R.id.main);
        menuView = rootView.findViewById(R.id.main2);
    }

    public void setToolbarAnimator(ToolbarAnimator animator) {
        toolbarAnimator = animator;
        toolbarAnimator.setToolbarClickListener(this);
        coverViewListener = toolbarAnimator;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        BOTTOM_Y_THRESHOLD = (int) (rootView.getHeight() - rootView.getHeight() * 0.15);
        MIDDLE_SWIPE_THRESHOLD = (rootView.getHeight() - appbarView.getHeight()) / 2;
        MENU_VIEW_MIN_Y = menuView.getY() - 170;
        MENU_VIEW_MAX_Y = menuView.getY();
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hasTouch = true;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            hasTouch = false;
            long duration = (coverView.getY() == appbarView.getBottom() ||
                    coverView.getY() == BOTTOM_Y_THRESHOLD) ? 0 : UP_ANIMATION_DURATION;
            if (coverView.getY() >= MIDDLE_SWIPE_THRESHOLD) {
                animateCoverViewOnScroll(BOTTOM_Y_THRESHOLD,
                        duration, true);
            } else {
                animateCoverViewOnScroll(appbarView.getBottom(),
                        duration, true);
            }
            return gestureDetector.onTouchEvent(event);
        } else {
            return gestureDetector.onTouchEvent(event);
        }
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            Log.d(TAG, "fling");
            try {
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        //swipe to bottom
                        if (coverView.getY() < BOTTOM_Y_THRESHOLD) {
                            animateCoverViewOnScroll(BOTTOM_Y_THRESHOLD,
                                    FLING_ANIMATION_DURATION, true);
                        }
                    } else {
                        //swipe to top
                        if (coverView.getY() != appbarView.getBottom()) {
                            animateCoverViewOnScroll(appbarView.getBottom(),
                                    FLING_ANIMATION_DURATION, true);
                        }
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "distance: " + distanceY);
            if (Math.abs(distanceY) >= SCROLL_THRESHOLD) {
                //fling event

            } else {
                if (distanceY > 0) {
                    //we are moving to TOP
                    if (coverView.getY() == appbarView.getBottom()) {
                        if (!hasTouch) {
                            coverViewListener.viewOpened();
                        }
                        return true;
                    } else if (coverView.getY() < appbarView.getBottom() ||
                            (coverView.getY() - distanceY) < appbarView.getBottom()) {
                        animateCoverViewOnScroll(appbarView.getBottom(),
                                0, false);
                    } else {
                        animateCoverViewOnScroll(coverView.getY() - distanceY,
                                0, false);
                    }
                } else {
                    //we are moving to bottom
                    if (coverView.getY() == BOTTOM_Y_THRESHOLD) {
                        if (!hasTouch) {
                            coverViewListener.viewClosed();
                        }
                        return true;
                    } else if (coverView.getY() > BOTTOM_Y_THRESHOLD ||
                            (coverView.getY() - distanceY) > BOTTOM_Y_THRESHOLD) {
                        animateCoverViewOnScroll(BOTTOM_Y_THRESHOLD,
                                0, false);
                    } else {
                        animateCoverViewOnScroll(coverView.getY() - distanceY,
                                0, false);
                    }
                }
                boolean notrans = false;
                float percent = getDistancePercentage();
                float distance = Math.abs(MENU_VIEW_MAX_Y) - Math.abs(MENU_VIEW_MIN_Y);
                float newY = MENU_VIEW_MAX_Y - distance * percent;
                if (menuView.getY() > MENU_VIEW_MAX_Y ||
                        newY > MENU_VIEW_MAX_Y) {
                    menuView.setY(MENU_VIEW_MAX_Y);
                    notrans = true;
                } else if (menuView.getY() < MENU_VIEW_MIN_Y ||
                        newY < MENU_VIEW_MIN_Y) {
                    menuView.setY(MENU_VIEW_MIN_Y);
                    notrans = true;
                }
                if (notrans) {
                    menuView.animate()
                            .alpha(getDistancePercentageForAlpha())
                            .setDuration(0)
                            .start();
                } else {
                    menuView.animate()
                            .alpha(getDistancePercentageForAlpha())
                            .y(newY)
                            .setDuration(0)
                            .start();
                }
            }
            return false;
        }
    }

    private void animateCoverViewOnScroll(final float valueY,
                                          final long duration,
                                          final boolean withListener) {
        ViewPropertyAnimator a = coverView.animate()
                .y(valueY)
                .setDuration(duration);
        a.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (withListener) {
                    Log.d(TAG, "with listener");
                    Log.d(TAG, "opened?: " + isMenuOpened());
                    Log.d(TAG, "------------------------------");
                    if (valueY == appbarView.getBottom()) {
                        //menu closed, cover view opened
                        coverViewListener.viewOpened();
                    } else if (valueY == BOTTOM_Y_THRESHOLD) {
                        coverViewListener.viewClosed();
                    } else {
                        coverViewListener.viewScrolling();
                    }
                    if (!isMenuOpened() && valueY == appbarView.getBottom()) {
                        Log.d(TAG, "animate menu back to bot");
                        animateMenuView(FLING_ANIMATION_DURATION, true);
                    } else if (isMenuOpened() && valueY == BOTTOM_Y_THRESHOLD) {
                        Log.d(TAG, "animate menu back to top");
                        animateMenuView(FLING_ANIMATION_DURATION, false);
                    } else {
                        animateMenuView(FLING_ANIMATION_DURATION);
                    }

                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
/*                if (withListener) {
                    Log.d(TAG, "with listener");
                    Log.d(TAG, "opened?: " + isMenuOpened());
                    Log.d(TAG, "------------------------------");
                    if (coverView.getY() == appbarView.getBottom()) {
                        //menu closed, cover view opened
                        coverViewListener.viewOpened();
                    } else if (coverView.getY() == BOTTOM_Y_THRESHOLD) {
                        coverViewListener.viewClosed();
                    } else {
                        coverViewListener.viewScrolling();
                    }
                    animateMenuView(FLING_ANIMATION_DURATION);
                }*/
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private ValueAnimator getCoverMenuYAnimation(float valueY) {
        ValueAnimator anim = ValueAnimator.ofFloat(coverView.getY(), valueY);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                coverView.setY((Float) animation.getAnimatedValue());
            }
        });
        return anim;
    }

    private ValueAnimator getMenuYAnimation(boolean opened) {
        ValueAnimator anim = ValueAnimator.ofFloat(menuView.getY(), opened ? MENU_VIEW_MAX_Y : MENU_VIEW_MIN_Y);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                menuView.setY((Float) animation.getAnimatedValue());
            }
        });
        return anim;
    }

    private ValueAnimator getMenuAlphaAnimation(boolean opened) {
        ValueAnimator anim = ValueAnimator.ofFloat(menuView.getAlpha(), opened ? 0.2F : 1);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                menuView.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        return anim;
    }

    private void animateCoverAndMenu(float valueY, final int duration, boolean opened) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(getCoverMenuYAnimation(valueY), getMenuAlphaAnimation(opened), getMenuYAnimation(opened));
        set.setDuration(duration);
        set.start();
    }

    private void animateMenuView(long duration) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(getMenuAlphaAnimation(isMenuOpened()), getMenuYAnimation(isMenuOpened()));
        set.setDuration(duration);
        set.start();
    }

    private void animateMenuView(long duration, boolean opened) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(getMenuAlphaAnimation(opened), getMenuYAnimation(opened));
        set.setDuration(duration);
        set.start();
    }

    private boolean isMenuOpened() {
        return toolbarAnimator.isHamburgerOpened();
    }

    private float getDistancePercentageForAlpha() {
        float result = (float) ((int) coverView.getY() - appbarView.getHeight()) /
                (BOTTOM_Y_THRESHOLD - appbarView.getHeight());
        return result < 0.1F ? 0.1F : result;
    }

    private float getDistancePercentage() {
        return (float) ((int) coverView.getY() - appbarView.getHeight()) /
                (BOTTOM_Y_THRESHOLD - appbarView.getHeight());
    }
    public void onPostCreate() {
        toolbarAnimator.onPostCreate();
    }

    @Override
    public void onToolbarClick(boolean opened) {
        if (opened) {
            //close menu
            animateCoverAndMenu(appbarView.getBottom(), CLICK_ANIMATION_DURATION, opened);
        } else {
            //open menu
            animateCoverAndMenu(BOTTOM_Y_THRESHOLD, CLICK_ANIMATION_DURATION, opened);
        }
    }
}
