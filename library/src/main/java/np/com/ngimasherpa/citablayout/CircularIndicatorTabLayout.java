package np.com.ngimasherpa.citablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.RestrictTo;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setupWithViewPager(ViewPager)} providing it the ViewPager this layout is being used for.
 * <p>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)}. The
 * alternative is via the {@link TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 * <p>
 * The views used as tabs can be customized by calling {@link #setCustomTabView(int, int)},
 * providing the layout ID of your custom layout.
 */
public class CircularIndicatorTabLayout extends HorizontalScrollView {


    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(TabColorizer)}.
     */
    public interface TabColorizer {

        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

        /**
         * @return return the color of the divider drawn to the right of {@code position}.
         */
        int getDividerColor(int position);

        /**
         * @return return the color of the normal tab text color of {@code position}.
         */
        int getTabViewTextViewColor(int position);

        /**
         * @return return the color of the selected tab text color of {@code position}.
         */
        int getTabViewTextViewSelectedColor(int position);

        /**
         * @return return the color of the line color.
         */
        int getLineColor();

        /**
         * @return return the color of the icon that are not selected.
         */
        int getIconColor(int position);

        /**
         * @return return the color of the selected icon.
         */
        int getSelectedIconColor(int position);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef({
            MODE_FIXED,
            MODE_SCROLLABLE
    })
    public @interface Mode {
    }

    @IntDef({POSITION_TOP, POSITION_BOTTOM})
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public @interface Position {
    }

    public static final int POSITION_TOP = 0;
    public static final int POSITION_BOTTOM = 1;
    public static final int GRAVITY_FILL = 0;
    public static final int GRAVITY_CENTER = 1;

    private static final int TITLE_OFFSET_DIPS = 24;
    private static final int TAB_VIEW_PADDING_DIPS = 16;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;

    private static final int INVALID_WIDTH = -1;
    private static final int MODE_SCROLLABLE = 0;
    private static final int MODE_FIXED = 1;

    private final SlidingTabStrip mTabStrip;

    int mContentInsetStart;
    int mTabPaddingStart;
    int mTabPaddingTop;
    int mTabPaddingEnd;
    int mTabPaddingBottom;

    private int mTitleOffset;
    private int mTabGravity;
    private int mTabViewLayoutId;
    private int mTabViewTextViewId;
    private int mTabViewIconId;
    private int mRequestedTabMinWidth;
    private int mScrollableTabMinWidth;
    private int mCurrentPosition = 0;


    int mPosition;
    private int mMode;
    private Context mContext;
    private ViewPager mViewPager;

    private int mSelectedTabTextColor;
    private int mTabTextColor;
    private int mIconColor;
    private int mSelectedIconColor;

    private SimpleIconManager mIconManager;
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    public CircularIndicatorTabLayout(Context context) {
        this(context, null);
        initialize(context, null);
    }

    public CircularIndicatorTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initialize(context, attrs);
    }

    public CircularIndicatorTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTabStrip = new SlidingTabStrip(context, attrs);
        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);
        // Make sure that the Tab Strips fills this View
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        initialize(context, attrs);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        applyModeAndGravity();
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (bitmap == null) {
            createWindowFrame();
        }
        super.dispatchDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    Bitmap bitmap;

    private void drawTransparentBackground(Canvas canvas) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = getHeight() / 2;

        paint.setAntiAlias(true);
//        paint.setAlpha(99);
//        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.TRANSPARENT);
//        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
//        canvas.drawBitmap(bitmap, rect, rect, paint);
        canvas.drawRect(rectF, paint);
    }

    protected void createWindowFrame() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(bitmap);

        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.TRANSPARENT);
        paint.setAlpha(0);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        osCanvas.drawRect(outerRectangle, paint);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }

    private void initialize(Context context, AttributeSet attributeSet) {
        mContext = context;

        TypedArray a = mContext.obtainStyledAttributes(attributeSet,
                R.styleable.CircularIndicatorTabLayout,
                0, 0);

        mTabViewLayoutId = a.getResourceId(R.styleable.CircularIndicatorTabLayout_tabViewLayoutId, 0);
        mTabViewTextViewId = a.getResourceId(R.styleable.CircularIndicatorTabLayout_tabViewTextViewId,
                0);
        mTabViewIconId = a.getResourceId(R.styleable.CircularIndicatorTabLayout_tabViewIconId, 0);

        mContentInsetStart = a.getDimensionPixelSize(R.styleable.CircularIndicatorTabLayout_contentInsetStart,
                INVALID_WIDTH);

        mTabPaddingStart = mTabPaddingTop = mTabPaddingEnd = mTabPaddingBottom = a
                .getDimensionPixelSize(R.styleable.CircularIndicatorTabLayout_tabPadding, 0);
        mTabPaddingStart = a.getDimensionPixelSize(R.styleable.CircularIndicatorTabLayout_tabPaddingStart,
                mTabPaddingStart);
        mTabPaddingTop = a.getDimensionPixelSize(R.styleable.CircularIndicatorTabLayout_tabPaddingTop,
                mTabPaddingTop);
        mTabPaddingEnd = a.getDimensionPixelSize(R.styleable.CircularIndicatorTabLayout_tabPaddingEnd,
                mTabPaddingEnd);
        mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.CircularIndicatorTabLayout_tabPaddingBottom,
                mTabPaddingBottom);

        mSelectedTabTextColor = a.getColor(R.styleable.CircularIndicatorTabLayout_tabViewTextViewSelectedColor,
                Utils.getAccentColor(getContext()));
        mTabTextColor = a.getColor(R.styleable.CircularIndicatorTabLayout_tabViewTextViewColor,
                Utils.getAccentColor(getContext()));
        mIconColor = a.getColor(R.styleable.CircularIndicatorTabLayout_iconColor,
                Utils.getAccentColor(getContext()));
        mSelectedIconColor = a.getColor(R.styleable.CircularIndicatorTabLayout_selectedIconColor,
                Utils.getAccentColor(getContext()));

        mTabGravity = a.getInt(R.styleable.CircularIndicatorTabLayout_tab_gravity, GRAVITY_CENTER);

        mRequestedTabMinWidth = a.getDimensionPixelSize(R.styleable.CircularIndicatorTabLayout_tabMinWidth,
                INVALID_WIDTH);
//        mMode = a.getInt(R.styleable.SlidingTabLayout_tabMode, MODE_FIXED);
//        mMode = MODE_FIXED; //default
        mMode = a.getInt(R.styleable.CircularIndicatorTabLayout_mode, MODE_FIXED);
        mPosition = a.getInt(R.styleable.CircularIndicatorTabLayout_indicatorPosition, POSITION_TOP);
        mScrollableTabMinWidth = getResources().getDimensionPixelSize(
                android.support.design.R.dimen.design_tab_scrollable_min_width);
    }

    /**
     * Set the custom {@link TabColorizer} to be used.
     * <p>
     * If you only require simple custmisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setDividerColors(int... colors) {
        mTabStrip.setDividerColors(colors);
    }

    /**
     * Set the {@link ViewPager.OnPageChangeListener}. When using {@link CircularIndicatorTabLayout} you are
     * required to set any {@link ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId  id of the {@link TextView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setupWithViewPager(ViewPager viewPager) {
        mTabStrip.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }


    public void setTabGravity(@TabLayout.TabGravity int gravity) {
        if (mTabGravity != gravity) {
            mTabGravity = gravity;
            applyModeAndGravity();
        }
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int)}.
     */
    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // If we're running on Honeycomb or newer, then we can use the Theme's
            // selectableItemBackground to ensure that the View has a pressed state
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                    outValue, true);
            textView.setBackgroundResource(outValue.resourceId);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
            textView.setAllCaps(true);
        }

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);

        return textView;
    }

//    private void se

    private void populateTabStrip() {
        final PagerAdapter adapter = mViewPager.getAdapter();
        final View.OnClickListener tabClickListener = new TabClickListener();
        Log.d("DEBUG", "restore: " + mCurrentPosition);

        for (int i = 0; i < adapter.getCount(); i++) {
            View tabView = null;
            TextView tabTitleView = null;
            ImageView tabIconView = null;

            if (mTabViewLayoutId != 0) {
                // If there is a custom tab view layout id set, try and inflate it
                tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip,
                        false);
                tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);

                tabIconView = (ImageView) tabView.findViewById(mTabViewIconId);

                if (mIconManager != null && tabIconView != null) {
                    tabIconView.setImageDrawable(ContextCompat.getDrawable(getContext(),
                            mIconManager.getIcon(i)));
                }
//                if (mIconManager != null && mTabViewIconId != 0 && tabIconView != null) {
//                    tabIconView.setImageDrawable(ContextCompat.getDrawable(getContext(),
//                            mIconManager.getIcon(i)));
//                }
            }

            if (tabView == null) {
                tabView = createDefaultTabView(getContext());
            }

            if (tabTitleView == null && TextView.class.isInstance(tabView)) {
                tabTitleView = (TextView) tabView;
            }


            tabTitleView.setText(adapter.getPageTitle(i));
//            Log.d("##debug", "populateTabStrip: " + adapter.getPageTitle(i));
            tabView.setOnClickListener(tabClickListener);

//            mTabStrip.addView(tabView);//// TODO: 5/12/17 .... hove to do more work
            tabView.setClickable(true);
            Utils.applyRippleEffect(getContext(), tabView);

            if (i == mCurrentPosition) {
                tabView.setSelected(true);
                tabTitleView.setTextColor(mSelectedTabTextColor);
                if (tabIconView != null) {
                    tabIconView.setImageDrawable(Utils.setupDrawableWithColor(tabIconView.getDrawable(),
                            mSelectedIconColor));
                }
            } else {
                tabTitleView.setTextColor(mTabTextColor);
                if (tabIconView != null) {
                    tabIconView.setImageDrawable(Utils.setupDrawableWithColor(tabIconView.getDrawable(), mIconColor));
                }
            }
            mTabStrip.addView(tabView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        applyModeAndGravity();
    }

    private void applyModeAndGravity() {
        int paddingStart = 0;
        if (mMode == MODE_SCROLLABLE) {
            // If we're scrollable, or fixed at start, inset using padding
            paddingStart = Math.max(0, mContentInsetStart - mTabPaddingStart);
        }
        ViewCompat.setPaddingRelative(mTabStrip, paddingStart, 0, 0, 0);

        switch (mMode) {
            case MODE_FIXED:
                mTabStrip.setGravity(mPosition == POSITION_TOP ? Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL :
                        Gravity.CENTER);
                break;
            case MODE_SCROLLABLE:
                mTabStrip.setGravity(GravityCompat.START | Gravity.CENTER_VERTICAL);
                break;
        }

        updateTabViews(true);
    }

    private void updateTabViews(final boolean requestLayout) {
        for (int i = 0; i < mTabStrip.getChildCount(); i++) {
            View child = mTabStrip.getChildAt(i);
            child.setMinimumWidth(getTabMinWidth());
            updateTabViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
            if (requestLayout) {
                child.requestLayout();
            }
        }
    }

    private void updateTabViewLayoutParams(LinearLayout.LayoutParams lp) {
        if (mMode == MODE_FIXED && mTabGravity == GRAVITY_FILL) {
            lp.width = 0;
            lp.weight = 1;
        } else {
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            lp.weight = 0;
        }
    }

    private int getTabMinWidth() {
        if (mRequestedTabMinWidth != INVALID_WIDTH) {
            // If we have been given a min width, use it
            return mRequestedTabMinWidth;
        }
        // Else, we'll use the default value
        return mMode == MODE_SCROLLABLE ? mScrollableTabMinWidth : 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }


    public void setIcons(@DrawableRes int... icons) {
        if (mIconManager == null) mIconManager = new SimpleIconManager();
        mIconManager.setIcon(icons);
        mTabStrip.removeAllViews();
        populateTabStrip();
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (tabIndex > 0 || positionOffset > 0) {
                // If we're not at the first child and are mid-scroll, make sure we obey the offset
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;
        private int mPreviousScrollState;

        private float tempOffset;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            if (tempOffset > positionOffset) {
                Log.d("MOVE", "onPageScrolled: swipe to left");
            } else {
                Log.d("MOVE", "onPageScrolled: swipe to right");
            }
            tempOffset = positionOffset;

            final boolean updateText = mScrollState != SCROLL_STATE_SETTLING ||
                    mPreviousScrollState == SCROLL_STATE_DRAGGING;

            mTabStrip.onViewPagerPageChanged(position, positionOffset, false);

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            tempOffset = 0;
            mCurrentPosition = position;
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE || mScrollState == SCROLL_STATE_SETTLING) {
                mTabStrip.onViewPagerPageChanged(position, 0f, false);
                scrollToTab(position, 0);
            }

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }

            invalidate();
        }
    }

    private class TabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);
                    mCurrentPosition = i;
                    Log.d("DEBUG", "onClick: " + i);
                    return;
                }
            }
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
        savedState.currentPosition = mCurrentPosition;
        return savedState;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPosition = savedState.currentPosition;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(savedState.childrenStates);
        }
        mTabStrip.removeAllViews();
        populateTabStrip();
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
        private int currentPosition;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
            currentPosition = in.readInt();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray(childrenStates);
            out.writeInt(currentPosition);
        }

        public static final ClassLoaderCreator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
