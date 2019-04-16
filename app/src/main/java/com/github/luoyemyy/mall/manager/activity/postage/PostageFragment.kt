package com.github.luoyemyy.mall.manager.activity.postage

import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.activity.base.hideLoading
import com.github.luoyemyy.mall.manager.activity.base.showLoading
import com.github.luoyemyy.mall.manager.api.getPostageApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.api.result
import com.github.luoyemyy.mall.manager.bean.Postage
import com.github.luoyemyy.mall.manager.databinding.FragmentPostageChangePriceBinding
import com.github.luoyemyy.mall.manager.databinding.FragmentPostageRecyclerBinding
import com.github.luoyemyy.mall.manager.databinding.LayoutRefreshRecyclerBinding
import com.github.luoyemyy.mall.manager.util.MvpRecyclerPresenter
import com.github.luoyemyy.mall.manager.util.MvpSingleAdapter
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class PostageFragment : BaseFragment() {

    private lateinit var mBinding: LayoutRefreshRecyclerBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutRefreshRecyclerBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.save, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.findItem(R.id.save)?.isVisible = mPresenter.change()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        mPresenter.save()
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mPresenter.setFlagObserver(this, Observer {
            if (it == 0) {
                findNavController().navigateUp()
            } else if (it == 1) {
                requireActivity().invalidateOptionsMenu()
            }
        })
        mBinding.recyclerView.apply {
            setGridManager(3)
            addItemDecoration(GridDecoration.create(requireContext(), 3, 1))
            setHasFixedSize(true)
        }
        mBinding.swipeRefreshLayout.setOnRefreshListener {
            mPresenter.loadRefresh()
        }

        mPresenter.loadInit()
    }

    inner class Adapter : MvpSingleAdapter<Postage, FragmentPostageRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int {
            return R.layout.fragment_postage_recycler
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun getItemClickViews(binding: FragmentPostageRecyclerBinding): Array<View> {
            return arrayOf(binding.root, binding.switchEnable)
        }

        override fun onItemClickListener(vh: VH<FragmentPostageRecyclerBinding>, view: View?) {
            if (view == vh.binding?.switchEnable) {
                mPresenter.changePost(vh.adapterPosition, vh.binding?.switchEnable?.isChecked
                        ?: false)
            } else {
                val price = getItem(vh.adapterPosition)?.price ?: 0f
                val binding = FragmentPostageChangePriceBinding.inflate(layoutInflater, null, false)
                val np100 = setNumberPicker(binding.np100, price, 100f)
                val np10 = setNumberPicker(binding.np10, np100, 10f)
                val np1 = setNumberPicker(binding.np1, np10, 1f)
                val np01 = setNumberPicker(binding.np01, np1, 0.1f)
                setNumberPicker(binding.np001, np01, 0.01f)
                AlertDialog.Builder(requireContext()).setView(binding.root).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.submit) { _, _ ->
                    mPresenter.changePrice(vh.adapterPosition, getPrice(binding))
                }.show()
            }
        }

        private fun setNumberPicker(numberPicker: NumberPicker, value: Float, bit: Float): Float {
            numberPicker.minValue = 0
            numberPicker.maxValue = 9
            numberPicker.value = (value / bit).toInt()
            numberPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS;
            return value - (numberPicker.value * bit)
        }

        private fun getPrice(binding: FragmentPostageChangePriceBinding): Float {
            return binding.np100.value * 100.00f +
                    binding.np10.value * 10.00f +
                    binding.np1.value * 1.00f +
                    binding.np01.value * 0.10f +
                    binding.np001.value * 0.01f
        }


        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun enableEmpty(): Boolean {
            return false
        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<Postage>(app) {

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?, afterLoad: (Boolean, List<Postage>?) -> Unit): Boolean {
            getPostageApi().list().list { ok, value ->
                value?.forEach {
                    it.edit()
                }
                afterLoad(ok, value)
                if (loadType.isRefresh()) {
                    flag.postValue(1)
                }
            }
            return true
        }

        fun change(): Boolean {
            return getDataSet().dataList().any { it.price != it.backPrice || it.post != it.backPost }
        }

        fun changePrice(position: Int, price: Float) {
            getDataSet().item(position)?.price = price
            flag.postValue(1)
            getAdapterSupport()?.apply {
                getDataSet().change(position, getAdapter())
            }
        }

        fun changePost(position: Int, checked: Boolean) {
            getDataSet().item(position)?.post = if (checked) 1 else 0
            flag.postValue(1)
            getAdapterSupport()?.apply {
                getDataSet().change(position, getAdapter())
            }
        }

        fun save() {
            val list = getDataSet().dataList().filter { it.price != it.backPrice || it.post != it.backPost }
            if (list.isNotEmpty()) {
                showLoading()
                getPostageApi().edit(list).result {
                    hideLoading()
                    if (it) {
                        app.toast(R.string.save_success)
                        loadRefresh()
                    }
                }
            }
        }
    }
}