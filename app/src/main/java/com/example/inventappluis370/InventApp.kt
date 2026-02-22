package com.example.inventappluis370

import android.app.Application
import android.os.StrictMode
import android.os.SystemClock
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InventApp : Application() {

    override fun onCreate() {
        val t0 = SystemClock.elapsedRealtime()
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    // IMPORTANTE: detectNetwork en algunos dispositivos/emuladores puede provocar ANR
                    // si alguna librería hace warmup/red (aunque sea interno) durante el arranque.
                    // .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        Log.d("InventApp", "onCreate() done in ${SystemClock.elapsedRealtime() - t0}ms")
    }
}
