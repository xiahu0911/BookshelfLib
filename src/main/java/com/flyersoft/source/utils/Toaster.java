package com.flyersoft.source.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class Toaster {

    private static Toast toast;
    private static Toast toastCenter;

    public static void showToast(Context context, String str) {
        try {
            if (toast == null) {
                toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
            }
            toast.setText(str);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showToastCenter(Context context, String str) {

        if (toastCenter != null) {
            toastCenter.cancel();
        }
        toastCenter = Toast.makeText(context, str, Toast.LENGTH_LONG);
        toastCenter.setGravity(Gravity.CENTER, 0, 0);
        toastCenter.setText(str);
        toastCenter.show();
    }
}
