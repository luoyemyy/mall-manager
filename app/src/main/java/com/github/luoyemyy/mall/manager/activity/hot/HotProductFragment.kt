package com.github.luoyemyy.mall.manager.activity.hot

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.ext.confirm
import com.github.luoyemyy.ext.toJsonString
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.api.getHotApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.Product
import com.github.luoyemyy.mall.manager.databinding.FragmentHotBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentProductPickerRecyclerBinding
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.MvpRecyclerPresenter
import com.github.luoyemyy.mall.manager.util.MvpSingleAdapter
import com.github.luoyemyy.mall.manager.util.popupMenu
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class HotProductFragment : BaseFragment() {

    private lateinit var mBinding: FragmentHotBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentHotBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        findNavController().navigate(R.id.action_hotProductFragment_to_productPickerFragment)
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mPresenter.setFlagObserver(this, Observer {

        })
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(LinearDecoration.middle(requireContext()))
            setHasFixedSize(true)
        }
        mBinding.swipeRefreshLayout.setOnRefreshListener { mPresenter.loadRefresh() }

        Bus.addCallback(lifecycle, this, BusEvent.PRODUCT_PICKER)

        mPresenter.loadInit(arguments)
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.PRODUCT_PICKER -> mPresenter.add(msg.extra?.getLongArray("ids"))
        }
    }

    inner class Adapter : MvpSingleAdapter<Product, FragmentProductPickerRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int {
            return R.layout.fragment_product_picker_recycler
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun onItemClickListener(vh: VH<FragmentProductPickerRecyclerBinding>, view: View?) {
            val product = getItem(vh.adapterPosition) ?: return
            findNavController().navigate(R.id.action_hotProductFragment_to_productDetailFragment, bundleOf("productId" to product.id))
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bindItemEvents(vh: VH<FragmentProductPickerRecyclerBinding>) {
            vh.binding?.root?.setOnLongClickListener {
                popupMenu(requireContext(), it, R.menu.delete, getTouchX(), getTouchY()) { id ->
                    when (id) {
                        R.id.delete -> {
                            requireActivity().confirm(messageId = R.string.hot_product_delete_confirm, cancelButtonId = R.string.cancel, okButtonId = R.string.submit) {
                                mPresenter.delete(vh.adapterPosition)
                            }
                        }
                    }
                }
                true
            }
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun enableEmpty(): Boolean {
            return false
        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<Product>(app) {

        var mHotId: Long = 0

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?, afterLoad: (Boolean, List<Product>?) -> Unit): Boolean {
            if (loadType.isInit()) {
                mHotId = bundle?.getLong("hotId", 0L) ?: 0
            }
            if (mHotId == 0L) {
                afterLoad(false, null)
                return true
            }
            getHotApi().listProduct(mHotId).list { ok, value ->
                afterLoad(ok, value)
            }
            return true
        }

        fun add(ids: LongArray?) {
            val value = ids?.toList()
            if (!value.isNullOrEmpty()) {
                val existIds = mutableListOf<Long>()
                val addIds = mutableListOf<Long>()
                getDataSet().dataList().apply {
                    val l = map { it.id }
                    value.forEach {
                        if (it in l) {
                            existIds += it
                        } else {
                            addIds += it
                        }
                    }
                }

                if (addIds.isNotEmpty()){
                    addIds.toJsonString()?.apply {
                        getHotApi().addProduct(mHotId, this).result {
                            if (it) {
                                loadRefresh()
                                postCurrentCount()
                            }
                        }
                    }
                }
            }
        }

        fun delete(position: Int) {
            val entity = getDataSet().item(position) ?: return
            getHotApi().deleteProduct(mHotId, entity.id).result {
                if (it) {
                    postCurrentCount()
                    getAdapterSupport()?.apply {
                        getDataSet().remove(listOf(entity), getAdapter())
                    }
                }
            }
        }

        private fun postCurrentCount() {
            Bus.post(BusEvent.HOT_COUNT)
        }
    }
}