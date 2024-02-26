package com.wozart.aura.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.bumptech.glide.Glide

import com.wozart.aura.R
import com.wozart.aura.aura.ui.signup.SignUpActivity
import kotlinx.android.synthetic.main.activity_decision.*

class DecisionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_decision)
        init()
    }

    private fun init() {
        //Remove for GIF
        Glide.with(this).load(R.drawable.login_background_city)
                .into(background);
        btn_submit.setOnClickListener {

            val intent = Intent(this, GoogleLoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }
        text_signup.setOnClickListener {
            //Auth0 function comment the below
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }
}
