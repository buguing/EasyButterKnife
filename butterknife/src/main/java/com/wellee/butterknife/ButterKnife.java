package com.wellee.butterknife;

import android.app.Activity;

import java.lang.reflect.Constructor;

public class ButterKnife {

    public static Unbinder bind(Activity activity) {
        String className = activity.getClass().getName() + "_ViewBinding";
        try {
            Class<? extends Unbinder> clazz = (Class<? extends Unbinder>) Class.forName(className);
            Constructor<? extends Unbinder> constructor = clazz.getDeclaredConstructor(activity.getClass());
            return constructor.newInstance(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Unbinder.EMPTY;
    }
}
