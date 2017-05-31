package np.com.ngimasherpa.citablayout;

import android.support.annotation.DrawableRes;

/**
 * Created by ngima on 5/30/17.
 */

interface IconManager {

    @DrawableRes
    int getIcon(int position);

    void setIcon(@DrawableRes int... icons);
}
