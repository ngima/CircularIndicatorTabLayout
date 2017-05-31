package np.com.ngimasherpa.citablayout;

import android.support.annotation.DrawableRes;

/**
 * Created by ngima on 5/30/17.
 */

class SimpleIconManager implements IconManager {
    int[] mIcons;

    @Override
    public int getIcon(int position) {
        return mIcons[position % mIcons.length];
    }

    @Override
    public void setIcon(@DrawableRes int... icons) {
        mIcons = icons;
    }
}
