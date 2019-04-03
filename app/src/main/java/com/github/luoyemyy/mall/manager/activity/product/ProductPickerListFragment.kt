package com.github.luoyemyy.mall.manager.activity.product

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.api.getProductApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.bean.Product
import com.github.luoyemyy.mall.manager.databinding.FragmentProductListBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentProductListRecyclerBinding
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.MvpRecyclerPresenter
import com.github.luoyemyy.mall.manager.util.MvpSingleAdapter
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class ProductPickerListFragment : BaseFragment() {

    private lateinit var mBinding: FragmentProductListBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun fromCategory(categoryId: Long): ProductPickerListFragment {
            return ProductPickerListFragment().apply {
                arguments = bundleOf("categoryId" to categoryId)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProductListBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(LinearDecoration.beginMiddle(requireContext()))
            setHasFixedSize(true)
        }
        mBinding.swipeRefreshLayout.setOnRefreshListener { mPresenter.loadRefresh() }

        Bus.addCallback(lifecycle, this, BusEvent.PRODUCT_PICKER_SELECT)

        mPresenter.loadInit(arguments)
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.PRODUCT_PICKER_SELECT -> {
                mPresenter.selectedResult(msg.longValue, msg.boolValue)
            }
        }
    }

    inner class Adapter : MvpSingleAdapter<Product, FragmentProductListRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int {
            return R.layout.fragment_product_picker_recycler
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun onItemClickListener(vh: VH<FragmentProductListRecyclerBinding>, view: View?) {
            mPresenter.selected(vh.adapterPosition)
        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<Product>(app) {

        private var categoryId: Long = -1

        private val mSelectIds = mutableSetOf<Long>()

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?, afterLoad: (Boolean, List<Product>?) -> Unit): Boolean {
            if (loadType.isInit()) {
                categoryId = bundle?.getLong("categoryId") ?: -1L
            }
            if (categoryId == -1L) {
                afterLoad(false, null)
                return true
            }
            getProductApi().list(categoryId, paging.current().toInt()).list { ok, value ->
                value?.forEach {
                    if (mSelectIds.contains(it.id)) {
                        it.selected = true
                    }
                }
                afterLoad(ok, value)
            }
            return true
        }

        fun selectedResult(id: Long, selected: Boolean) {
            if (selected) {
                mSelectIds.add(id)
            } else {
                mSelectIds.remove(id)
            }
            getAdapterSupport()?.apply {
                getDataSet().apply {
                    dataList().firstOrNull { it.id == id }?.also {
                        it.selected = selected
                        change(it, getAdapter())
                    }
                }
            }
        }

        fun selected(position: Int) {
            val product = getDataSet().item(position) ?: return
            Bus.post(BusEvent.PRODUCT_PICKER_SELECT, longValue = product.id, boolValue = !product.selected)
        }
    }
}