package com.cj.video.playVideo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Created by cai.jia on 2017/7/4 0004
 */

public abstract class GestureVideoController extends RelativeLayout {

    private static final int NONE = 0;
    private static final int HORIZONTAL = 1;
    private static final int VERTICAL_LEFT = 2;
    private static final int VERTICAL_RIGHT = 3;

    private float initialX;
    private float initialY;
    private float startX;
    private float startY;
    private int orientation = NONE;

    private int touchSlop;

    public GestureVideoController(Context context) {
        this(context,null);
    }

    public GestureVideoController(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GestureVideoController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GestureVideoController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration config = ViewConfiguration.get(context);
        touchSlop = config.getScaledTouchSlop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                initialX = x;
                initialY = y;
                startX = x;
                startY = y;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float deltaX = x - startX;
                float deltaY = y - startY;

                float distanceX = x - initialX;
                float distanceY = y - initialY;
                boolean isFirst = false;

                if (orientation == NONE && Math.abs(distanceX) > Math.abs(distanceY)
                        && Math.abs(distanceX) > touchSlop) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    orientation = HORIZONTAL;
                    initialX = distanceX > 0 ? initialX + touchSlop : initialX - touchSlop;
                    distanceX = x - initialX;
                    onHorizontalMove(START,distanceX,0);
                    isFirst = true;
                }

                if (orientation == NONE && Math.abs(distanceY) > Math.abs(distanceX)
                        && Math.abs(distanceY) > touchSlop) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    boolean left = x < getWidth() / 2;
                    orientation = left ? VERTICAL_LEFT : VERTICAL_RIGHT;
                    initialY = distanceY > 0 ? initialY + touchSlop : initialY - touchSlop;
                    distanceY = y - initialY;

                    if (left) {
                        onLeftVerticalMove(START,-distanceY,0);
                    }else{
                        onRightVerticalMove(START,-distanceY,0);
                    }
                    isFirst = true;
                }

                startX = x;
                startY = y;
                if (isFirst) {
                    return super.onTouchEvent(event);
                }

                switch (orientation) {
                    case HORIZONTAL: {
                        onHorizontalMove(MOVE,distanceX,deltaX);
                        break;
                    }

                    case VERTICAL_LEFT: {
                        onLeftVerticalMove(MOVE,-distanceY,-deltaY);
                        break;
                    }

                    case VERTICAL_RIGHT: {
                        onRightVerticalMove(MOVE,-distanceY,-deltaY);
                        break;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                float distanceX = x - initialX;
                float distanceY = y - initialY;
                float deltaX = x - startX;
                float deltaY = y - startY;

                switch (orientation) {
                    case HORIZONTAL: {
                        onHorizontalMove(END,distanceX,deltaX);
                        break;
                    }

                    case VERTICAL_LEFT: {
                        onLeftVerticalMove(END,-distanceY,-deltaY);
                        break;
                    }

                    case VERTICAL_RIGHT: {
                        onRightVerticalMove(END,-distanceY,-deltaY);
                        break;
                    }
                }

                if (orientation != NONE) {
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                initialX = 0;
                initialY = 0;
                startX = 0;
                startY = 0;
                orientation = NONE;
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    public static final int START = 1;
    public static final int MOVE = 2;
    public static final int END = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({START,MOVE,END})
    public @interface MoveState{
    }

    public abstract void onLeftVerticalMove(@MoveState int state,float distance,float deltaY);

    public abstract void onRightVerticalMove(@MoveState int state,float distance,float deltaY);

    public abstract void onHorizontalMove(@MoveState int state,float distance,float deltaX);
}
