package com.github.luoyemyy.mall.manager.activity.admin

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.ext.md5
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.activity.base.hideLoading
import com.github.luoyemyy.mall.manager.activity.base.showLoading
import com.github.luoyemyy.mall.manager.api.alert
import com.github.luoyemyy.mall.manager.api.getUserApi
import com.github.luoyemyy.mall.manager.databinding.FragmentManagerAddBinding
import com.github.luoyemyy.mall.manager.util.*
import com.github.luoyemyy.mvp.getPresenter

class AdminAddFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentManagerAddBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentManagerAddBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            findNavController().navigateUp()
        })
        mBinding.btnSubmit.setOnClickListener(this)
        mBinding.layout2.editText?.apply {
            setKeyActionDone(requireActivity())
            enableSubmit(mBinding.btnSubmit, mBinding.layout1.editText)
        }
    }

    private fun getInput(): Pair<String?, String?> {
        return Pair(mBinding.layout1.editText?.text?.toString(), mBinding.layout2.editText?.text?.toString()?.md5())
    }

    override fun onClick(v: View?) {
        val (phone, password) = getInput()
        if (phone.isNullOrEmpty()) {
            requireContext().toast(R.string.phone_hint)
            return
        }
        if (password.isNullOrEmpty()) {
            requireContext().toast(R.string.password_hint)
            return
        }
        mPresenter.submit(phone, password)
    }

    class Presenter(var app: Application) : MvpSimplePresenter<String>(app) {

        fun submit(phone: String, password: String) {
            showLoading()
            getUserApi().add(phone, password, Role.ADMIN_ID).alert { ok, alert ->
                hideLoading()
                if (ok) {
                    alert?.apply {
                        app.toast(message = this)
                    }
                    flag.postValue(1)
                    Bus.post(BusEvent.ADMIN_ADD)
                }
            }
        }
    }
}