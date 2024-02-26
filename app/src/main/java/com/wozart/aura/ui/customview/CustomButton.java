package com.wozart.aura.ui.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;

import com.wozart.aura.R.styleable;
import com.wozart.aura.R.string;


public class CustomButton extends AppCompatButton {

    public static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";
    String customFont;

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public CustomButton(Context context) {
        super(context);
        this.init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(this.getContext().getAssets(), "font/Roboto-Thin.ttf");
        this.setTypeface(tf, 1);

    }

    public void init(Context context, AttributeSet attrs) {
        int textStyle = attrs.getAttributeIntValue(CustomButton.ANDROID_SCHEMA, "textStyle", Typeface.NORMAL);

        TypedArray a = context.obtainStyledAttributes(attrs,
            styleable.CustomTextView);
        int cf = a.getInteger(styleable.CustomTextView_fontName, 0);
        int fontName = 0;
        switch (textStyle) {
            case Typeface.BOLD:
                fontName = string.Roboto_Bold;
                break;
            case Typeface.ITALIC:
                fontName = string.Roboto_Italic;
                break;
            case 3:
                fontName = string.Roboto_Light;
                break;
            case 4:
                fontName = string.Roboto_Medium;
                break;
            case 5:
                fontName = string.Roboto_Regular;
                break;
            case 6:
                fontName = string.Roboto_Thin;
                break;
            default:
                fontName = string.Roboto_Regular;
                break;
        }

        this.customFont = this.getResources().getString(fontName);

        Typeface tf = Typeface.createFromAsset(context.getAssets(),
            "font/" + this.customFont + ".ttf");
        this.setTypeface(tf);
        a.recycle();
    }
}
