package com.wozart.aura.utilities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.wozart.aura.R;



public class DialogFactory {

    //Error dialogs
    public static Dialog createSimpleOkErrorDialog(Context context, String title, String message) {
        AlertDialog.Builder alertDialog = (new AlertDialog.Builder(context))
                .setTitle(title).setMessage(message)
                .setNeutralButton(R.string.dialog_action_ok, null);
        return alertDialog.create();
    }

    public static Dialog createSimpleOkErrorDialog(Context context, @StringRes int titleResource, @StringRes int messageResource) {
        return createSimpleOkErrorDialog(context, context.getString(titleResource), context.getString(messageResource));
    }

    public static Dialog createSimpleOkErrorDialog(Context context, String message) {
        AlertDialog.Builder alertDialog = (new AlertDialog.Builder(context))
                .setTitle(context.getString(R.string.dialog_error_title))
                .setMessage(message)
                .setNeutralButton(R.string.dialog_action_ok, null);
        return alertDialog.create();
    }

    public static Dialog createSimpleOkErrorDialog(Context context, @StringRes int messageResource) {
        return createSimpleOkErrorDialog(context, context.getString(messageResource));
    }


    //Progress Dialog
    public static ProgressDialog createProgressDialog(Context context) {
        return createProgressDialog(context,
                context.getString(R.string.app_name), context.getString(R.string.txt_please_wait));
    }

    public static ProgressDialog createProgressDialog(Context context, String message) {
        return createProgressDialog(context,
                context.getString(R.string.app_name), message);

    }

    public static ProgressDialog createProgressDialog(Context context, String title, String message) {
        ProgressDialog progressDialog = ProgressDialog.show(context, title, message);
        progressDialog.setCancelable(false);
        return progressDialog;
    }
}
