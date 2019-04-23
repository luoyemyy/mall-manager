package com.github.luoyemyy.mall.manager.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.ext.hide
import com.github.luoyemyy.ext.show
import com.github.luoyemyy.mall.manager.R
import com.github.luoyemyy.mall.manager.activity.base.BaseActivity
import com.github.luoyemyy.mall.manager.activity.login.LoginActivity
import com.github.luoyemyy.mall.manager.databinding.ActivityMainBinding
import com.github.luoyemyy.mall.manager.service.AppService
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.Oss

class MainActivity : BaseActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private val mTopLevelIds = setOf(
            R.id.orderFragment,
            R.id.categoryFragment,
            R.id.productFragment,
            R.id.hotFragment,
            R.id.userFragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.bottomNavigationView.menu.apply {

        }

        Oss.init(application)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setTitle(R.string.category)

        findNavController(R.id.host).also {
            mBinding.apply {
                toolbar.setupWithNavController(it, AppBarConfiguration(mTopLevelIds))
                bottomNavigationView.setupWithNavController(it)
            }
            it.addOnDestinationChangedListener { _, destination, bundle ->
                if (mTopLevelIds.contains(destination.id)) {
                    mBinding.bottomNavigationView.show()
                } else {
                    mBinding.bottomNavigationView.hide()
                }
                if (destination.id == R.id.categoryAoeFragment && bundle?.get("categoryId") != null) {
                    mBinding.toolbar.setTitle(R.string.category_edit)
                }
                if (destination.id == R.id.productAoeFragment && bundle?.get("productId") != null) {
                    mBinding.toolbar.setTitle(R.string.product_edit)
                }
                if (destination.id == R.id.hotAoeFragment && bundle?.get("hotId") != null) {
                    mBinding.toolbar.setTitle(R.string.hot_edit)
                }
            }
        }

        Bus.addCallback(lifecycle, this, BusEvent.LOGIN_EXPIRE,BusEvent.CLEAR_IMAGE_CACHE)
    }


    override fun busResult(event: String, msg: BusMsg) {
        super.busResult(event, msg)
        when (event) {
            BusEvent.LOGIN_EXPIRE -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            BusEvent.CLEAR_IMAGE_CACHE->{
                AppService.clearImage(this)
            }
        }
    }
}
