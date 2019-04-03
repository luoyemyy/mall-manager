package com.github.luoyemyy.mall.manager.activity.category

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
import com.github.luoyemyy.mall.manager.activity.base.hideLoading
import com.github.luoyemyy.mall.manager.activity.base.showLoading
import com.github.luoyemyy.mall.manager.api.getCategoryApi
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.databinding.FragmentCategoryAoeBinding
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.MvpSimplePresenter
import com.github.luoyemyy.mall.manager.util.enableSubmit
import com.github.luoyemyy.mall.manager.util.setKeyActionDone
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.recycler.LoadType

class CategoryAoeFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentCategoryAoeBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentCategoryAoeBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            findNavController().navigateUp()
        })

        mPresenter.setDataObserver(this, Observer {
            mBinding.entity = it
        })

        mBinding.btnSubmit.setOnClickListener(this)
        mBinding.layoutText.editText?.apply {
            setKeyActionDone(requireActivity())
            enableSubmit(mBinding.btnSubmit)
        }

        mPresenter.loadInit(arguments)
    }

    private fun getInput(): String? {
        return mBinding.layoutText.editText?.text?.toString()
    }

    override fun onClick(v: View?) {
        val name = getInput()
        if (name.isNullOrEmpty()) {
            requireContext().toast(R.string.category_name_hint)
            return
        }
        mPresenter.submit(name)
    }

    class Presenter(var app: Application) : MvpSimplePresenter<String>(app) {

        private var id: Long = 0
        private var name: String? = null

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            bundle?.apply {
                id = bundle.getLong("categoryId", 0)
                name = bundle.getString("name")
            }
            data.postValue(name)
        }

        fun submit(name: String) {
            if (id > 0) {
                showLoading()
                getCategoryApi().edit(id, name).result { ok ->
                    hideLoading()
                    if (ok) {
                        flag.postValue(1)
                        Bus.post(BusEvent.CATEGORY_AOE)
                    }
                }
            } else {
                showLoading()
                getCategoryApi().add(name).result { ok ->
                    hideLoading()
                    if (ok) {
                        flag.postValue(1)
                        Bus.post(BusEvent.CATEGORY_AOE)
                    }
                }
            }
        }
    }
}