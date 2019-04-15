package com.github.luoyemyy.mall.manager.activity.product

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.activity.base.hideLoading
import com.github.luoyemyy.mall.manager.activity.base.showLoading
import com.github.luoyemyy.mall.manager.api.getProductApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.ProductImage
import com.github.luoyemyy.mall.manager.databinding.FragmentProductAoeImageBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentProductTemplateBinding
import com.github.luoyemyy.mall.manager.util.*
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*
import com.github.luoyemyy.mvp.runOnWorker

class ProductTemplate3Fragment : BaseFragment() {

    private lateinit var mBinding: FragmentProductTemplateBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mSortHelper: ItemTouchHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProductTemplateBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        mPresenter.save()
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mPresenter.setFlagObserver(this, Observer {
            when (it) {
                0 -> findNavController().navigateUp()
            }
        })

        mSortHelper = ItemTouchHelper(object : SortCallback() {
            override fun move(source: Int, target: Int): Boolean = mPresenter.move(source, target)
        }).apply {
            attachToRecyclerView(mBinding.recyclerView)
        }

        mBinding.apply {
            recyclerView.apply {
                setGridManager(3)
                setHasFixedSize(true)
                addItemDecoration(GridDecoration.create(requireContext(), 3, 1))
            }
        }

        mPresenter.loadInit(arguments)
    }

    inner class Adapter : MvpSingleAdapter<ProductImage, FragmentProductAoeImageBinding>(mBinding.recyclerView) {
        override fun getLayoutId(): Int {
            return R.layout.fragment_product_aoe_image
        }

        override fun getItemClickViews(binding: FragmentProductAoeImageBinding): Array<View> {
            return arrayOf(binding.imgPreview, binding.imgPicker, binding.imgClear)
        }

        override fun onItemClickListener(vh: VH<FragmentProductAoeImageBinding>, view: View?) {
            if (view == null) return
            val binding = vh.binding ?: return
            when (view) {
                binding.imgClear -> mPresenter.delete(vh.adapterPosition)
                binding.imgPreview -> preview(vh.adapterPosition, view)
                binding.imgPicker -> picker()
            }
        }

        private fun preview(position: Int, view: View) {
            val image = mPresenter.getDataSet().item(position)?.image() ?: return
            previewImage(view, image)
        }

        private fun picker() {
            pickerDescImages(requireContext()) {
                mPresenter.add(it)
            }
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun enableEmpty(): Boolean {
            return false
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bindItemEvents(vh: VH<FragmentProductAoeImageBinding>) {
            vh.binding?.imgPreview?.setOnTouchListener { _, _ ->
                mSortHelper.startDrag(vh).let { true }
            }
        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<ProductImage>(app) {

        fun delete(position: Int) {
            getAdapterSupport()?.apply {
                getDataSet().remove(intArrayOf(position), getAdapter())
            }
        }

        fun add(list: List<String>?) {
            if (!list.isNullOrEmpty()) {
                getAdapterSupport()?.apply {
                    list.map { ProductImage(3,it) }.apply {
                        val last = getDataSet().dataList().lastOrNull { it.isImage() }
                        getDataSet().addDataAfter(last, this, getAdapter())
                    }
                }
            }
        }

        override fun loadData(
            loadType: LoadType,
            paging: Paging,
            bundle: Bundle?,
            search: String?,
            afterLoad: (Boolean, List<ProductImage>?) -> Unit
        ): Boolean {
            getProductApi().template(3).list { ok, value ->
                val list = mutableListOf<ProductImage>()
                if (ok) {
                    value?.apply {
                        value.forEach {
                            it.type = 3
                        }
                        list += value
                    }
                }
                list += ProductImage(0)
                afterLoad(true, list)
            }
            return true
        }

        fun move(source: Int, target: Int): Boolean {
            if (source == target) return false
            if (getDataSet().item(target)?.isImage() == true) {
                getAdapterSupport()?.apply {
                    getDataSet().move(source, target, getAdapter())
                    return true
                }
            }
            return false
        }

        fun save() {
            showLoading()
            runOnWorker {
                val list = getDataSet().dataList().filter { it.isImage() }
                if (list.isEmpty()) {
                    hideLoading()
                    flag.postValue(0)
                    return@runOnWorker
                }
                list.forEachIndexed { index, image ->
                    if (!image.tryUpload(app,app.getString(R.string.product_template3_upload_error, index + 1))){
                        hideLoading()
                        return@runOnWorker
                    }
                }
                getProductApi().templateAoe(list).result {
                    hideLoading()
                    if (it) {
                        flag.postValue(0)
                        Bus.post(BusEvent.CLEAR_IMAGE_CACHE)
                    }
                }
            }
        }
    }
}