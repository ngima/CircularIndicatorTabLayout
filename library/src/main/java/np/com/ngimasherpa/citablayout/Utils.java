package np.com.ngimasherpa.citablayout;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.RestrictTo;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by ngima on 5/15/17.
 */

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class Utils {

    static int dpToPx(Resources r, int dp) {
        return Math.round(r.getDisplayMetrics().density * dp);
    }

    static Drawable setupDrawableWithColor(Drawable drawable, int colorToBeApplied) {
//        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
//        DrawableCompat.setTintList(wrappedDrawable, new ColorStateList(colorToBeApplied));

//        return wrappedDrawable;
        drawable.mutate().setColorFilter(colorToBeApplied, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    static int getAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    static int getPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    static int getPrimaryDarkColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    static void applyRippleEffect(Context context, View view) {
        if (view == null) return;
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        view.setBackgroundResource(outValue.resourceId);
        view.setClickable(true);
    }
}
