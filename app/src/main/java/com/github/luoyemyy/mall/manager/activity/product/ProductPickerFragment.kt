package com.github.luoyemyy.mall.manager.activity.product

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.api.getProductApi
import com.github.luoyemyy.mall.manager.api.list
import com.github.luoyemyy.mall.manager.bean.Category
import com.github.luoyemyy.mall.manager.databinding.FragmentProductBinding
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.MvpSimplePresenter
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.recycler.LoadType

class ProductPickerFragment : BaseFragment() {

    private lateinit var mBinding: FragmentProductBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProductBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.submit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        mPresenter.submit()
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            when (it) {
                0 -> findNavController().navigateUp()
                -1 -> refreshState(false)
                else -> {
                    refreshState(refreshing = false, enable = false)
                    mBinding.apply {
                        viewPager.adapter = ViewPagerAdapter(childFragmentManager)
                        tabLayout.setupWithViewPager(viewPager)
                    }
                }
            }
        })
        mBinding.swipeRefreshLayout.setOnRefreshListener {
            mPresenter.loadRefresh()
        }
        refreshState(true)

        Bus.addCallback(lifecycle, this, BusEvent.PRODUCT_PICKER_SELECT)

        mPresenter.loadInit()
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.PRODUCT_PICKER_SELECT -> {
                mPresenter.selectedResult(msg.longValue, msg.boolValue)
            }
        }
    }

    private fun refreshState(refreshing: Boolean, enable: Boolean = true) {
        mBinding.swipeRefreshLayout.apply {
            isRefreshing = refreshing
            isEnabled = enable
        }
    }

    inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return ProductPickerListFragment.fromCategory(mPresenter.categories[position].id)
        }

        override fun getCount(): Int {
            return mPresenter.categories.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mPresenter.categories[position].name
        }
    }

    class Presenter(var app: Application) : MvpSimplePresenter<Category>(app) {

        val categories = mutableListOf<Category>()

        private val mSelectIds = mutableSetOf<Long>()

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            getProductApi().categoryList().list { ok, value ->
                if (ok) {
                    categories.add(Category().apply {
                        id = 0
                        name = app.getString(R.string.product_all)
                    })
                    if (!value.isNullOrEmpty()) {
                        categories.addAll(value)
                    }
                    flag.postValue(1)
                } else {
                    flag.postValue(-1)
                }
            }
        }

        fun selectedResult(id: Long, selected: Boolean) {
            if (selected) {
                mSelectIds.add(id)
            } else {
                mSelectIds.remove(id)
            }
        }

        fun submit() {
            Bus.post(BusEvent.PRODUCT_PICKER, extra = bundleOf("ids" to mSelectIds.toLongArray()))
            flag.postValue(0)
        }
    }
}