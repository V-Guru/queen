package com.wozart.aura.utilities;

import android.content.Context;
import android.widget.Toast;

import com.wozart.aura.R;


public class ToastFactory {

    public static Toast createToast(Context context, String message) {
        return Toast.makeText(context, message, Toast.LENGTH_LONG);
    }

    public static Toast createToast(Context context, int messageId) throws Exception {
        try {
            return ToastFactory.createToast(context, context.getString(messageId));
        } catch (Exception ex) {
            throw new Exception("Android Resource not found exception");
        }
    }

    public static Toast createToast(Context context) throws Exception {
        return ToastFactory.createToast(context, R.string.txt_please_wait);
    }
}
