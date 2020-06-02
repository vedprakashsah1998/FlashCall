package com.infinty8.flashcall

import com.infinty8.flashcall.di.appModule
import com.infinty8.flashcall.di.mainModule
import com.infinty8.flashcall.di.meetingHistoryModule
import com.infinty8.flashcall.sharedpref.AppPref
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.chibatching.kotpref.Kotpref
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FlashCall : Application() {


    override fun onCreate() {
        super.onCreate()

        initializeKotPref()
        setThemeMode()
        initializeKoin()
    }

    private fun initializeKotPref() {
        Kotpref.init(this)
    }

    private fun setThemeMode() {
        if (AppPref.isLightThemeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger()
            androidContext(this@FlashCall)
            modules(
                listOf(
                    appModule,
                    mainModule,
                    meetingHistoryModule
                )
            )
        }
    }


}