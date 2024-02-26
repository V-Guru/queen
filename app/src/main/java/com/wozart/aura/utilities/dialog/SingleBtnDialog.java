package com.wozart.aura.utilities.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.wozart.aura.R;

import java.util.Objects;


/**
 * Created by cl-mac-mini-3 on 1/4/17.
 */

public class SingleBtnDialog {

    private static Dialog dialog;
    private Context context;

    public SingleBtnDialog(Context context) {
        this.context = context;
        if (context instanceof AppCompatActivity) {
            if (dialog != null && dialog.isShowing() && !((AppCompatActivity) context).isFinishing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        dialog = new Dialog(context, R.style.full_screen_dialog);
        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.single_btn_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Button btnOk = dialog.findViewById(
                R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    public static SingleBtnDialog with(Context context) {
        return new SingleBtnDialog(context);
    }

    public SingleBtnDialog setMessage(String msg) {
        if (dialog != null) {
            TextView tvMessage = dialog.findViewById(R.id.tv_message);
            tvMessage.setText(msg);
        }
        return this;
    }

    public SingleBtnDialog hideHeading() {
        if (dialog != null) {
            TextView tvMessage = dialog.findViewById(R.id.tv_heading);
            tvMessage.setVisibility(View.GONE);
        }
        return this;
    }

    public SingleBtnDialog setCancelable(boolean bool) {
        if (dialog != null) {
            dialog.setCancelable(bool);
            if (!bool) {
                ImageView ivClose = dialog.findViewById(R.id.ivClose);
                ivClose.setVisibility(View.GONE);
            }
        }
        return this;
    }

    public SingleBtnDialog setDrawableImage(Integer drawableImage) {
        if (dialog != null) {
            TextView tvMessage = dialog.findViewById(R.id.tv_message);
            tvMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(context, drawableImage), null, null);
        }
        return this;
    }


    public SingleBtnDialog setCancelableOnTouchOutside(boolean bool) {
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false);
        }
        return this;
    }

    public SingleBtnDialog setHeading(String heading) {
        if (dialog != null) {
            TextView tvMessage = dialog.findViewById(R.id.tv_heading);
            tvMessage.setText(heading);
            tvMessage.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public SingleBtnDialog setOptionPositive(String optionPositive) {
        if (dialog != null) {
            Button btnOk = dialog.findViewById(R.id.btn_ok);
            btnOk.setText(optionPositive);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        return this;
    }

    public SingleBtnDialog setCallback(final OnActionPerformed onActionPerformed) {
        if (dialog != null && onActionPerformed != null) {
            Button btnOk = dialog.findViewById(R.id.btn_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    onActionPerformed.positive();
                }
            });
        }
        return this;
    }

    public void show() {
        if (context instanceof AppCompatActivity) {
            if (dialog != null && !dialog.isShowing() && !((AppCompatActivity) context).isFinishing()) {
                try {
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface OnActionPerformed {
        void positive();
    }

    private static Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity) cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) cont).getBaseContext());
        return null;
    }
}
