package com.windnsoft.tbus.Common;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoFitTextView extends TextView {
    private float minTextSize;
    private float maxTextSize;

    public AutoFitTextView(Context context) {
        super(context);
        Init();
    }

    public AutoFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    private void Init() {
        maxTextSize = this.getTextSize();
        if (maxTextSize < 200) {
            maxTextSize = 195;
        }
        minTextSize = 10;
    }

    private void RefitText(String text, int textWidth, int textHeight) {
        if (textWidth > 0 && textHeight > 0) {

            int availableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
            int availableHeight = textHeight - this.getPaddingTop() - this.getPaddingBottom();

            minTextSize = availableHeight / 3;
            maxTextSize = availableHeight;

            float trySize = maxTextSize;
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);

            Rect checkBound = new Rect();
            this.getPaint().getTextBounds(text, 0, text.length(), checkBound);
            int checkHeight = checkBound.height();
            availableHeight = (int) (availableHeight / 2);

            boolean bcheck = (trySize > minTextSize)
                    && ((this.getPaint().measureText(text) > availableWidth)
                    || (checkHeight > availableHeight));

            int modSize = (int) (maxTextSize - minTextSize) / 10;
            if (modSize <= 2)
                modSize = 2;
            while (bcheck) {
                trySize -= modSize;
                if (trySize <= minTextSize) {
                    break;
                }

                this.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
                this.getPaint().getTextBounds(text, 0, text.length(), checkBound);
                checkHeight = checkBound.height();
                bcheck = (trySize >= minTextSize)
                        && ((this.getPaint().measureText(text) > availableWidth)
                        || (checkHeight > availableHeight));
            }
        }
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start,
                                 final int before, final int after) {
        if (text.length() != 0)
            RefitText(text.toString(), this.getWidth(), this.getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            RefitText(this.getText().toString(), w, h);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        RefitText(this.getText().toString(), parentWidth, parentHeight);
    }

    public float MinTextSize() {
        return minTextSize;
    }

    public void MinTextSize(int minTextSize) {
        this.minTextSize = minTextSize;
    }

    public float MaxTextSize() {
        return maxTextSize;
    }

    public void MaxTextSize(int minTextSize) {
        this.maxTextSize = minTextSize;
    }
}