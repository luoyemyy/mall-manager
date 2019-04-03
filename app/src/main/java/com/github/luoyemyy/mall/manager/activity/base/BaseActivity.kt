package com.github.luoyemyy.mall.manager.activity.base

import android.os.Bundle
import android.view.MotionEvent
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.bus.BusResult
import com.github.luoyemyy.ext.autoCloseKeyboardAndClearFocus
import com.github.luoyemyy.mall.manager.util.BusEvent

abstract class BaseActivity : AppCompatActivity(), BusResult {

    var touchX: Int = 0
    var touchY: Int = 0

    private var mLoadingFragment: LoadingFragment? = null

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        touchX = ev?.rawX?.toInt() ?: 0
        touchY = ev?.rawY?.toInt() ?: 0
        autoCloseKeyboardAndClearFocus(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bus.addCallback(lifecycle, this, BusEvent.LOADING_START, BusEvent.LOADING_END)
    }

    @CallSuper
    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.LOADING_START -> {
                val fragment = mLoadingFragment
                if (fragment != null && fragment.dialog.isShowing) {
                    //pass
                } else {
                    mLoadingFragment = LoadingFragment().apply {
                        show(supportFragmentManager, null)
                    }
                }
            }
            BusEvent.LOADING_END -> {
                mLoadingFragment?.dismissAllowingStateLoss()
                mLoadingFragment = null
            }
        }
    }
}