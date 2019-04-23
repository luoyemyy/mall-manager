package com.github.luoyemyy.mall.manager.activity.order

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.api.getOrderApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.bean.OrderItem
import com.github.luoyemyy.mall.manager.databinding.FragmentOrderListRecyclerBinding
import com.github.luoyemyy.mall.manager.databinding.LayoutRefreshRecyclerBinding
import com.github.luoyemyy.mall.manager.util.MvpRecyclerPresenter
import com.github.luoyemyy.mall.manager.util.MvpSingleAdapter
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class OrderListFragment : BaseFragment() {

    private lateinit var mBinding: LayoutRefreshRecyclerBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun fromType(typeId: Int): OrderListFragment {
            return OrderListFragment().apply {
                arguments = bundleOf("stateId" to typeId)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutRefreshRecyclerBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(LinearDecoration.beginMiddle(requireContext()))
            setHasFixedSize(true)
        }
        mBinding.swipeRefreshLayout.setOnRefreshListener { mPresenter.loadRefresh() }

        mPresenter.loadInit(arguments)
    }

    inner class Adapter : MvpSingleAdapter<OrderItem, FragmentOrderListRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int {
            return R.layout.fragment_order_list_recycler
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun onItemClickListener(vh: VH<FragmentOrderListRecyclerBinding>, view: View?) {

        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<OrderItem>(app) {

        var stateId: Int = 0

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?, afterLoad: (Boolean, List<OrderItem>?) -> Unit): Boolean {
            if (loadType.isInit()) {
                stateId = bundle?.getInt("stateId") ?: 0
            }
            getOrderApi().list(stateId, paging.current().toInt()).list { ok, value ->
                value?.forEach {
                    it.map(app)
                }
                afterLoad(ok, value)
            }
            return true
        }
    }
}