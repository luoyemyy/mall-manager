package com.github.luoyemyy.mall.manager.activity.user

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.databinding.FragmentUserInfoNameBinding
import com.github.luoyemyy.mall.manager.util.*
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.recycler.LoadType

class UserInfoEditNameFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentUserInfoNameBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserInfoNameBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mPresenter = getPresenter()
        mPresenter.setDataObserver(this, Observer {
            mBinding.entity = it
        })

        mBinding.layoutText.editText?.apply {
            setKeyActionDone(requireActivity())
            enableSubmit(mBinding.btnSubmit)
        }
        mBinding.btnSubmit.setOnClickListener(this)

        mPresenter.loadInit()
    }

    private fun getInputName(): String? {
        return mBinding.layoutText.editText?.text?.toString()
    }

    override fun onClick(v: View?) {
        val name = getInputName()
        if (name.isNullOrEmpty()) {
            requireContext().toast(R.string.user_info_name_hint)
            return
        }
        Bus.post(BusEvent.USER_EDIT_NAME, stringValue = name)
        findNavController().navigateUp()
    }

    class Presenter(var app: Application) : MvpSimplePresenter<String>(app) {

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            data.postValue(UserInfo.getUser(app)?.name)
        }

    }
}