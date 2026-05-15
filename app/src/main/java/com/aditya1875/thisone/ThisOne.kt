// ─────────────────────────────────────────────
// ThisOneApp.kt  (Application class)
// Add to AndroidManifest: android:name=".ThisOneApp"
// ─────────────────────────────────────────────
package com.aditya1875.thisone

import android.app.Application
import com.aditya1875.thisone.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ThisOneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ThisOneApp)
            modules(appModule)
        }
    }
}
