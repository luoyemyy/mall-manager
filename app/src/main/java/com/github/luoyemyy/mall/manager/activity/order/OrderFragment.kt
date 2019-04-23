package com.github.luoyemyy.mall.manager.activity.order

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.github.luoyemyy.mall.manager.activity.base.BaseFragment
import com.github.luoyemyy.mall.manager.bean.OrderState
import com.github.luoyemyy.mall.manager.databinding.FragmentOrderBinding
import com.github.luoyemyy.mall.manager.util.MvpSimplePresenter
import com.github.luoyemyy.mvp.getPresenter
import com.google.android.material.tabs.TabLayout

class OrderFragment : BaseFragment() {


    private lateinit var mBinding: FragmentOrderBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentOrderBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()

        mBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {}

            override fun onTabUnselected(p0: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                mPresenter.selectIndex = tab?.position ?: 0
            }

        })
        mBinding.apply {
            viewPager.adapter = ViewPagerAdapter(childFragmentManager)
            viewPager.setCurrentItem(2, false)
            tabLayout.setupWithViewPager(viewPager)
        }

    }

    inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return OrderListFragment.fromType(mPresenter.states[position].id)
        }

        override fun getCount(): Int {
            return mPresenter.states.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mPresenter.states[position].name
        }
    }

    class Presenter(var app: Application) : MvpSimplePresenter<OrderState>(app) {

        val states = OrderState.all(app)
        var selectIndex = 0

    }
}