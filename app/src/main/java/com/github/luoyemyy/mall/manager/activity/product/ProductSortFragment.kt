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
import com.github.luoyemyy.mall.manager.bean.Product
import com.github.luoyemyy.mall.manager.bean.Sort
import com.github.luoyemyy.mall.manager.databinding.FragmentProductListBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentProductSortRecyclerBinding
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.MvpRecyclerPresenter
import com.github.luoyemyy.mall.manager.util.MvpSingleAdapter
import com.github.luoyemyy.mall.manager.util.SortCallback
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class ProductSortFragment : BaseFragment() {

    private lateinit var mBinding: FragmentProductListBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mSortHelper: ItemTouchHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProductListBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.save -> mPresenter.saveSort()
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mPresenter.setFlagObserver(this, Observer {
            if (it == 0) {
                findNavController().navigateUp()
            } else if (it == 1) {
                mBinding.swipeRefreshLayout.isEnabled = false
            }
        })
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(LinearDecoration.beginMiddle(requireContext()))
            setHasFixedSize(true)
        }
        mSortHelper = ItemTouchHelper(object : SortCallback() {
            override fun move(source: Int, target: Int): Boolean = mPresenter.move(source, target)
        }).apply {
            attachToRecyclerView(mBinding.recyclerView)
        }
        mBinding.swipeRefreshLayout.setOnRefreshListener { mPresenter.loadRefresh() }

        mPresenter.loadInit(arguments)
    }

    inner class Adapter : MvpSingleAdapter<Product, FragmentProductSortRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int {
            return R.layout.fragment_product_sort_recycler
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bindItemEvents(vh: VH<FragmentProductSortRecyclerBinding>) {
            vh.binding?.imgSort?.setOnTouchListener { _, _ ->
                mSortHelper.startDrag(vh).let { true }
            }
        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<Product>(app) {
        var categoryId: Long = 0

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?, afterLoad: (Boolean, List<Product>?) -> Unit): Boolean {
            if (loadType.isInit()) {
                categoryId = bundle?.getLong("categoryId") ?: 0L
            }
            getProductApi().list(categoryId, paging.current().toInt()).list { ok, value ->
                afterLoad(ok, value)
            }
            return true
        }

        override fun afterLoadInit(ok: Boolean, list: List<Product>?) {
            super.afterLoadInit(ok, list)
            if (ok) {
                flag.postValue(1)
            }
        }

        fun move(source: Int, target: Int): Boolean {
            if (source == target) return false
            if (getDataSet().item(target) == null) return false
            getAdapterSupport()?.apply {
                getDataSet().move(source, target, getAdapter())
                return true
            }
            return false
        }

        fun saveSort() {
            val sortIds = getDataSet().dataList().map { it.sort }.sortedDescending()

            val sort = mutableListOf<Sort>()
            getDataSet().dataList().forEachIndexed { index, product ->
                val sortId = sortIds[index]
                if (product.sort != sortId) {
                    sort.add(Sort(product.id, sortId, categoryId))
                }
            }

            if (sort.isNotEmpty()) {
                showLoading()
                getProductApi().sort(sort).result {
                    hideLoading()
                    if (it) {
                        flag.postValue(0)
                        Bus.post(BusEvent.PRODUCT_SORT, longValue = categoryId)
                    }
                }
            } else {
                app.toast(R.string.sort_nothing)
            }
        }
    }
}