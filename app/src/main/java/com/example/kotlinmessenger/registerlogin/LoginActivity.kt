package com.example.kotlinmessenger.registerlogin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.kotlinmessenger.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener {

        }
        text_view_back_to_register.setOnClickListener {
            finish()
        }
    }
}