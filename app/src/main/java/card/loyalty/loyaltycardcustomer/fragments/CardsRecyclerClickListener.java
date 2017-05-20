package card.loyalty.loyaltycardcustomer.fragments;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Sam on 5/05/2017.
 */

public class CardsRecyclerClickListener extends RecyclerView.SimpleOnItemTouchListener {
    private static final String TAG = "CardsRecyclerClickLis";

    public interface OnCardsRecyclerClickListener {
        void onClick(View view, int position);
    }

    private final OnCardsRecyclerClickListener mListener;
    private final GestureDetectorCompat mGestureDetector;

    public CardsRecyclerClickListener(Context context, final RecyclerView recyclerView, OnCardsRecyclerClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d(TAG, "onSingleTapConfirmed: starts");
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    Log.d(TAG, "onSingleTapConfirmed: calling listener.onClick");
                    mListener.onClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        Log.d(TAG, "onInterceptTouchEvent: starts");
        if (mGestureDetector != null) {
            boolean result = mGestureDetector.onTouchEvent(e);
            Log.d(TAG, "onInterceptTouchEvent: returned " + result);
            return result;
        } else {
            Log.d(TAG, "onInterceptTouchEvent: returned: false");
            return false;
        }
    }
}
