package com.wozart.aura.utilities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;



public class IntentFactory {

    public static Intent getIntent(Context context, Class targetClass) {
        return new Intent(context, targetClass);
    }

    public static Intent getIntent(Context context, Class targetClass, Bundle arguments) {
        Intent intent =  getIntent(context, targetClass);
        intent.putExtras(arguments);
        return intent;
    }
}
