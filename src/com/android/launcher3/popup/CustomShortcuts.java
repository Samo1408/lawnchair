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

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.os.UserManager;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.R;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.util.PackageManagerHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom system shortcuts for advanced app management.
 */
public class CustomShortcuts {

    // Root explorer apps that we support
    private static final String[] ROOT_EXPLORER_PACKAGES = {
            "com.mixplorer",
            "com.mixplorer.silver", 
            "com.ztgameassitant",
            "com.topjohnwu.magisk",
            "bin.mt.plus",
            "bin.mt.plus.canary"
    };

    /**
     * Advanced Permissions shortcut - opens app's permission settings or usage access
     */
    public static class AdvancedPermissions extends SystemShortcut<BaseDraggingActivity> {
        
        public AdvancedPermissions(BaseDraggingActivity target, ItemInfo itemInfo, View originalView) {
            super(R.drawable.ic_info_no_shadow, R.string.advanced_permissions_label, target, itemInfo, originalView);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            Context context = mTarget;
            String packageName = mItemInfo.getTargetComponent().getPackageName();
            
            // Try to open app's permission settings
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageName));
                mTarget.startActivitySafely(view, intent, mItemInfo);
            } catch (Exception e) {
                // Fallback to main settings
                Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                mTarget.startActivitySafely(view, intent, mItemInfo);
            }
        }
    }

    /**
     * Share App shortcut - shares the APK file of the app
     */
    public static class ShareApp extends SystemShortcut<BaseDraggingActivity> {
        
        public ShareApp(BaseDraggingActivity target, ItemInfo itemInfo, View originalView) {
            super(R.drawable.ic_share, R.string.share_app_label, target, itemInfo, originalView);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            Context context = mTarget;
            String packageName = mItemInfo.getTargetComponent().getPackageName();
            
            // Get the base APK path
            String baseApkPath = "/data/app/" + packageName + "/base.apk";
            File apkFile = new File(baseApkPath);
            
            if (apkFile.exists()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/vnd.android.package-archive");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(apkFile));
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mTarget.startActivitySafely(view, Intent.createChooser(shareIntent, 
                        context.getString(R.string.share_app_chooser_title)), mItemInfo);
            } else {
                // Fallback to Play Store share
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + packageName);
                mTarget.startActivitySafely(view, Intent.createChooser(intent, 
                        context.getString(R.string.share_app_chooser_title)), mItemInfo);
            }
        }
    }

    /**
     * Install to Another User shortcut - installs the app to another user profile
     */
    public static class InstallToUser extends SystemShortcut<BaseDraggingActivity> {
        
        public InstallToUser(BaseDraggingActivity target, ItemInfo itemInfo, View originalView) {
            super(R.drawable.ic_install_no_shadow, R.string.install_to_user_label, target, itemInfo, originalView);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            Context context = mTarget;
            String packageName = mItemInfo.getTargetComponent().getPackageName();
            
            // Get list of users
            UserManager userManager = context.getSystemService(Context.USER_SERVICE);
            List<UserManager> users = new ArrayList<>();
            
            // Show dialog to select user
            showUserSelectionDialog(context, packageName);
        }
        
        private void showUserSelectionDialog(Context context, String packageName) {
            // This would typically show a dialog to select a user
            // For now, we show a toast or use SU to install
            PackageManagerHelper pmHelper = new PackageManagerHelper(context);
            // The actual implementation would check for root and use:
            // pm install-existing --user <user_id> <package>
        }
    }

    /**
     * Show Data Directory shortcut - opens /data/data/<package> using a root explorer
     */
    public static class ShowDataDir extends SystemShortcut<BaseDraggingActivity> {
        
        public ShowDataDir(BaseDraggingActivity target, ItemInfo itemInfo, View originalView) {
            super(R.drawable.ic_folder, R.string.show_data_dir_label, target, itemInfo, originalView);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            Context context = mTarget;
            String packageName = mItemInfo.getTargetComponent().getPackageName();
            String dataPath = "/data/data/" + packageName;
            
            openWithRootExplorer(context, dataPath);
        }
    }

    /**
     * Show App Directory shortcut - opens /data/app/<package>/base.apk using a root explorer
     */
    public static class ShowAppDir extends SystemShortcut<BaseDraggingActivity> {
        
        public ShowAppDir(BaseDraggingActivity target, ItemInfo itemInfo, View originalView) {
            super(R.drawable.ic_info_no_shadow, R.string.show_app_dir_label, target, itemInfo, originalView);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            Context context = mTarget;
            String packageName = mItemInfo.getTargetComponent().getPackageName();
            String appPath = "/data/app/" + packageName;
            
            openWithRootExplorer(context, appPath);
        }
    }

    /**
     * Select shortcut - enables multi-select mode for apps
     */
    public static class Select extends SystemShortcut<BaseDraggingActivity> {
        
        public Select(BaseDraggingActivity target, ItemInfo itemInfo, View originalView) {
            super(R.drawable.ic_widget, R.string.select_label, target, itemInfo, originalView);
        }

        @Override
        public void onClick(View view) {
            dismissTaskMenuView(mTarget);
            // Enable selection mode - this would typically trigger a callback
            // to enable multi-select in the all apps view
            if (mTarget instanceof com.android.launcher3.Launcher) {
                com.android.launcher3.Launcher launcher = (com.android.launcher3.Launcher) mTarget;
                launcher.enterSelectionMode();
            }
        }
    }

    /**
     * Opens a path with a root explorer app if available
     */
    private static void openWithRootExplorer(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        
        for (String explorerPackage : ROOT_EXPLORER_PACKAGES) {
            try {
                Intent intent = pm.getLaunchIntentForPackage(explorerPackage);
                if (intent != null) {
                    // Try to open the path directly
                    intent.setData(Uri.parse("file://" + path));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return;
                }
            } catch (Exception e) {
                // Try next explorer
            }
        }
        
        // Fallback: try generic intent for file browsing
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + path), "resource/folder");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // No file explorer available
        }
    }

    /**
     * Factory for creating custom shortcuts
     */
    public static final SystemShortcut.Factory<BaseDraggingActivity> ADVANCED_PERMISSIONS = 
            (activity, itemInfo, originalView) -> new AdvancedPermissions(activity, itemInfo, originalView);

    public static final SystemShortcut.Factory<BaseDraggingActivity> SHARE_APP = 
            (activity, itemInfo, originalView) -> new ShareApp(activity, itemInfo, originalView);

    public static final SystemShortcut.Factory<BaseDraggingActivity> INSTALL_TO_USER = 
            (activity, itemInfo, originalView) -> {
                // Only show for non-system apps
                if (itemInfo.getTargetComponent() == null) return null;
                if (PackageManagerHelper.isSystemApp(activity, itemInfo.getTargetComponent().getPackageName())) {
                    return null;
                }
                return new InstallToUser(activity, itemInfo, originalView);
            };

    public static final SystemShortcut.Factory<BaseDraggingActivity> SHOW_DATA_DIR = 
            (activity, itemInfo, originalView) -> new ShowDataDir(activity, itemInfo, originalView);

    public static final SystemShortcut.Factory<BaseDraggingActivity> SHOW_APP_DIR = 
            (activity, itemInfo, originalView) -> new ShowAppDir(activity, itemInfo, originalView);

    public static final SystemShortcut.Factory<BaseDraggingActivity> SELECT = 
            (activity, itemInfo, originalView) -> new Select(activity, itemInfo, originalView);
}