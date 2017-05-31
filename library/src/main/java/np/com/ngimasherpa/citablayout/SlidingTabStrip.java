package np.com.ngimasherpa.citablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import static np.com.ngimasherpa.citablayout.CircularIndicatorTabLayout.POSITION_BOTTOM;
import static np.com.ngimasherpa.citablayout.CircularIndicatorTabLayout.POSITION_TOP;

class SlidingTabStrip extends LinearLayout {

    private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 2;
    private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26;
    private static final int SELECTED_INDICATOR_THICKNESS_DIPS = 8;
    private static final int SELECTED_INDICATOR_RADIUS = 3;
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

    private final float mDividerHeight;
    private final Paint mDividerPaint;

    private int mPosition;
    private int mSelectedPosition;
    private float mSelectionOffset;
    private int mIndicatorRadius;

    @IdRes
    private int mCustomTextViewId;
    @IdRes
    private int mTabViewIconId;

    private CircularIndicatorTabLayout.TabColorizer mCustomTabColorizer;
    private final SimpleTabColorizer mDefaultTabColorizer;

    private boolean lineEnabled;
    private boolean dividerEnabled;// TODO: 5/29/17 make attr for this as well

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

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularIndicatorTabLayout);
        initialize(typedArray);
    }

    private void initialize(TypedArray a) {

        final int colorAccent = Utils.getAccentColor(getContext());
        final int colorPrimaryDark = Utils.getPrimaryDarkColor(getContext());

        int dividerColor = a.getColor(R.styleable.CircularIndicatorTabLayout_dividerColor,
                colorAccent);

        int indicatorColor = a.getColor(R.styleable.CircularIndicatorTabLayout_indicatorColor,
                colorAccent);

        int lineColor = a.getColor(R.styleable.CircularIndicatorTabLayout_lineColor,
                indicatorColor);

        int tabViewTextViewColor = a.getColor(R.styleable.CircularIndicatorTabLayout_tabViewTextViewColor,
                colorPrimaryDark);

        int tabViewTextViewSelectedColor = a
                .getColor(R.styleable.CircularIndicatorTabLayout_tabViewTextViewSelectedColor, colorAccent);
        int iconColor = a
                .getColor(R.styleable.CircularIndicatorTabLayout_iconColor,
                        colorPrimaryDark);
        int selectedIconColor = a
                .getColor(R.styleable.CircularIndicatorTabLayout_selectedIconColor,
                        colorAccent);

        mPosition = mPreSelection = a.getInt(R.styleable.CircularIndicatorTabLayout_indicatorPosition, POSITION_TOP);


        lineEnabled = a.getBoolean(R.styleable.CircularIndicatorTabLayout_lineEnabled, false);

        mCustomTextViewId = a.getResourceId(R.styleable.CircularIndicatorTabLayout_tabViewTextViewId, 0);
        mTabViewIconId = a.getResourceId(R.styleable.CircularIndicatorTabLayout_tabViewIconId, 0);

        mIndicatorRadius = (int) a.getDimension(R.styleable.CircularIndicatorTabLayout_indicatorRadius, SELECTED_INDICATOR_RADIUS);
        a.recycle();

        mDefaultTabColorizer.setDividerColors(dividerColor);
        mDefaultTabColorizer.setIndicatorColors(indicatorColor);
        mDefaultTabColorizer.setLineColor(lineColor);
        mDefaultTabColorizer.setTabViewTextViewColor(tabViewTextViewColor);
        mDefaultTabColorizer.setTabViewTextViewSelectedColor(tabViewTextViewSelectedColor);
        mDefaultTabColorizer.setIconColors(iconColor);
        mDefaultTabColorizer.setSelectedIconColors(selectedIconColor);
    }

    void setCustomTabColorizer(CircularIndicatorTabLayout.TabColorizer customTabColorizer) {
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

    int mPreSelection;

    void onViewPagerPageChanged(int position, float positionOffset, boolean updateText) {

        mPreSelection = mSelectedPosition;
        mSelectedPosition = position;
        mSelectionOffset = positionOffset;

        invalidate();
    }

    public void updateText(boolean updateText) {
        if (!updateText) return;
        TextView selectedTextView = ((TextView) getChildAt(mSelectedPosition).findViewById(mCustomTextViewId));
        TextView preSelectedTextView = ((TextView) getChildAt(mPreSelection).findViewById(mCustomTextViewId));
        if (selectedTextView == null || preSelectedTextView == null) return;
        selectedTextView.setTextColor(mDefaultTabColorizer.getTabViewTextViewSelectedColor(mSelectedPosition));
        preSelectedTextView.setTextColor(mDefaultTabColorizer.getTabViewTextViewColor(mPreSelection));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        /*final int height = getHeight();
        final int childCount = getChildCount();
        final int dividerHeightPx = (int) (Math.min(Math.max(0f, mDividerHeight), 1f) * height);
        final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
                ? mCustomTabColorizer
                : mDefaultTabColorizer;
        mLinePaint.setColor(mDefaultTabColorizer.getLineColor());
        mSelectedIndicatorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        int childHeight;

        // Thick colored underline below the current selection
        if (childCount > 0) {
            TextView selectedTitle;
            childHeight = getChildAt(mSelectedPosition).getHeight();
            try {
                selectedTitle = (TextView) getChildAt(mSelectedPosition);
            } catch (ClassCastException e) {
                selectedTitle = ((TextView) getChildAt(mSelectedPosition).findViewById(mCustomTextViewId));
            }
            int left = selectedTitle.getLeft();
            int right = selectedTitle.getRight();
            int color = tabColorizer.getIndicatorColor(mSelectedPosition);
            int textSelectedColor = tabColorizer.getTabViewTextViewSelectedColor(mSelectedPosition);
            int textColor = tabColorizer.getTabViewTextViewColor(mSelectedPosition);

            if (mSelectedPosition < (getChildCount() - 1)) {
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
//                View unSelectedTitle = getChildAt(i);
//                ((TextView) getChildAt(i).findViewById(mCustomTextViewId)).setTextColor(textColor);
            }

//            canvas.drawIndicator(left, height - mSelectedIndicatorThickness, right,
//                    height, mSelectedIndicatorPaint);

            int cx = (left + (right - left) / 2);
            int cy = mPosition == POSITION_TOP ?
                    getBottom() - Utils.dpToPx(getResources(), 3) : getTop() + ((getChildAt(0).getTop() - getTop()) / 2);

            if (lineEnabled)
                canvas.drawIndicator(getLeft(), cy, getRight(), cy + 1, mLinePaint);
            canvas.drawCircle(cx, cy, Utils.dpToPx(getResources(), 3), mSelectedIndicatorPaint);
        }*/
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawIndicator(canvas);
        /*final int height = getHeight();
        final int childCount = getChildCount();
        final int dividerHeightPx = (int) (Math.min(Math.max(0f, mDividerHeight), 1f) * height);
        final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
                ? mCustomTabColorizer
                : mDefaultTabColorizer;
        mLinePaint.setColor(mDefaultTabColorizer.getLineColor());
        mSelectedIndicatorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        // Thick colored underline below the current selection
        if (childCount > 0) {
            TextView selectedTitle;
            try {
                selectedTitle = (TextView) getChildAt(mSelectedPosition);
            } catch (ClassCastException e) {
                selectedTitle = ((TextView) getChildAt(mSelectedPosition).findViewById(mCustomTextViewId));
            }
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
//                View unSelectedTitle = getChildAt(i);
//                ((TextView) getChildAt(i).findViewById(mCustomTextViewId)).setTextColor(textColor);
            }

//            canvas.drawIndicator(left, height - mSelectedIndicatorThickness, right,
//                    height, mSelectedIndicatorPaint);

            int cx = (left + (right - left) / 2);
            int cy = mPosition == POSITION_TOP ?
                    getBottom() - Utils.dpToPx(getResources(), 3) : dividerHeightPx / 2;

            if (lineEnabled)
                canvas.drawIndicator(getLeft(), cy, getRight(), cy + 1, mLinePaint);
            canvas.drawCircle(cx, cy, Utils.dpToPx(getResources(), 3), mSelectedIndicatorPaint);
        }*/

//        canvas.drawIndicator(getLeft(), height/2, getResources().getDimensionPixelSize(np.com.ngimasherpa.circularindicatortablayout.R.dimen.spacing), height, mDividerPaint);

        // Thin underline along the entire bottom edge
//        canvas.drawIndicator(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);

        // Vertical separators between the titles
//        int separatorTop = (height - dividerHeightPx) / 2;
//        for (int i = 0; i < childCount - 1; i++) {
//            View child = getChildAt(i);
//            mDividerPaint.setColor(tabColorizer.getDividerColor(i));
//            canvas.drawLine(child.getRight(), separatorTop, child.getRight(),
//                    separatorTop + dividerHeightPx, mDividerPaint);
//        }
    }


    private void drawIndicator(Canvas canvas) {
        final int height = getHeight();
        final int childCount = getChildCount();
        final int dividerHeightPx = (int) (Math.min(Math.max(0f, mDividerHeight), 1f) * height);
        final CircularIndicatorTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
                ? mCustomTabColorizer
                : mDefaultTabColorizer;

        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition);
            int left = selectedTitle.getLeft();
            int right = selectedTitle.getRight();
            int color = tabColorizer.getIndicatorColor(mSelectedPosition);
//            int color = tabColorizer.getTabViewTextViewSelectedColor(mSelectedPosition);

            if (mSelectedPosition < (getChildCount()-1)) {
                int nextColor = tabColorizer.getIndicatorColor(mSelectedPosition + 1);
                if (color != nextColor) {
                    color = blendColors(nextColor, color, mSelectionOffset);
                }
                int nextTextColor = blendColors(tabColorizer.getTabViewTextViewSelectedColor(mSelectedPosition),
                        tabColorizer.getTabViewTextViewColor(mPreSelection), mSelectionOffset);
                int textColor = blendColors(tabColorizer.getTabViewTextViewColor(mPreSelection),
                        tabColorizer.getTabViewTextViewSelectedColor(mSelectedPosition), mSelectionOffset);

                int nextIconColor = blendColors(tabColorizer.getSelectedIconColor(mSelectedPosition),
                        tabColorizer.getIconColor(mPreSelection), mSelectionOffset);
                int iconColor = blendColors(tabColorizer.getIconColor(mPreSelection),
                        tabColorizer.getSelectedIconColor(mSelectedPosition), mSelectionOffset);

                View nextTab = getChildAt(mSelectedPosition + 1);
                TextView currentTextView;
                TextView nextTextView;
                if (mCustomTextViewId != 0) {
                    currentTextView = (TextView) getChildAt(mSelectedPosition).findViewById(mCustomTextViewId);
                    nextTextView = (TextView) nextTab.findViewById(mCustomTextViewId);
                } else {
                    currentTextView = (TextView) getChildAt(mSelectedPosition);
                    nextTextView = (TextView) nextTab;
                }
                // Draw the selection partway between the tabs

                ImageView currentIcon = (ImageView) getChildAt(mSelectedPosition).findViewById(mTabViewIconId);
                ImageView nextIcon = (ImageView) nextTab.findViewById(mTabViewIconId);
                if (mPreSelection != mSelectedPosition) {
                    TextView textView;
                    ImageView iconView;
                    if (null != (textView = (TextView) getChildAt(mPreSelection).findViewById(mCustomTextViewId))) {
                        textView.setTextColor(tabColorizer.getTabViewTextViewColor(mPreSelection));
                    }
                    if (null != (iconView = (ImageView) getChildAt(mPreSelection).findViewById(mTabViewIconId))) {
                        iconView.setImageDrawable(Utils.setupDrawableWithColor(iconView.getDrawable(),
                                tabColorizer.getIconColor(mPreSelection)));
                    }
                }
                if (currentTextView != null) currentTextView.setTextColor(textColor);
                if (nextTextView != null) nextTextView.setTextColor(nextTextColor);
                if (currentIcon != null) {
                    currentIcon.setImageDrawable(Utils.setupDrawableWithColor(currentIcon.getDrawable(),
                            iconColor));
                }
                if (nextIcon != null)
                    nextIcon.setImageDrawable(Utils.setupDrawableWithColor(nextIcon.getDrawable(),
                            nextIconColor));

                left = (int) (mSelectionOffset * nextTab.getLeft() +
                        (1.0f - mSelectionOffset) * left);
                right = (int) (mSelectionOffset * nextTab.getRight() +
                        (1.0f - mSelectionOffset) * right);
            }

            mSelectedIndicatorPaint.setColor(color);
            mSelectedIndicatorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

            mLinePaint.setColor(mDefaultTabColorizer.getLineColor());
            mSelectedIndicatorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
//
//            canvas.drawIndicator(left, height - mSelectedIndicatorThickness, right,
//                    height, mSelectedIndicatorPaint);
            int cx = (left + (right - left) / 2);
            int cy = mPosition == POSITION_BOTTOM ?
                    getBottom() - 2 * Utils.dpToPx(getResources(), mIndicatorRadius) : getTop() +
                    2 * Utils.dpToPx(getResources(), mIndicatorRadius);

            if (lineEnabled)
                canvas.drawRect(getLeft(), cy, getRight(), cy + 1, mLinePaint);
            canvas.drawCircle(cx, cy, Utils.dpToPx(getResources(), 3), mSelectedIndicatorPaint);
        }


        // Thin underline along the entire bottom edge
//        canvas.drawIndicator(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);

        // Vertical separators between the titles
        if (!dividerEnabled) return;
        int separatorTop = (height - dividerHeightPx) / 2;
        for (int i = 0; i < childCount - 1; i++) {
            View child = getChildAt(i);
            mDividerPaint.setColor(tabColorizer.getDividerColor(i));
            canvas.drawLine(child.getRight(), separatorTop, child.getRight(),
                    separatorTop + dividerHeightPx, mDividerPaint);
        }
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

    private static class SimpleTabColorizer implements CircularIndicatorTabLayout.TabColorizer {
        private int[] mIndicatorColors;
        private int[] mDividerColors;
        private int[] mTabViewTextViewColors;
        private int[] mTabViewTextViewSelectedColors;
        private int[] mIconColors;
        private int[] mSelectedIconColors;
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
            return mTabViewTextViewColors[position % mTabViewTextViewColors.length];
        }

        @Override
        public int getTabViewTextViewSelectedColor(int position) {
            return mTabViewTextViewSelectedColors[position % mTabViewTextViewSelectedColors.length];
        }

        @Override
        public int getLineColor() {
            return mLineColor;
        }

        @Override
        public int getIconColor(int position) {
            return mIconColors[position % mIconColors.length];
        }

        @Override
        public int getSelectedIconColor(int position) {
            return mSelectedIconColors[position % mSelectedIconColors.length];
        }

        void setIndicatorColors(int... colors) {
            mIndicatorColors = colors;
        }

        void setDividerColors(int... colors) {
            mDividerColors = colors;
        }

        void setTabViewTextViewColor(int... tabViewTextViewColor) {
            this.mTabViewTextViewColors = tabViewTextViewColor;
        }

        void setTabViewTextViewSelectedColor(int... tabViewTextViewSelectedColor) {
            this.mTabViewTextViewSelectedColors = tabViewTextViewSelectedColor;
        }

        void setLineColor(int lineColor) {
            this.mLineColor = lineColor;
        }

        public void setIconColors(int... iconColors) {
            this.mIconColors = iconColors;
        }

        public void setSelectedIconColors(int... selectedIconColors) {
            this.mSelectedIconColors = selectedIconColors;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable saveInstanceState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(saveInstanceState);
        savedState.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(savedState.childrenStates);
        }
//        savedState.selection = mSelectedPosition;
//        savedState.preSelection = mPreSelection;
        return savedState;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(savedState.childrenStates);
//            getChildAt(i).restoreHierarchyState(savedState.selection);
        }
//        mSelectedPosition = savedState.selection;
//        mPreSelection = savedState.preSelection;
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    private static class SavedState extends BaseSavedState {
        private SparseArray childrenStates;
//        private int selection;
//        private int preSelection;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
//            selection = in.readInt();
//            preSelection = in.readInt();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray(childrenStates);
//            out.writeInt(selection);
//            out.writeInt(preSelection);
        }

        public static final ClassLoaderCreator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}