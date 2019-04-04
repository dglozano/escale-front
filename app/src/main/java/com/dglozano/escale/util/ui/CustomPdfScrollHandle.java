package com.dglozano.escale.util.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.ScrollHandle;
import com.github.barteksc.pdfviewer.util.Util;

import timber.log.Timber;

public class CustomPdfScrollHandle extends RelativeLayout implements ScrollHandle {

    private static final int HANDLE_SHORT = 4;

    private float relativeHandlerMiddle = 0f;

    protected Context context;
    private PDFView pdfView;
    private float currentPos;

    private Handler handler = new Handler();
    private Runnable hidePageScrollerRunnable = this::hide;

    public CustomPdfScrollHandle(Context context) {
        super(context);
        this.context = context;
        setVisibility(INVISIBLE);
    }

    @Override
    public void setupLayout(PDFView pdfView) {
        int align, width, height;
        int pageCount = pdfView.getPageCount();
        int pdfViewHeight = pdfView.getMeasuredHeight();
        final int HANDLE_LONG = pdfViewHeight / pageCount / 2;
        Timber.d("Paginas %s - Altura %s - Scroll %s", pageCount, pdfViewHeight, HANDLE_LONG);
        Drawable background = ContextCompat.getDrawable(context, R.drawable.pdf_scroll_vertical);
        if (pdfView.isSwipeVertical()) {
            width = HANDLE_SHORT;
            height = HANDLE_LONG;
            align = ALIGN_PARENT_RIGHT;
        } else {
            width = HANDLE_LONG;
            height = HANDLE_SHORT;
            align = ALIGN_PARENT_BOTTOM;
        }

        setBackground(background);

        LayoutParams lp = new LayoutParams(Util.getDP(context, width), Util.getDP(context, height));
        lp.setMargins(0, 0, 0, 0);
        lp.addRule(align);
        pdfView.addView(this, lp);
        this.pdfView = pdfView;

        if (pageCount <= 1) {
            this.destroyLayout();
        }
    }

    @Override
    public void destroyLayout() {
        pdfView.removeView(this);
    }

    @Override
    public void setScroll(float position) {
        if (!shown()) {
            show();
        } else {
            handler.removeCallbacks(hidePageScrollerRunnable);
        }
        setPosition((pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth()) * position);
    }

    private void setPosition(float pos) {
        if (Float.isInfinite(pos) || Float.isNaN(pos)) {
            return;
        }
        float pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pdfViewSize = pdfView.getHeight();
        } else {
            pdfViewSize = pdfView.getWidth();
        }
        pos -= relativeHandlerMiddle;

        if (pos < 0) {
            pos = 0;
        } else if (pos > pdfViewSize - Util.getDP(context, HANDLE_SHORT)) {
            pos = pdfViewSize - Util.getDP(context, HANDLE_SHORT);
        }

        if (pdfView.isSwipeVertical()) {
            setY(pos);
        } else {
            setX(pos);
        }

        calculateMiddle();
        invalidate();
    }

    private void calculateMiddle() {
        float pos, viewSize, pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pos = getY();
            viewSize = getHeight();
            pdfViewSize = pdfView.getHeight();
        } else {
            pos = getX();
            viewSize = getWidth();
            pdfViewSize = pdfView.getWidth();
        }
        relativeHandlerMiddle = ((pos + relativeHandlerMiddle) / pdfViewSize) * viewSize;
    }

    @Override
    public void hideDelayed() {
        handler.postDelayed(hidePageScrollerRunnable, 1000);
    }

    @Override
    public void setPageNum(int pageNum) {

    }

    @Override
    public boolean shown() {
        return getVisibility() == VISIBLE;
    }

    @Override
    public void show() {
        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        setVisibility(INVISIBLE);
    }

    private boolean isPDFViewReady() {
        return pdfView != null && pdfView.getPageCount() > 0 && !pdfView.documentFitsView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isPDFViewReady()) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                pdfView.stopFling();
                handler.removeCallbacks(hidePageScrollerRunnable);
                if (pdfView.isSwipeVertical()) {
                    currentPos = event.getRawY() - getY();
                } else {
                    currentPos = event.getRawX() - getX();
                }
            case MotionEvent.ACTION_MOVE:
                if (pdfView.isSwipeVertical()) {
                    setPosition(event.getRawY() - currentPos + relativeHandlerMiddle);
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getHeight(), false);
                } else {
                    setPosition(event.getRawX() - currentPos + relativeHandlerMiddle);
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getWidth(), false);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                hideDelayed();
                pdfView.performPageSnap();
                return true;
        }

        return super.onTouchEvent(event);
    }
}