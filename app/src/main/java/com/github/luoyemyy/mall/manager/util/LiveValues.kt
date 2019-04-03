package com.github.luoyemyy.mall.manager.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.github.luoyemyy.mall.manager.app.App

object LiveValues {
    var userId: Long = 0
    var token: String? = null
    var network: Boolean = false

    fun initValues(app: App){
        userId = UserInfo.getUserId(app)
        token = UserInfo.getToken(app)
        network = hasNetwork(app)

        app.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                LiveValues.network = hasNetwork(app)
            }
        }, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }


    private fun hasNetwork(context: Context): Boolean {
        val conn = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return conn.activeNetworkInfo != null
    }
}