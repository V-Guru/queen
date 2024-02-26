package com.wozart.aura

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate




class App : Application(){

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

    }
}