package com.github.luoyemyy.mall.manager.activity.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.mall.manager.databinding.FragmentLoadingBinding
import com.github.luoyemyy.mall.manager.util.BusEvent

class LoadingFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentLoadingBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isCancelable = false
    }
}

fun showLoading() {
    Bus.post(BusEvent.LOADING_START)
}

fun hideLoading() {
    Bus.post(BusEvent.LOADING_END)
}