package com.github.luoyemyy.mall.manager.app

import android.app.Application
import com.github.luoyemyy.config.AppInfo
import com.github.luoyemyy.mall.manager.util.BusEvent
import com.github.luoyemyy.mall.manager.util.LiveValues


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppInfo.init(this)

        LiveValues.initValues(this)
        BusEvent.appEvent(this)


//        Bus.addDebugListener(object : Bus.DebugListener {
//            override fun onPost(event: String, callbacks: List<Bus.Callback>) {
//                Log.e("App", "onPost:  event=$event, callbacks=${callbacks.map { it.interceptEvent() }}")
//            }
//
//            override fun onRegister(currentCallback: Bus.Callback, allCallbacks: List<Bus.Callback>) {
//                Log.e("App", "onRegister:  current=${currentCallback.interceptEvent()}, callbacks=${allCallbacks.map { it.interceptEvent() }}")
//            }
//
//            override fun onUnRegister(currentCallback: Bus.Callback, allCallbacks: List<Bus.Callback>) {
//                Log.e("App", "onUnRegister:  current=${currentCallback.interceptEvent()}, callbacks=${allCallbacks.map { it.interceptEvent() }}")
//            }
//        })
    }
}