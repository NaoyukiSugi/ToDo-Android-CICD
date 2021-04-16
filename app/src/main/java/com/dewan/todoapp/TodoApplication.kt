package com.dewan.todoapp

import android.app.Application
import android.content.Context
import com.dewan.todoapp.model.local.AppPreferences
import com.dewan.todoapp.util.log.DebugTree
import com.dewan.todoapp.util.log.ReleaseTree
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import timber.log.Timber


class TodoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupAppCenter()
        setup()
    }

    private fun setup() {
        val sharesPreferences =
            this.getSharedPreferences(BuildConfig.PREF_NAME, Context.MODE_PRIVATE)
        val appPreferences = AppPreferences(sharesPreferences)

        if (BuildConfig.DEBUG) Timber.plant(DebugTree(appPreferences)) else Timber.plant(
            ReleaseTree(
                appPreferences
            )
        )
    }

    private fun setupAppCenter() {
        AppCenter.start(
            this, "0bc0f90a-a9c2-4d09-8bf3-3dfb683d92aa",
            Analytics::class.java, Crashes::class.java
        )
    }
}
