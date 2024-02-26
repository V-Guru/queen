package com.wozart.aura.entity.service

import android.app.Activity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics


/**
 * Created by Saif on 07/10/21.
 * mds71964@gmail.com
 */
class FirebaseEvent {
    var firebaseAnalytics: FirebaseAnalytics? = null

    companion object {
        val EVENT_GET_INAPP: String = "Get_InApp_Message"
        var firebaseEventClass: FirebaseEvent? = null
        fun getInstance(): FirebaseEvent {
            if (firebaseEventClass == null) {
                firebaseEventClass =
                        FirebaseEvent()
            }
            return firebaseEventClass!!
        }


        const val EVENT_RATED_INAPP = "Rated_app"
        const val EVENT_SIGNUP = "Sign_up"
        const val PARAM_EMAIL = "email"
        const val PARAM_RATING = "rating"
        const val PARAM_PHONE = "phone"

    }

    fun setAppEventsLogger(appEventsLogger: FirebaseAnalytics): FirebaseEvent {
        this.firebaseAnalytics = appEventsLogger
        return this
    }


    fun logEvent(eventName: String, params: Bundle?) {
        firebaseAnalytics?.logEvent(eventName, params)
    }


    fun logSignup(email: String?, phone: String?) {
        val params = Bundle()
        email?.let { params.putString(PARAM_EMAIL, it) }
        phone?.let { params.putString(PARAM_PHONE, it) }
        logEvent(EVENT_SIGNUP, params)
    }


    fun logAppRated(userId: String?, rating: Int?) {
        val params = Bundle()
        params.putString(
                PARAM_EMAIL, userId)
        params.putInt(PARAM_RATING, rating ?: 1)
        logEvent(
                EVENT_RATED_INAPP, params)
    }


    fun getInAppMessage() {
        val params = Bundle()
        logEvent(
                EVENT_GET_INAPP, params)
    }
}