package com.github.luoyemyy.mall.manager.activity.user

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.bus.BusResult
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.activity.base.hideLoading
import com.github.luoyemyy.mall.manager.activity.base.showLoading
import com.github.luoyemyy.mall.manager.api.getUserApi
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.User
import com.github.luoyemyy.mall.manager.databinding.FragmentUserInfoBinding
import com.github.luoyemyy.mall.manager.util.*
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.recycler.LoadType
import com.github.luoyemyy.picker.ImagePicker
import java.io.File

class UserInfoFragment : BaseFragment(), View.OnClickListener, BusResult {

    private lateinit var mBinding: FragmentUserInfoBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserInfoBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mPresenter = getPresenter()
        mPresenter.setDataObserver(this, Observer {
            mBinding.entity = it
        })
        mPresenter.setFlagObserver(this, Observer {
            mPresenter.loadRefresh()
        })

        mBinding.layoutHeadImage.setOnClickListener(this)
        mBinding.layoutName.setOnClickListener(this)
        mBinding.layoutGender.setOnClickListener(this)
        mBinding.layoutPassword.setOnClickListener(this)

        Bus.addCallback(lifecycle, this, BusEvent.USER_EDIT_NAME)

        mPresenter.loadInit()
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.layoutHeadImage -> {
                ImagePicker.create(requireContext().packageName)
                    .cropByPercent()
                    .compress(100)
                    .build()
                    .picker(this) {
                        if (!it.isNullOrEmpty()) {
                            mPresenter.updateHeadImage(it[0])
                        }
                    }
            }
            mBinding.layoutName -> {
                findNavController().navigate(R.id.action_userInfoFragment_to_userInfoEditNameFragment)
            }
            mBinding.layoutGender -> {
                AlertDialog.Builder(requireContext()).setItems(R.array.gender) { _, which ->
                    mPresenter.updateGender(which + 1)
                }.show()
            }
            mBinding.layoutPassword -> {
                findNavController().navigate(R.id.action_userInfoFragment_to_userInfoEditPasswordFragment)
            }
        }
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.USER_EDIT_NAME -> {
                msg.stringValue?.apply {
                    mPresenter.updateName(this)
                }
            }
        }
    }

    class Presenter(var app: Application) : MvpSimplePresenter<User>(app) {

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            data.postValue(UserInfo.getUser(app))
        }

        fun updateHeadImage(headImage: String) {
            showLoading()
            val name = File(headImage).name
            Oss.getInstance().asyncPutObject(PutObjectRequest(UserInfo.getOssBucket(app), name, headImage),
                object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                    override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                        updateUser(Oss.url(name), null, -1) {
                            UserInfo.updateHeadImage(app, headImage)
                        }
                        Log.e("Presenter", "onSuccess:  ${result?.eTag}")
                        Log.e("Presenter", "onSuccess:  ${result?.serverCallbackReturnBody}")
                    }

                    override fun onFailure(
                        request: PutObjectRequest?,
                        clientException: ClientException?,
                        serviceException: ServiceException?
                    ) {
                        hideLoading()
                    }
                })
        }

        fun updateName(name: String) {
            updateUser(null, name, -1) {
                UserInfo.updateName(app, name)
            }
        }

        fun updateGender(gender: Int) {
            updateUser(null, null, gender) {
                UserInfo.updateGender(app, gender)
            }
        }

        private fun updateUser(headImage: String?, name: String?, gender: Int = -1, result: () -> Unit) {
            showLoading()
            getUserApi().editInfo(LiveValues.userId, name, gender, headImage).result {
                hideLoading()
                if (it) {
                    result()
                    flag.postValue(1)
                }
            }
        }
    }
}