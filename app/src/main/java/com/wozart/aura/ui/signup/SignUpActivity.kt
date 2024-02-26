package com.wozart.aura.aura.ui.signup

import android.content.Intent
import android.os.Bundle
import com.wozart.aura.R
import com.wozart.aura.ui.login.GoogleLoginActivity
import com.wozart.aura.ui.base.projectbase.BaseAbstractActivity
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : BaseAbstractActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        init()
    }
    private fun init(){

        text_login.setOnClickListener {
            val intent=Intent(this, GoogleLoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }
}
