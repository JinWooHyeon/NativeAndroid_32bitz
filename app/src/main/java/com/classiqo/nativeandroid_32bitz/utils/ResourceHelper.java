package com.classiqo.nativeandroid_32bitz.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;

/**
 * Created by JsFish-DT on 2017-03-09.
 */
public class ResourceHelper {
    public static int getThemeColor(Context context, int attribute, int defaultColor) {
        int themeColor = 0;
        String packgeName = context.getPackageName();

        try {
            Context packageContext = context.createPackageContext(packgeName, 0);
            ApplicationInfo applicationInfo =
                    context.getPackageManager().getApplicationInfo(packgeName, 0);
            packageContext.setTheme(applicationInfo.theme);
            Resources.Theme theme = packageContext.getTheme();
            TypedArray ta = theme.obtainStyledAttributes(new int[] {attribute});
            themeColor = ta.getColor(0, defaultColor);
            ta.recycle();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return themeColor;
    }
}
