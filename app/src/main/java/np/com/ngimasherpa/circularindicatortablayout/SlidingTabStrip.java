package np.com.ngimasherpa.circularindicatortablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import np.com.ngimasherpa.circularindicatortablayout.R;

import static np.com.ngimasherpa.circularindicatortablayout.SlidingTabLayout.POSITION_TOP;

class SlidingTabStrip extends LinearLayout {

    private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 2;
    private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26;
    private static final int SELECTED_INDICATOR_THICKNESS_DIPS = 8;
    private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5;

    private static final int DEFAULT_DIVIDER_THICKNESS_DIPS = 1;
    private static final byte DEFAULT_DIVIDER_COLOR_ALPHA = 0x20;
    private static final float DEFAULT_DIVIDER_HEIGHT = 0.5f;

    private final int mBottomBorderThickness;
    private final Paint mBottomBorderPaint;

    private final int mSelectedIndicatorThickness;
    private final Paint mSelectedIndicatorPaint;
    private final Paint mLinePaint;

    private final int mDefaultBottomBorderColor;

    private final Paint mDividerPaint;
    private final float mDividerHeight;

    private int mPosition;
    private int mSelectedPosition;
    private float mSelectionOffset;
    private int mIndicatorRadius;

    private SlidingTabLayout.TabColorizer mCustomTabColorizer;
    private final SimpleTabColorizer mDefaultTabColorizer;

    private boolean isLineEnabled;

    SlidingTabStrip(Context context) {
        this(context, null);
    }

    SlidingTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorForeground, outValue, true);
        final int themeForegroundColor = outValue.data;

        mDefaultBottomBorderColor = setColorAlpha(themeForegroundColor,
                DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);

        mDefaultTabColorizer = new SimpleTabColorizer();
        mDefaultTabColorizer.setDividerColors(setColorAlpha(themeForegroundColor,
                DEFAULT_DIVIDER_COLOR_ALPHA));

        mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density);
        mBottomBorderPaint = new Paint();
        mBottomBorderPaint.setColor(mDefaultBottomBorderColor);

        mSelectedIndicatorThickness = (int) (SELECTED_INDICATOR_THICKNESS_DIPS * density);
        mSelectedIndicatorPaint = new Paint();
        mLinePaint = new Paint();


        mDividerHeight = DEFAULT_DIVIDER_HEIGHT;
        mDividerPaint = new Paint();
        mDividerPaint.setStrokeWidth((int) (DEFAULT_DIVIDER_THICKNESS_DIPS * density));

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);
        initialize(typedArray);
    }

    private void initialize(TypedArray a) {

        int dividerColor = a.getColor(R.styleable.SlidingTabLayout_dividerColor,
                DEFAULT_SELECTED_INDICATOR_COLOR);

        int indicatorColor = a.getColor(R.styleable.SlidingTabLayout_indicatorColor,
                DEFAULT_SELECTED_INDICATOR_COLOR);

        int lineColor = a.getColor(R.styleable.SlidingTabLayout_lineColor,
                indicatorColor);

        int tabViewTextViewColor = a.getColor(R.styleable.SlidingTabLayout_tabViewTextViewColor,
                DEFAULT_SELECTED_INDICATOR_COLOR);

        int tabViewTextViewSelectedColor = a
                .getColor(R.styleable.SlidingTabLayout_tabViewTextViewSelectedColor,
                        DEFAULT_SELECTED_INDICATOR_COLOR);

        mPosition = a.getInt(R.styleable.SlidingTabLayout_position, POSITION_TOP);


        isLineEnabled = a.getBoolean(R.styleable.SlidingTabLayout_lineEnabled, false);

        a.recycle();

        mDefaultTabColorizer.setDividerColors(dividerColor);
        mDefaultTabColorizer.setIndicatorColors(indicatorColor);
        mDefaultTabColorizer.setLineColor(lineColor);
        mDefaultTabColorizer.setTabViewTextViewColor(tabViewTextViewColor);
        mDefaultTabColorizer.setTabViewTextViewSelectedColor(tabViewTextViewSelectedColor);
    }

    void setCustomTabColorizer(SlidingTabLayout.TabColorizer customTabColorizer) {
        mCustomTabColorizer = customTabColorizer;
        invalidate();
    }

    void setSelectedIndicatorColors(int... colors) {
        // Make sure that the custom colorizer is removed
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setIndicatorColors(colors);
        invalidate();
    }

    void setDividerColors(int... colors) {
        // Make sure that the custom colorizer is removed
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setDividerColors(colors);
        invalidate();
    }

    void setTextColor(int... colors) {
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setTabViewTextViewColor(colors);
    }

    void setSelectedTextColor(int... colors) {
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setTabViewTextViewSelectedColor(colors);
    }

    void onViewPagerPageChanged(int position, float positionOffset) {
        mSelectedPosition = position;
        mSelectionOffset = positionOffset;
        Log.e("##debug", "onViewPagerPageChanged: " + positionOffset);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight();
        final int childCount = getChildCount();
        final int dividerHeightPx = (int) (Math.min(Math.max(0f, mDividerHeight), 1f) * height);
        final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
                ? mCustomTabColorizer
                : mDefaultTabColorizer;
        mLinePaint.setColor(mDefaultTabColorizer.getLineColor());

        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition);
            int left = selectedTitle.getLeft();
            int right = selectedTitle.getRight();
            int color = tabColorizer.getIndicatorColor(mSelectedPosition);
            int textSelectedColor = tabColorizer.getTabViewTextViewSelectedColor(mSelectedPosition);
            int textColor = tabColorizer.getTabViewTextViewColor(mSelectedPosition);

            if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {
                int nextColor = tabColorizer.getIndicatorColor(mSelectedPosition + 1);
                int nextTextColor = tabColorizer.getTabViewTextViewSelectedColor(mSelectedPosition + 1);
                if (color != nextColor) {
                    color = blendColors(nextColor, color, mSelectionOffset);
                    textSelectedColor = blendColors(nextTextColor, textSelectedColor, mSelectionOffset);
                }

                // Draw the selection partway between the tabs
                View nextTitle = getChildAt(mSelectedPosition + 1);
                left = (int) (mSelectionOffset * nextTitle.getLeft() +
                        (1.0f - mSelectionOffset) * left);
                right = (int) (mSelectionOffset * nextTitle.getRight() +
                        (1.0f - mSelectionOffset) * right);
            }

            mSelectedIndicatorPaint.setColor(color);

            for (int i = 0; i < childCount; i++) {
                if (i == mSelectedPosition) {
                    ((TextView) selectedTitle).setTextColor(textSelectedColor);
                    continue;
                }
                View unSelectedTitle = getChildAt(i);
                ((TextView) unSelectedTitle).setTextColor(textColor);

            }

//            canvas.drawRect(left, height - mSelectedIndicatorThickness, right,
//                    height, mSelectedIndicatorPaint);

            int cx = (left + (right - left) / 2);
            int cy = mPosition == POSITION_TOP ? getBottom() - getResources()
                    .getDimensionPixelSize(R.dimen.radius_indicator) : dividerHeightPx / 2;

            if (isLineEnabled)
                canvas.drawRect(getLeft(), dividerHeightPx / 2, getRight(), dividerHeightPx / 2 + 1, mLinePaint);
            canvas.drawCircle(cx, cy, getResources().getDimensionPixelSize(
                    R.dimen.radius_indicator), mSelectedIndicatorPaint);
        }

//        canvas.drawRect(getLeft(), height/2, getResources().getDimensionPixelSize(np.com.ngimasherpa.circularindicatortablayout.R.dimen.spacing), height, mDividerPaint);

        // Thin underline along the entire bottom edge
//        canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);

        // Vertical separators between the titles
//        int separatorTop = (height - dividerHeightPx) / 2;
//        for (int i = 0; i < childCount - 1; i++) {
//            View child = getChildAt(i);
//            mDividerPaint.setColor(tabColorizer.getDividerColor(i));
//            canvas.drawLine(child.getRight(), separatorTop, child.getRight(),
//                    separatorTop + dividerHeightPx, mDividerPaint);
//        }
    }

    /**
     * Set the alpha value of the {@code color} to be the given {@code alpha} value.
     */
    private static int setColorAlpha(int color, byte alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 1.0 will return {@code color1}, 0.5 will give an even blend,
     *              0.0 will return {@code color2}.
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    private static class SimpleTabColorizer implements SlidingTabLayout.TabColorizer {
        private int[] mIndicatorColors;
        private int[] mDividerColors;
        private int[] mTabViewTextViewColor;
        private int[] mTabViewTextViewSelectedColor;
        private int mLineColor;

        @Override
        public final int getIndicatorColor(int position) {
            return mIndicatorColors[position % mIndicatorColors.length];
        }

        @Override
        public final int getDividerColor(int position) {
            return mDividerColors[position % mDividerColors.length];
        }

        @Override
        public int getTabViewTextViewColor(int position) {
            return mTabViewTextViewColor[position % mTabViewTextViewColor.length];
        }

        @Override
        public int getTabViewTextViewSelectedColor(int position) {
            return mTabViewTextViewSelectedColor[position % mTabViewTextViewSelectedColor.length];
        }

        @Override
        public int getLineColor() {
            return mLineColor;
        }

        void setIndicatorColors(int... colors) {
            mIndicatorColors = colors;
        }

        void setDividerColors(int... colors) {
            mDividerColors = colors;
        }

        void setTabViewTextViewColor(int... tabViewTextViewColor) {
            this.mTabViewTextViewColor = tabViewTextViewColor;
        }

        void setTabViewTextViewSelectedColor(int... tabViewTextViewSelectedColor) {
            this.mTabViewTextViewSelectedColor = tabViewTextViewSelectedColor;
        }

        public void setLineColor(int lineColor) {
            this.mLineColor = lineColor;
        }
    }
}