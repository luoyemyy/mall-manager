package com.github.luoyemyy.mall.manager.activity.hot

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.activity.base.hideLoading
import com.github.luoyemyy.mall.manager.activity.base.showLoading
import com.github.luoyemyy.mall.manager.api.getHotApi
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.Hot
import com.github.luoyemyy.mall.manager.databinding.FragmentHotAoeBinding
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.MvpSimplePresenter
import com.github.luoyemyy.mall.manager.util.pickerImage
import com.github.luoyemyy.mall.manager.util.previewImage
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.recycler.LoadType
import com.github.luoyemyy.mvp.runOnWorker

class HotAoeFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentHotAoeBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentHotAoeBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        getInput()
        mPresenter.submit()
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            findNavController().navigateUp()
        })

        mPresenter.setDataObserver(this, Observer {
            mBinding.entity = it
        })

        mBinding.imgCover.setOnClickListener(this)
        mBinding.imgCoverClear.setOnClickListener(this)
        mBinding.imgCoverPicker.setOnClickListener(this)

        mPresenter.loadInit(arguments)
    }

    private fun getInput() {
        mPresenter.hot.description = mBinding.edtDesc.text.toString()
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.imgCoverClear -> {
                mPresenter.clearLocalImage()
            }
            mBinding.imgCover -> {
                mPresenter.hot.showImage()?.apply {
                    previewImage(v, this)
                }
            }
            mBinding.imgCoverPicker -> {
                getInput()
                pickerImage(requireContext()) {
                    mPresenter.updateImage(it)
                }
            }
        }
    }

    class Presenter(var app: Application) : MvpSimplePresenter<Hot>(app) {

        val hot = Hot()

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            bundle?.apply {
                hot.id = bundle.getLong("hotId", 0)
                hot.description = bundle.getString("description")
                hot.image = bundle.getString("image")
            }
            data.postValue(hot)
        }

        fun clearLocalImage() {
            hot.uploadImage.clear()
            data.postValue(hot)
        }

        fun updateImage(image: String?) {
            hot.uploadImage.localUrl = image
            data.postValue(hot)
        }

        fun submit() {
            val image = hot.showImage()
            val description = hot.description
            if (image.isNullOrEmpty()) {
                app.toast(R.string.hot_image_hint)
                return
            }
            if (description.isNullOrEmpty()) {
                app.toast(R.string.hot_desc_hint)
                return
            }
            showLoading()
            runOnWorker {
                val image2 = hot.submitImage(app, R.string.hot_upload_image_error) ?: let {
                    hideLoading()
                    return@runOnWorker
                }
                if (hot.id > 0) {
                    getHotApi().edit(hot.id, image2, description).result { ok ->
                        hideLoading()
                        if (ok) {
                            flag.postValue(0)
                            Bus.post(BusEvent.HOT_AOE)
                        }
                    }
                } else {
                    getHotApi().add(image2, description).result { ok ->
                        hideLoading()
                        if (ok) {
                            flag.postValue(0)
                            Bus.post(BusEvent.HOT_AOE)
                        }
                    }
                }
            }
        }
    }
}