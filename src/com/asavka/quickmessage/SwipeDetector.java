package com.asavka.quickmessage;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SwipeDetector implements OnTouchListener {
	
	public static enum Action {
        LR, // Left to Right
        RL, // Right to Left
        TB, // Top to bottom
        BT, // Bottom to Top
        None // when no action was detected
    }
	
	private static final int MIN_DISTANCE = 300;
    private float downX, downY, upX, upY;
    private Action mSwipeDetected = Action.None;
    private static final String logTag = "SwipeDetector";
    
    public boolean swipeDetected() {
    	return mSwipeDetected != Action.None;
    }
    
    public Action getAction() {
    	return mSwipeDetected;
    }
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = event.getX();
			downY = event.getY();
			mSwipeDetected = Action.None;
			return false;
			
		case MotionEvent.ACTION_MOVE:
			upX = event.getX();
			upY = event.getY();
			
			float deltaX = downX - upX;
			float deltaY = downY - upY;
			
			// horizontal swipe detection
			if (Math.abs(deltaX) > MIN_DISTANCE) {
				// left to right
				if (deltaX < 0) {
					mSwipeDetected = Action.LR;
					Log.d(logTag, "Swipe Left to Right");
				} else {
					mSwipeDetected = Action.RL;
					Log.d(logTag, "Swipe Right to Left");
				}
				
				return true;
			} else if (Math.abs(deltaY) > MIN_DISTANCE) {
				// top to bottom
				if (deltaY < 0) {
					mSwipeDetected = Action.TB;
					Log.d(logTag, "Swipe Top to Bottom");
				} else {
					mSwipeDetected = Action.BT;
					Log.d(logTag, "Swipe Bottom to Top");
				}
				
				return false;
			}
			
			return true;
		}
		
		
		return false;
	}

}
