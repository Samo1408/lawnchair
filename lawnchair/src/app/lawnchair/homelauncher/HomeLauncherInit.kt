package app.lawnchair.homelauncher

import android.content.Context

object HomeLauncherInit {
    @Volatile private var initialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        LawnchairProfileManager.getInstance(context).loadAllAsync()
        LawnchairProfileManager.getInstance(context).registerPackageListener(context)
        HomeLauncherIntegration.initialize(context)
    }
}
