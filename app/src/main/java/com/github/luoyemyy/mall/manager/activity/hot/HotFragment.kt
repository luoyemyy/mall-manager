package com.github.luoyemyy.mall.manager.activity.hot

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.ext.confirm
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.activity.base.hideLoading
import com.github.luoyemyy.mall.manager.activity.base.showLoading
import com.github.luoyemyy.mall.manager.api.getHotApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.Hot
import com.github.luoyemyy.mall.manager.bean.Sort
import com.github.luoyemyy.mall.manager.databinding.FragmentHotBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentHotRecyclerBinding
import com.github.luoyemyy.mall.manager.util.*
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class HotFragment : BaseFragment() {

    private lateinit var mBinding: FragmentHotBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mSortHelper: ItemTouchHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentHotBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.add_sort, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.apply {
            findItem(R.id.add)?.isVisible = !mPresenter.enableSort
            findItem(R.id.sort)?.isVisible = !mPresenter.enableSort
            findItem(R.id.save)?.isVisible = mPresenter.enableSort
            findItem(R.id.cancel)?.isVisible = mPresenter.enableSort
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add -> findNavController().navigate(R.id.action_hotFragment_to_hotAoeFragment)
            R.id.save -> mPresenter.saveSort()
            R.id.cancel -> mPresenter.cancelSort()
            R.id.sort -> mPresenter.startSort()

        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mPresenter.setFlagObserver(this, Observer {
            when (it) {
                1 -> { //cancelSort
                    mBinding.swipeRefreshLayout.isEnabled = false
                    requireActivity().invalidateOptionsMenu()
                }
                2 -> { //startSort
                    mBinding.swipeRefreshLayout.isEnabled = true
                    requireActivity().invalidateOptionsMenu()
                }
            }
        })
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(LinearDecoration.middle(requireContext()))
            setHasFixedSize(true)
        }
        mSortHelper = ItemTouchHelper(object : SortCallback() {
            override fun move(source: Int, target: Int): Boolean = mPresenter.move(source, target)
        }).apply {
            attachToRecyclerView(mBinding.recyclerView)
        }
        mBinding.swipeRefreshLayout.setOnRefreshListener { mPresenter.loadRefresh() }

        Bus.addCallback(lifecycle, this, BusEvent.HOT_AOE,BusEvent.HOT_COUNT)

        mPresenter.loadInit()
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.HOT_AOE -> mPresenter.loadRefresh()
            BusEvent.HOT_COUNT -> mPresenter.loadRefresh()
        }
    }

    inner class Adapter : MvpSingleAdapter<Hot, FragmentHotRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int {
            return R.layout.fragment_hot_recycler
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun bindContentViewHolder(binding: FragmentHotRecyclerBinding, content: Hot, position: Int, payloads: MutableList<Any>): Boolean {
            binding.switchEnable.isChecked = payloads[0] as Boolean
            return false
        }

        override fun onItemClickListener(vh: VH<FragmentHotRecyclerBinding>, view: View?) {
            val hot = getItem(vh.adapterPosition) ?: return
            findNavController().navigate(R.id.action_hotFragment_to_hotProductFragment, bundleOf("hotId" to hot.id))
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bindItemEvents(vh: VH<FragmentHotRecyclerBinding>) {
            vh.binding?.imgSort?.setOnTouchListener { _, _ ->
                mSortHelper.startDrag(vh).let { true }
            }

            vh.binding?.root?.setOnLongClickListener {
                if (!mPresenter.enableSort) {
                    popupMenu(requireContext(), it, R.menu.edit_delete, getTouchX(), getTouchY()) { id ->
                        when (id) {
                            R.id.edit -> {
                                getItem(vh.adapterPosition)?.also { c ->
                                    findNavController().navigate(R.id.action_hotFragment_to_hotAoeFragment, bundleOf("hotId" to c.id, "description" to c.description, "image" to c.image))
                                }
                            }
                            R.id.delete -> {
                                requireActivity().confirm(messageId = R.string.hot_delete_confirm, cancelButtonId = R.string.cancel, okButtonId = R.string.submit) {
                                    mPresenter.delete(vh.adapterPosition)
                                }
                            }
                        }
                    }
                }
                true
            }
            vh.binding?.switchEnable?.apply {
                setOnClickListener {
                    switchChange(vh.adapterPosition, isChecked)
                }
            }
        }

        private fun switchChange(position: Int, isChecked: Boolean) {
            val msg = if (isChecked) R.string.hot_show_confirm else R.string.hot_hide_confirm
            requireActivity().confirm(messageId = msg, cancelButtonId = R.string.cancel, cancelCallback = {
                notifyItemChanged(position, !isChecked)
            }, okButtonId = R.string.submit) {
                mPresenter.state(position, isChecked)
            }
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun enableEmpty(): Boolean {
            return false
        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<Hot>(app) {
        var enableSort: Boolean = false

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?, afterLoad: (Boolean, List<Hot>?) -> Unit): Boolean {
            getHotApi().list().list { ok, value ->
                afterLoad(ok, value)
            }
            return true
        }

        fun move(source: Int, target: Int): Boolean {
            if (source == target) return false
            getAdapterSupport()?.apply {
                getDataSet().move(source, target, getAdapter())
                return true
            }
            return false
        }

        fun startSort() {
            getAdapterSupport()?.apply {
                getDataSet().dataList().forEach {
                    it.enableSort = true
                }
                getAdapter().notifyDataSetChanged()
                enableSort = true
                flag.postValue(1)
            }
        }

        fun cancelSort() {
            getAdapterSupport()?.apply {
                val list = getDataSet().dataList().apply {
                    forEach { it.enableSort = false }
                }.sortedByDescending { it.sort }
                getDataSet().apply {
                    setData(list, getAdapter())
                }
                enableSort = false
                flag.postValue(2)
            }
        }

        fun saveSort() {
            val sortIds = getDataSet().dataList().map { it.sort }.sortedDescending()

            val sort = mutableListOf<Sort>()
            getDataSet().dataList().forEachIndexed { index, category ->
                val sortId = sortIds[index]
                if (category.sort != sortId) {
                    sort.add(Sort(category.id, sortId))
                }
            }

            if (sort.isNotEmpty()) {
                showLoading()
                getHotApi().sort(sort).result {
                    hideLoading()
                    if (it) {
                        getAdapterSupport()?.apply {
                            val list = getDataSet().dataList().mapIndexed { index, category ->
                                category.apply {
                                    this.sort = sortIds[index]
                                    this.enableSort = false
                                }
                            }
                            getDataSet().apply {
                                setData(list, getAdapter())
                            }
                            enableSort = false
                            flag.postValue(2)
                        }
                    }
                }
            } else {
                cancelSort()
            }
        }


        fun delete(position: Int) {
            val category = getDataSet().item(position) ?: return
            getHotApi().delete(category.id).result {
                if (it) {
                    getAdapterSupport()?.apply {
                        getDataSet().remove(listOf(category), getAdapter())
                    }
                }
            }
        }

        fun state(position: Int, enable: Boolean) {
            val hot = getDataSet().item(position) ?: return
            if (hot.count == 0 && enable) {
                getAdapterSupport()?.getAdapter()?.notifyItemChanged(position, false)
                app.toast(R.string.hot_show_tip)
                return
            }
            val state = if (enable) 1 else 0
            getHotApi().state(hot.id, state).result {
                if (!it) {
                    getAdapterSupport()?.getAdapter()?.notifyItemChanged(position, !enable)
                } else {
                    getDataSet().item(position)?.state = state
                }
            }
        }
    }
}