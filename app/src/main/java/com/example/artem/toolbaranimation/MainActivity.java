package com.example.artem.toolbaranimation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private DraggableRelativeLayout container;

    private boolean hamburgerOpened = false;
    private boolean hamburgerAnimationEnded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        container = (DraggableRelativeLayout) findViewById(R.id.container);
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_opened,
                R.string.drawer_closed);
        toggle.setDrawerIndicatorEnabled(true);
        container.setToolbarAnimator(new ToolbarAnimator(toolbar, drawer, toggle));

        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // logic to decide if drawer open/close, or pop fragment etc
                container.openCoverView();

            }
        });
        container.addCoverViewListener(new DraggableRelativeLayout.CoverViewListener() {
            @Override
            public void viewClosed() {
                animateToolbarHamburger();
            }

            @Override
            public void viewOpened() {
                animateToolbarHamburger();
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.openCoverView();
            }

        });*/
    }

    /*private void animateToolbarHamburger() {
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
        anim.setDuration(500);
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
        anim.setDuration(500);
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

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return anim;
    }*/

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
        container.onPostCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        toggle.onConfigurationChanged(newConfig);
        container.onPostCreate();
    }
}
