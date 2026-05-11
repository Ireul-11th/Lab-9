package com.example.lab9

import android.app.Application
import com.example.lab9.data.AppContainer
import com.example.lab9.data.DefaultAppContainer

class BluromaticApplication : Application()  {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
