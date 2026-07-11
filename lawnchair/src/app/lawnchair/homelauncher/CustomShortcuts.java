package app.lawnchair.homelauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.os.UserHandle;
import android.view.View;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.popup.PopupDataProvider;
import com.android.launcher3.popup.SystemShortcut;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CustomShortcuts {

    public static Stream<SystemShortcut.Factory<?>> getShortcutStream(Context context) {
        List<SystemShortcut.Factory<?>> factories = new ArrayList<>();

        factories.add(itemInfo -> {
            if (!(itemInfo instanceof AppInfo)) return null;
            AppInfo app = (AppInfo) itemInfo;
            String pkg = app.componentName.getPackageName();
            UserHandle user = app.user;
            LawnchairProfileManager pm = LawnchairProfileManager.getInstance(context);
            return new SystemShortcut.AppInfo(R.drawable.ic_info_no_shadow, R.string.hl_advanced_permissions, context, app.componentName, user) {
                @Override public void onClicked(View view, Launcher launcher) {
                    pm.openAdvancedPermissions(launcher, pkg);
                    AbstractFloatingView.closeAllOpenViews(launcher);
                }
            };
        });

        factories.add(itemInfo -> {
            if (!(itemInfo instanceof AppInfo)) return null;
            AppInfo app = (AppInfo) itemInfo;
            String pkg = app.componentName.getPackageName();
            LawnchairProfileManager pm = LawnchairProfileManager.getInstance(context);
            return new SystemShortcut.AppInfo(R.drawable.ic_share, R.string.hl_share_app, context, app.componentName, app.user) {
                @Override public void onClicked(View view, Launcher launcher) {
                    pm.shareApp(launcher, pkg);
                    AbstractFloatingView.closeAllOpenViews(launcher);
                }
            };
        });

        factories.add(itemInfo -> {
            if (!(itemInfo instanceof AppInfo)) return null;
            AppInfo app = (AppInfo) itemInfo;
            String pkg = app.componentName.getPackageName();
            LawnchairProfileManager pm = LawnchairProfileManager.getInstance(context);
            return new SystemShortcut.AppInfo(R.drawable.ic_install_no_shadow, R.string.hl_install_to_user, context, app.componentName, app.user) {
                @Override public void onClicked(View view, Launcher launcher) {
                    pm.showUserSelectionDialog(launcher, pkg);
                    AbstractFloatingView.closeAllOpenViews(launcher);
                }
            };
        });

        factories.add(itemInfo -> {
            if (!(itemInfo instanceof AppInfo)) return null;
            AppInfo app = (AppInfo) itemInfo;
            String pkg = app.componentName.getPackageName();
            LawnchairProfileManager pm = LawnchairProfileManager.getInstance(context);
            return new SystemShortcut.AppInfo(R.drawable.ic_folder, R.string.hl_show_data_dir, context, app.componentName, app.user) {
                @Override public void onClicked(View view, Launcher launcher) {
                    pm.showDataDirectory(launcher, pkg);
                    AbstractFloatingView.closeAllOpenViews(launcher);
                }
            };
        });

        factories.add(itemInfo -> {
            if (!(itemInfo instanceof AppInfo)) return null;
            AppInfo app = (AppInfo) itemInfo;
            String pkg = app.componentName.getPackageName();
            LawnchairProfileManager pm = LawnchairProfileManager.getInstance(context);
            return new SystemShortcut.AppInfo(R.drawable.ic_folder, R.string.hl_show_app_dir, context, app.componentName, app.user) {
                @Override public void onClicked(View view, Launcher launcher) {
                    pm.showAppDirectory(launcher, pkg);
                    AbstractFloatingView.closeAllOpenViews(launcher);
                }
            };
        });

        factories.add(itemInfo -> {
            if (!(itemInfo instanceof AppInfo)) return null;
            AppInfo app = (AppInfo) itemInfo;
            return new SystemShortcut.AppInfo(R.drawable.ic_check_box, R.string.hl_select, context, app.componentName, app.user) {
                @Override public void onClicked(View view, Launcher launcher) {
                    AllAppsSelectionManager.getInstance(launcher).startSelection(app);
                    AbstractFloatingView.closeAllOpenViews(launcher);
                }
            };
        });

        return factories.stream();
    }

    public static List<SystemShortcut<?>> getCustomShortcuts(Context context, ItemInfo itemInfo) {
        List<SystemShortcut<?>> shortcuts = new ArrayList<>();
        if (!(itemInfo instanceof AppInfo)) return shortcuts;
        AppInfo appInfo = (AppInfo) itemInfo;
        String pkg = appInfo.componentName.getPackageName();
        UserHandle user = appInfo.user;
        LawnchairProfileManager pm = LawnchairProfileManager.getInstance(context);
        shortcuts.add(new SystemShortcut.AppInfo(R.drawable.ic_info_no_shadow, R.string.hl_advanced_permissions, context, appInfo.componentName, user) {
            @Override public void onClicked(View view, Launcher launcher) { pm.openAdvancedPermissions(launcher, pkg); AbstractFloatingView.closeAllOpenViews(launcher); }
        });
        shortcuts.add(new SystemShortcut.AppInfo(R.drawable.ic_share, R.string.hl_share_app, context, appInfo.componentName, user) {
            @Override public void onClicked(View view, Launcher launcher) { pm.shareApp(launcher, pkg); AbstractFloatingView.closeAllOpenViews(launcher); }
        });
        shortcuts.add(new SystemShortcut.AppInfo(R.drawable.ic_install_no_shadow, R.string.hl_install_to_user, context, appInfo.componentName, user) {
            @Override public void onClicked(View view, Launcher launcher) { pm.showUserSelectionDialog(launcher, pkg); AbstractFloatingView.closeAllOpenViews(launcher); }
        });
        shortcuts.add(new SystemShortcut.AppInfo(R.drawable.ic_folder, R.string.hl_show_data_dir, context, appInfo.componentName, user) {
            @Override public void onClicked(View view, Launcher launcher) { pm.showDataDirectory(launcher, pkg); AbstractFloatingView.closeAllOpenViews(launcher); }
        });
        shortcuts.add(new SystemShortcut.AppInfo(R.drawable.ic_folder, R.string.hl_show_app_dir, context, appInfo.componentName, user) {
            @Override public void onClicked(View view, Launcher launcher) { pm.showAppDirectory(launcher, pkg); AbstractFloatingView.closeAllOpenViews(launcher); }
        });
        shortcuts.add(new SystemShortcut.AppInfo(R.drawable.ic_check_box, R.string.hl_select, context, appInfo.componentName, user) {
            @Override public void onClicked(View view, Launcher launcher) { AllAppsSelectionManager.getInstance(launcher).startSelection(appInfo); AbstractFloatingView.closeAllOpenViews(launcher); }
        });
        return shortcuts;
    }
}
