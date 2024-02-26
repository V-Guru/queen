package com.wozart.aura.ui.base.projectbase;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.wozart.aura.R;
import com.wozart.aura.utilities.DialogFactory;
import com.wozart.aura.utilities.DialogListener;
import com.wozart.aura.utilities.IntentFactory;
import com.wozart.aura.utilities.ToastFactory;

public abstract class BaseAbstractActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public void showToast() {
        try {
            ToastFactory.createToast(this).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setViewToFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void showToast(String message) {
        ToastFactory.createToast(this, message).show();
    }

    public void showToast(int messageId) {
        try {
            ToastFactory.createToast(this, messageId).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideSoftKeyBoard() {
        final View view = this.getCurrentFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }, 500);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    protected void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void showProgressDialog() {
        progressDialog = DialogFactory.createProgressDialog(this);
    }

    public void showProgressDialog(String message) {
        progressDialog = DialogFactory.createProgressDialog(this, message);
    }

    public void showProgressDialog(int messageId) {
        showProgressDialog(getString(messageId));
    }

    public void showProgressDialog(String title, String message) {
        progressDialog = DialogFactory.createProgressDialog(this, title, message);
    }

    public void showNetworkErrorDialog() {
        DialogFactory.createSimpleOkErrorDialog(this, R.string.dialog_internet_error_title,
                R.string.dialog_internet_error_msg);
    }

    public void startActivity(Class targetClass) {
        Intent intent = IntentFactory.getIntent(this, targetClass);
        startActivity(intent);
    }

    public void startActivity(Class targetClass, Bundle arguments) {
        Intent intent = IntentFactory.getIntent(this, targetClass, arguments);
        startActivity(intent);


    }


    public void showOKCancelDialog(String message, String title, Drawable drawable, final DialogListener listener) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(drawable);
        alertDialog.setPositiveButton(getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (listener != null)
                    listener.onOkClicked();
            }
        });
        alertDialog.setNegativeButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null)
                    listener.onOkClicked();
                dialog.cancel();
            }
        });

        alertDialog.show();
    }


    public void navigateToFragment(Fragment fragment) {
        navigateToFragment(fragment, getString(R.string.empty_tag), false, false);
    }
    public void navigateToFragment(Fragment fragment, String tag) {
        navigateToFragment(fragment, tag, false, false);
    }


    public void navigateToFragment(Fragment fragment, String tag, boolean noHistory) {
        navigateToFragment(fragment, tag, noHistory, false);
    }


    public void navigateToFragment(Fragment fragment, String tag, boolean noHistory, boolean clearStack) {
        FragmentTransaction beginTransaction = getSupportFragmentManager()
            .beginTransaction();

        if (clearStack) {
            getSupportFragmentManager().popBackStack(null, FragmentManager
                .POP_BACK_STACK_INCLUSIVE);
        }

        if (!noHistory) {
            beginTransaction.addToBackStack(tag);
        }
        beginTransaction.replace(R.id.containerAutomation, fragment, tag);
        beginTransaction.commit();
        invalidateOptionsMenu();
    }
}
