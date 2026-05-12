/*
 * Copyright (C) 2024 Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.popup;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.model.data.ItemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides custom system shortcuts for the app long-press menu.
 * This extends the standard system shortcuts with additional options.
 */
public class CustomPopupAdapter {

    /**
     * Gets all custom system shortcuts for an item.
     */
    @NonNull
    public static List<SystemShortcut.Factory<BaseDraggingActivity>> getCustomShortcuts() {
        List<SystemShortcut.Factory<BaseDraggingActivity>> shortcuts = new ArrayList<>();
        
        // Add all custom shortcuts
        shortcuts.add(CustomShortcuts.ADVANCED_PERMISSIONS);
        shortcuts.add(CustomShortcuts.SHARE_APP);
        shortcuts.add(CustomShortcuts.INSTALL_TO_USER);
        shortcuts.add(CustomShortcuts.SHOW_DATA_DIR);
        shortcuts.add(CustomShortcuts.SHOW_APP_DIR);
        shortcuts.add(CustomShortcuts.SELECT);
        
        return shortcuts;
    }

    /**
     * Gets shortcuts that should appear in the left group (main actions).
     */
    @NonNull
    public static List<SystemShortcut.Factory<BaseDraggingActivity>> getLeftGroupShortcuts() {
        List<SystemShortcut.Factory<BaseDraggingActivity>> shortcuts = new ArrayList<>();
        
        // Main actions: Share App, Select
        shortcuts.add(CustomShortcuts.SHARE_APP);
        shortcuts.add(CustomShortcuts.SELECT);
        
        return shortcuts;
    }

    /**
     * Gets shortcuts that should appear in the right group (secondary actions).
     */
    @NonNull
    public static List<SystemShortcut.Factory<BaseDraggingActivity>> getRightGroupShortcuts() {
        List<SystemShortcut.Factory<BaseDraggingActivity>> shortcuts = new ArrayList<>();
        
        // Secondary actions
        shortcuts.add(CustomShortcuts.ADVANCED_PERMISSIONS);
        shortcuts.add(CustomShortcuts.INSTALL_TO_USER);
        shortcuts.add(CustomShortcuts.SHOW_DATA_DIR);
        shortcuts.add(CustomShortcuts.SHOW_APP_DIR);
        
        return shortcuts;
    }

    /**
     * Adds custom shortcuts to an existing list of shortcuts.
     */
    @NonNull
    public static List<SystemShortcut<BaseDraggingActivity>> createCustomShortcuts(
            BaseDraggingActivity activity, ItemInfo itemInfo, View originalView) {
        List<SystemShortcut<BaseDraggingActivity>> shortcuts = new ArrayList<>();
        
        // Create and add custom shortcuts
        SystemShortcut<BaseDraggingActivity> advancedPerms = 
                CustomShortcuts.ADVANCED_PERMISSIONS.getShortcut(activity, itemInfo, originalView);
        if (advancedPerms != null) shortcuts.add(advancedPerms);
        
        SystemShortcut<BaseDraggingActivity> shareApp = 
                CustomShortcuts.SHARE_APP.getShortcut(activity, itemInfo, originalView);
        if (shareApp != null) shortcuts.add(shareApp);
        
        SystemShortcut<BaseDraggingActivity> installToUser = 
                CustomShortcuts.INSTALL_TO_USER.getShortcut(activity, itemInfo, originalView);
        if (installToUser != null) shortcuts.add(installToUser);
        
        SystemShortcut<BaseDraggingActivity> showDataDir = 
                CustomShortcuts.SHOW_DATA_DIR.getShortcut(activity, itemInfo, originalView);
        if (showDataDir != null) shortcuts.add(showDataDir);
        
        SystemShortcut<BaseDraggingActivity> showAppDir = 
                CustomShortcuts.SHOW_APP_DIR.getShortcut(activity, itemInfo, originalView);
        if (showAppDir != null) shortcuts.add(showAppDir);
        
        SystemShortcut<BaseDraggingActivity> select = 
                CustomShortcuts.SELECT.getShortcut(activity, itemInfo, originalView);
        if (select != null) shortcuts.add(select);
        
        return shortcuts;
    }

    /**
     * Gets the number of custom shortcuts available.
     */
    public static int getCustomShortcutCount() {
        return 6; // ADVANCED_PERMISSIONS, SHARE_APP, INSTALL_TO_USER, 
                  // SHOW_DATA_DIR, SHOW_APP_DIR, SELECT
    }
}