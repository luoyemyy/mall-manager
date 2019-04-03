package com.github.luoyemyy.mall.manager.activity.start

import android.content.Intent
import android.os.Bundle
import com.github.luoyemyy.mall.manager.activity.base.BaseActivity
import com.github.luoyemyy.mall.manager.activity.login.LoginActivity
import com.github.luoyemyy.mall.manager.activity.main.MainActivity
import com.github.luoyemyy.mall.manager.util.LiveValues

class StartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (LiveValues.token.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}