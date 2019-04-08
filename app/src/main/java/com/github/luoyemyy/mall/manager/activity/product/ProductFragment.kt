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
import com.github.luoyemyy.ext.toJsonString
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
import com.google.android.material.tabs.TabLayout

class ProductFragment : BaseFragment() {

    private lateinit var mBinding: FragmentProductBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentProductBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.product, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add -> aoe()
            R.id.sort -> {
                mPresenter.getCategoryId().also {
                    if (it >= 0) {
                        findNavController().navigate(R.id.action_productFragment_to_productSortFragment, bundleOf("categoryId" to it))
                    }
                }
            }
            R.id.template -> {
                findNavController().navigate(R.id.action_productFragment_to_productTemplateFragment)
            }
        }
        return true
    }

    private fun aoe(productId: Long = 0) {
        findNavController().navigate(R.id.action_productFragment_to_productAoeFragment, bundleOf("productId" to productId, "category" to mPresenter.categoryJson))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            if (it == -1) {
                refreshState(false)
            } else {
                refreshState(refreshing = false, enable = false)
                mBinding.apply {
                    viewPager.adapter = ViewPagerAdapter(childFragmentManager)
                    viewPager.setCurrentItem(mPresenter.selectIndex, false)
                    tabLayout.setupWithViewPager(viewPager)
                }
            }
        })

        mBinding.swipeRefreshLayout.setOnRefreshListener {
            mPresenter.loadRefresh()
        }
        mBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {}

            override fun onTabUnselected(p0: TabLayout.Tab?) {}

            override fun onTabSelected(p0: TabLayout.Tab?) {
                mPresenter.selectIndex = p0?.position ?: 0
            }

        })
        refreshState(true)

        Bus.addCallback(lifecycle, this, BusEvent.PRODUCT_EDIT, BusEvent.PRODUCT_DETAIL)

        mPresenter.loadInit()
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.PRODUCT_EDIT -> aoe(msg.longValue)
            BusEvent.PRODUCT_DETAIL -> {
                findNavController().navigate(R.id.action_productFragment_to_productDetailFragment, bundleOf("productId" to msg.longValue))
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
            return ProductListFragment.fromCategory(mPresenter.categories[position].id)
        }

        override fun getCount(): Int {
            return mPresenter.categories.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mPresenter.categories[position].name
        }
    }

    class Presenter(var app: Application) : MvpSimplePresenter<Category>(app) {

        var categoryJson: String? = null
        val categories = mutableListOf<Category>()
        var selectIndex = 0

        fun getCategoryId(): Long {
            return if (categories.isNotEmpty() && selectIndex >= 0 && selectIndex < categories.size) {
                categories[selectIndex].id
            } else -1L
        }

        override fun reload(): Boolean {
            return false
        }

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            getProductApi().categoryList().list { ok, value ->
                if (ok) {
                    setInitialized()
                    categoryJson = value.toJsonString()
                    categories.add(Category().apply {
                        id = 0
                        name = app.getString(R.string.product_all)
                    })
                    if (!value.isNullOrEmpty()) {
                        categories.addAll(value)
                    }
                    flag.postValue(selectIndex)
                } else {
                    flag.postValue(-1)
                }
            }
        }
    }
}