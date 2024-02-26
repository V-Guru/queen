package com.wozart.aura.utilities.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.wozart.aura.R;

import java.util.Objects;


public class DoubleBtnDialog implements View.OnClickListener {
    private static Dialog dialog;
    private Context context;
    private EditText etAmount;
    private String editError = "";

    public void setEditError(String editError) {
        this.editError = editError;
    }

    private DoubleBtnDialog(Context context) {
        // custom dialog
        if (dialog != null && dialog.isShowing() && !((AppCompatActivity) context).isFinishing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dialog = new Dialog(context, R.style.full_screen_dialog);
        this.context = context;
        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_double_btn);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btn_no).setOnClickListener(this);
//        dialog.findViewById(R.id.ivClose).setOnClickListener(this);
    }

    public DoubleBtnDialog setCancelable(boolean bool) {
        if (dialog != null) {
            dialog.setCancelable(bool);
        }
        return this;
    }

    public DoubleBtnDialog setCancelableOnTouchOutside(boolean bool) {
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(bool);
        }
        return this;
    }


    public static DoubleBtnDialog with(Context context) {
        return new DoubleBtnDialog(context);
    }

    public DoubleBtnDialog setMessage(String msg) {
        if (dialog != null) {
            TextView tvMessage = dialog.findViewById(R.id.tv_message);
            tvMessage.setText(msg);
        }
        return this;
    }


    public DoubleBtnDialog setHeading(String heading) {
        if (dialog != null) {
            TextView tvMessage = dialog.findViewById(R.id.tv_heading);
            tvMessage.setText(heading);
            tvMessage.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public DoubleBtnDialog setDrawableImage(Integer drawableImage) {
        if (dialog != null) {
            TextView tvMessage = dialog.findViewById(R.id.tv_message);
            tvMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(null, ContextCompat.getDrawable(context, drawableImage), null, null);
        }
        return this;
    }

    public DoubleBtnDialog hideHeading() {
        if (dialog != null) {
            TextView tvMessage = dialog.findViewById(R.id.tv_heading);
            tvMessage.setVisibility(View.GONE);
        }
        return this;
    }

    public DoubleBtnDialog setOptionPositive(String optionPositive) {
        if (dialog != null) {
            Button btnYes = dialog.findViewById(R.id.btn_yes);
            btnYes.setText(optionPositive);
            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        return this;
    }

    public DoubleBtnDialog setOptionNegative(String optionNegative) {
        if (dialog != null) {
            TextView btnNo = dialog.findViewById(R.id.btn_no);
            btnNo.setText(optionNegative);
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        return this;
    }

    public DoubleBtnDialog setCallback(final OnActionPerformed onActionPerformed) {
        if (dialog != null && onActionPerformed != null) {
            Button btnYes = dialog.findViewById(R.id.btn_yes);
            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    onActionPerformed.positive();
                }
            });
            TextView btnNo = dialog.findViewById(R.id.btn_no);
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    onActionPerformed.negative();
                }
            });
        }
        return this;
    }

    /**
     * New Callback for Edit Field
     *
     * @param onActionPerformed
     * @return
     */
    public DoubleBtnDialog setNewCallback(final NewOnActionPerformed onActionPerformed) {
        if (dialog != null && onActionPerformed != null) {
            Button btnYes = dialog.findViewById(R.id.btn_yes);
            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (etAmount != null && etAmount.getVisibility() == View.VISIBLE) {
                        if (etAmount.getText().toString().trim().isEmpty()) {
                            etAmount.setError(editError);
                        } else {
                            dialog.dismiss();
                            onActionPerformed.positive(etAmount != null && etAmount.getVisibility() == View.VISIBLE
                                    ? etAmount.getText().toString().trim() : null);
                        }
                    } else {
                        dialog.dismiss();
                        onActionPerformed.positive();
                    }
                }
            });
            TextView btnNo = dialog.findViewById(R.id.btn_no);
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    onActionPerformed.negative();
                }
            });
        }
        return this;
    }


    public void show() {
        try {
            if (dialog != null && !dialog.isShowing() && !((AppCompatActivity) context).isFinishing()) {
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_no || id == R.id.btn_yes || id == R.id.ivClose) {
            dialog.dismiss();
        }
    }

    public interface OnActionPerformed {
        void positive();

        void negative();

    }

    public interface NewOnActionPerformed {
        void positive(@Nullable String... text);

        void negative();
    }
}
