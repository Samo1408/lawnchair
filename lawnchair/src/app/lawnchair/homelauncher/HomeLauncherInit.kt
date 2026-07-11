package app.lawnchair.homelauncher

import android.content.Context

object HomeLauncherInit {
    @Volatile private var initialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        initialized = true

        // Start loading apps from all users in background
        LawnchairProfileManager.getInstance(context).loadAsync()

        // Listen for package changes to auto-refresh
        LawnchairProfileManager.getInstance(context).registerPackageListener(context)

        // Initialize Home-Launcher integration
        HomeLauncherIntegration.initialize(context)
    }
}
