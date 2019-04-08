package com.github.luoyemyy.mall.manager.activity.login

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.github.luoyemyy.config.Profile
import com.github.luoyemyy.ext.md5
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseActivity
import com.github.luoyemyy.mall.manager.activity.main.MainActivity
import com.github.luoyemyy.mall.manager.api.data
import com.github.luoyemyy.mall.manager.api.getUserApi
import com.github.luoyemyy.mall.manager.databinding.ActivityLoginBinding
import com.github.luoyemyy.mall.manager.util.MvpSimplePresenter
import com.github.luoyemyy.mall.manager.util.UserInfo
import com.github.luoyemyy.mvp.getPresenter

class LoginActivity : BaseActivity(), TextWatcher, View.OnClickListener {

    private lateinit var mBinding: ActivityLoginBinding
    private lateinit var mPresenter: Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        mPresenter = getPresenter()

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setTitle(R.string.login)

        mPresenter.setFlagObserver(this, Observer {
            when (it) {
                0 -> enableLogin(true, true)
                1 -> {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
        })

        mBinding.layoutPhone.editText?.addTextChangedListener(this)
        mBinding.layoutPassword.editText?.addTextChangedListener(this)
        mBinding.btnLogin.setOnClickListener(this)

        enableLogin()

        mBinding.txtProfile.setOnClickListener {
            AlertDialog.Builder(this).setItems(arrayOf("dev", "pro")) { _, which ->
                Profile.setType(if (which == 0) Profile.DEV else Profile.PRO)
            }.show()
        }
    }

    private fun enableLogin(force: Boolean = false, enable: Boolean = true) {
        if (force) {
            enable
        } else {
            val (phone, password) = getInput()
            !phone.isNullOrEmpty() && !password.isNullOrEmpty()
        }.also {
            mBinding.btnLogin.isEnabled = it
        }
    }

    override fun afterTextChanged(s: Editable?) {
        enableLogin()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun onClick(v: View?) {
        val (phone, password) = getInput()
        if (phone.isNullOrEmpty()) {
            toast(R.string.phone_hint)
            return
        }
        if (password.isNullOrEmpty()) {
            toast(R.string.password_hint)
            return
        }
        enableLogin(true)
        mPresenter.login(phone, password)
    }

    private fun getInput(): Pair<String?, String?> {
        return Pair(mBinding.layoutPhone.editText?.text?.toString(), mBinding.layoutPassword.editText?.text?.toString())
    }

    class Presenter(var app: Application) : MvpSimplePresenter<Any>(app) {

        fun login(phone: String, password: String) {
            getUserApi().login(phone, password.md5() ?: "").data { ok, value ->
                flag.postValue(0)
                if (ok && value != null) {
                    UserInfo.saveUser(app, value)
                    flag.postValue(1)
                }
            }
        }
    }
}