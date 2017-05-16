package np.com.ngimasherpa.circularindicatortablayout;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by ngima on 5/15/17.
 */

public class Utils {

    public static int dpToPx(Resources r, int dp) {
        return Math.round(r.getDisplayMetrics().density * dp);
    }
}
