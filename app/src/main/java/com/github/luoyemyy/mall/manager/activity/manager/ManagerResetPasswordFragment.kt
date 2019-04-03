package com.github.luoyemyy.mall.manager.activity.manager

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.ext.md5
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.activity.base.hideLoading
import com.github.luoyemyy.mall.manager.activity.base.showLoading
import com.github.luoyemyy.mall.manager.api.getUserApi
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.databinding.FragmentManagerResetPasswordBinding
import com.github.luoyemyy.mall.manager.util.LiveValues
import com.github.luoyemyy.mall.manager.util.MvpSimplePresenter
import com.github.luoyemyy.mall.manager.util.enableSubmit
import com.github.luoyemyy.mall.manager.util.setKeyActionDone
import com.github.luoyemyy.mvp.getPresenter

class ManagerResetPasswordFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentManagerResetPasswordBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentManagerResetPasswordBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            findNavController().navigateUp()
        })

        mBinding.btnSubmit.setOnClickListener(this)
        mBinding.layoutNewPassword.editText?.apply {
            setKeyActionDone(requireActivity())
            enableSubmit(mBinding.btnSubmit)
        }
    }

    private fun getInput(): String? {
        return mBinding.layoutNewPassword.editText?.text?.toString()?.md5()
    }

    override fun onClick(v: View?) {
        val password = getInput()
        if (password.isNullOrEmpty()) {
            requireContext().toast(R.string.admin_reset_password_hint)
            return
        }
        mPresenter.submit(password)
    }

    class Presenter(var app: Application) : MvpSimplePresenter<String>(app) {

        fun submit(newPassword: String) {
            showLoading()
            getUserApi().passwordByAdmin(LiveValues.userId, newPassword).result {
                hideLoading()
                if (it) {
                    flag.postValue(1)
                }
            }
        }
    }
}