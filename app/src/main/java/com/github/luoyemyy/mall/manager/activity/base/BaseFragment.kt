package com.github.luoyemyy.mall.manager.activity.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.bus.BusResult

abstract class BaseFragment : Fragment(), BusResult {

    fun getTouchX() = (requireActivity() as? BaseActivity)?.touchX ?: 0
    fun getTouchY() = (requireActivity() as? BaseActivity)?.touchY ?: 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun busResult(event: String, msg: BusMsg) {

    }
}