package com.wellee.butterknife;

import android.app.Activity;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

public class Utils {
    public static <T extends View> T findViewById(@NonNull Activity activity, @IdRes int viewId) {
        return activity.findViewById(viewId);
    }
}
