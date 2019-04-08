package com.github.luoyemyy.mall.manager.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.github.luoyemyy.file.FileManager


class AppService : IntentService("AppService") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_CLEAR_IMAGE -> clearImage()
        }
    }

    private fun clearImage() {
        FileManager.getInstance().inner().cacheDir(FileManager.IMAGE)?.listFiles()?.forEach {
            if (it.isFile && it.delete()) {
                //nothing
            }
        }
    }

    companion object {

        private const val ACTION_CLEAR_IMAGE = "com.github.luoyemyy.mall.manager.service.action.ACTION_CLEAR_IMAGE"

        @JvmStatic
        fun clearImage(context: Context) {
            val intent = Intent(context, AppService::class.java).apply {
                action = ACTION_CLEAR_IMAGE
            }
            context.startService(intent)
        }
    }
}
