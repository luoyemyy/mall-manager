package com.github.luoyemyy.mall.manager.activity.user

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
import com.github.luoyemyy.mall.manager.databinding.FragmentUserInfoPasswordBinding
import com.github.luoyemyy.mall.manager.util.LiveValues
import com.github.luoyemyy.mall.manager.util.MvpSimplePresenter
import com.github.luoyemyy.mall.manager.util.enableSubmit
import com.github.luoyemyy.mall.manager.util.setKeyActionDone
import com.github.luoyemyy.mvp.getPresenter

class UserInfoEditPasswordFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentUserInfoPasswordBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserInfoPasswordBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            findNavController().navigateUp()
        })

        mBinding.btnSubmit.setOnClickListener(this)
        mBinding.layoutNewPassword.editText?.apply {
            setKeyActionDone(requireActivity())
            enableSubmit(mBinding.btnSubmit, mBinding.layoutOldPassword.editText)
        }
    }

    private fun getInput(): Pair<String?, String?> {
        return Pair(mBinding.layoutOldPassword.editText?.text?.toString()?.md5(), mBinding.layoutNewPassword.editText?.text?.toString()?.md5())
    }

    override fun onClick(v: View?) {
        val (oldPassword, newPassword) = getInput()
        if (oldPassword.isNullOrEmpty()) {
            requireContext().toast(R.string.user_info_password_old_hint)
            return
        }
        if (newPassword.isNullOrEmpty()) {
            requireContext().toast(R.string.user_info_password_new_hint)
            return
        }
        mPresenter.submit(oldPassword, newPassword)
    }

    class Presenter(var app: Application) : MvpSimplePresenter<String>(app) {
        fun submit(oldPassword: String, newPassword: String) {
            showLoading()
            getUserApi().passwordBySelf(LiveValues.userId, oldPassword, newPassword).result {
                hideLoading()
                if (it) {
                    flag.postValue(1)
                }
            }
        }
    }
}