package com.fastlink.huaweinotification

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PushServiceManager.getPushToken(this)
    }
}