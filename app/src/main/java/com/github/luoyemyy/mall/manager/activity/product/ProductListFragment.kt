package com.github.luoyemyy.mall.manager.activity.product

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.ext.confirm
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.api.getProductApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.Product
import com.github.luoyemyy.mall.manager.databinding.FragmentProductListBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentProductListRecyclerBinding
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.MvpRecyclerPresenter
import com.github.luoyemyy.mall.manager.util.MvpSingleAdapter
import com.github.luoyemyy.mall.manager.util.popupMenu
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class ProductListFragment : BaseFragment() {

    private lateinit var mBinding: FragmentProductListBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun fromCategory(categoryId: Long): ProductListFragment {
            return ProductListFragment().apply {
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

        Bus.addCallback(lifecycle, this, BusEvent.PRODUCT_AOE, BusEvent.PRODUCT_SORT, BusEvent.PRODUCT_ONLINE, BusEvent.PRODUCT_DELETE)

        mPresenter.loadInit(arguments)
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.PRODUCT_AOE -> {
                val oldIds = msg.extra?.getLongArray("oldIds")
                val newIds = msg.extra?.getLongArray("newIds")
                val categoryId = mPresenter.categoryId
                if (categoryId == 0L || oldIds?.contains(categoryId) == true || newIds?.contains(categoryId) == true) {
                    mPresenter.loadRefresh()
                }
            }
            BusEvent.PRODUCT_SORT -> {
                if (mPresenter.categoryId == msg.longValue) {
                    mPresenter.loadRefresh()
                }
            }
            BusEvent.PRODUCT_ONLINE -> {
                mPresenter.onlineResult(msg.longValue, msg.boolValue)
            }

            BusEvent.PRODUCT_DELETE -> {
                mPresenter.deleteResult(msg.longValue)
            }
        }
    }

    inner class Adapter : MvpSingleAdapter<Product, FragmentProductListRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int {
            return R.layout.fragment_product_list_recycler
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun bindContentViewHolder(binding: FragmentProductListRecyclerBinding, content: Product, position: Int, payloads: MutableList<Any>): Boolean {
            binding.switchEnable.isChecked = payloads[0] as Boolean
            return false
        }

        override fun getItemClickViews(binding: FragmentProductListRecyclerBinding): Array<View> {
            return arrayOf(binding.root, binding.switchEnable)
        }

        override fun onItemClickListener(vh: VH<FragmentProductListRecyclerBinding>, view: View?) {
            val binding = vh.binding ?: return
            when (view) {
                binding.root -> {
                    mPresenter.detail(vh.adapterPosition)
                }
                binding.switchEnable -> {
                    switchChange(vh.adapterPosition, binding.switchEnable.isChecked)
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bindItemEvents(vh: VH<FragmentProductListRecyclerBinding>) {
            vh.binding?.root?.setOnLongClickListener {
                popupMenu(requireContext(), it, R.menu.edit_delete, getTouchX(), getTouchY()) { id ->
                    when (id) {
                        R.id.edit -> {
                            mPresenter.edit(vh.adapterPosition)
                        }
                        R.id.delete -> {
                            requireActivity().confirm(messageId = R.string.product_delete_confirm, cancelButtonId = R.string.cancel, okButtonId = R.string.submit) {
                                mPresenter.delete(vh.adapterPosition)
                            }
                        }
                    }
                }
            }
        }

        private fun switchChange(position: Int, isChecked: Boolean) {
            val msg = if (isChecked) R.string.product_online_confirm else R.string.product_offline_confirm
            requireActivity().confirm(messageId = msg, cancelButtonId = R.string.cancel, cancelCallback = {
                notifyItemChanged(position, !isChecked)
            }, okButtonId = R.string.submit) {
                mPresenter.online(position, isChecked)
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

        fun edit(position: Int) {
            val product = getDataSet().item(position) ?: return
            Bus.post(BusEvent.PRODUCT_EDIT, longValue = product.id)
        }

        fun detail(position: Int) {
            val product = getDataSet().item(position) ?: return
            Bus.post(BusEvent.PRODUCT_DETAIL, longValue = product.id)
        }

        fun deleteResult(id: Long) {
            getAdapterSupport()?.apply {
                getDataSet().dataList().firstOrNull { it.id == id }?.apply {
                    getDataSet().remove(listOf(this), getAdapter())
                }
            }
        }

        fun delete(position: Int) {
            val product = getDataSet().item(position) ?: return
            getProductApi().delete(product.id).result {
                if (it) {
                    Bus.post(BusEvent.PRODUCT_DELETE, longValue = product.id)
                }
            }
        }

        fun onlineResult(id: Long, enable: Boolean) {
            getAdapterSupport()?.apply {
                getDataSet().apply {
                    dataList().firstOrNull { it.id == id }?.also {
                        it.online = enable
                        change(it, getAdapter(), enable)
                    }
                }
            }
        }

        fun online(position: Int, enable: Boolean) {
            val product = getDataSet().item(position) ?: return
            getProductApi().online(product.id, enable).result {
                if (it) {
                    Bus.post(BusEvent.PRODUCT_ONLINE, longValue = product.id, boolValue = enable)
                } else {
                    getAdapterSupport()?.getAdapter()?.notifyItemChanged(position, !enable)
                }
            }
        }
    }
}