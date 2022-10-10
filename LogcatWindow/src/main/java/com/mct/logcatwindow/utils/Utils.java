package com.mct.logcatwindow.utils;

import android.Manifest;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

public class Utils {

    public static final String LOGCAT_WINDOW_TAG = "Logcat Window";
    private static final Point display;

    static {
        display = new Point();
        display.x = Resources.getSystem().getDisplayMetrics().widthPixels;
        display.y = Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    @NonNull
    public static Point getDisplay() {
        return new Point(display);
    }

    public static int getScreenWidth() {
        return getDisplay().x;
    }

    public static int getScreenHeight() {
        return getDisplay().y;
    }

    public static int dp2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static void requestOverlayPermission(FragmentActivity activity, RequestCallback callback) {
        PermissionX.init(activity)
                .permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
                .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList,
                        "You need to grant the app permission to use this feature.",
                        "OK",
                        "Cancel"))
                .request(callback);
    }

    public static boolean isOverlayGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }


}
