/*
 * Home-Launcher Integration for Lawnchair
 * 
 * Main integration point that ties together all Home-Launcher features:
 * - Multi-user apps cache (MultiUserAppsHelper)
 * - Screen lock (QuickLockHelper)
 * - Filling grid layout (FillingGridLayoutManager)
 * 
 * This module enhances Lawnchair with features from Samo1408/Home-Launcher.
 */

package app.lawnchair.homelauncher

import android.content.Context
import app.lawnchair.LawnchairLauncher
import app.lawnchair.gestures.handlers.GestureHandler
import app.lawnchair.lawnchairApp
import kotlinx.coroutines.launch

/**
 * Gesture handler to lock the screen - can be bound to any gesture
 * in Lawnchair's gesture settings.
 */
class HomeLauncherLockGestureHandler(context: Context) : GestureHandler(context) {

    override suspend fun onTrigger(launcher: LawnchairLauncher) {
        QuickLockHelper.lockWithFeedback(launcher)
    }
}

/**
 * Initialize Home-Launcher integrations when the app starts.
 * Call this from LawnchairApp.onCreate() or similar.
 */
object HomeLauncherIntegration {

    fun initialize(context: Context) {
        // Start loading apps in background for fast drawer access
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            MultiUserAppsHelper.getInstance(context).loadAll()
        }

        // Listen for package changes
        MultiUserAppsHelper.getInstance(context).registerPackageListener(context)
    }
}
