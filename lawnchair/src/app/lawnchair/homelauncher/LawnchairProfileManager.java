package app.lawnchair.homelauncher;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.pm.UserCache;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.PackageManagerHelper;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LawnchairProfileManager {

    private static final String TAG = "HLProfileManager";
    private static LawnchairProfileManager instance;
    private final Context context;
    private final UserManager userManager;
    private final LauncherApps launcherApps;
    private final Map<String, List<AppInfo>> userAppCache = new HashMap<>();
    private boolean cacheDirty = true;

    private LawnchairProfileManager(Context context) {
        this.context = context.getApplicationContext();
        this.userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    public static synchronized LawnchairProfileManager getInstance(Context context) {
        if (instance == null) instance = new LawnchairProfileManager(context);
        return instance;
    }

    public List<UserHandle> getAllUserProfiles() { return userManager.getUserProfiles(); }
    public UserHandle getCurrentUser() { return Process.myUserHandle(); }

    public String getUserLabel(UserHandle user) {
        if (user.equals(getCurrentUser())) return "Personal";
        long serial = userManager.getSerialNumberForUser(user);
        return "User " + serial;
    }

    public boolean isWorkProfile(UserHandle user) { return !user.equals(getCurrentUser()); }

    public List<AppInfo> getAppsForUser(UserHandle user) {
        List<AppInfo> apps = new ArrayList<>();
        for (android.content.pm.LauncherActivityInfo info : launcherApps.getActivityList(null, user)) {
            AppInfo appInfo = new AppInfo(context, info, user);
            apps.add(appInfo);
        }
        Collections.sort(apps, (a, b) -> a.title.toString().toLowerCase().compareTo(b.title.toString().toLowerCase()));
        return apps;
    }

    public Map<String, List<AppInfo>> getAllAppsGroupedByUser() {
        if (!cacheDirty && !userAppCache.isEmpty()) return userAppCache;
        userAppCache.clear();
        for (UserHandle user : getAllUserProfiles()) {
            userAppCache.put(getUserLabel(user), getAppsForUser(user));
        }
        cacheDirty = false;
        return userAppCache;
    }

    public void invalidateCache() { cacheDirty = true; userAppCache.clear(); }

    public void createProfileFolderOnDesktop(Launcher launcher, UserHandle user) {
        ProfileFolderManager.createProfileFolder(launcher, user, getUserLabel(user));
    }

    public void openAdvancedPermissions(Context ctx, String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } catch (Exception e) { Toast.makeText(ctx, "Cannot open settings", Toast.LENGTH_SHORT).show(); }
    }

    public void shareApp(Context ctx, String packageName) {
        try {
            ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(packageName, 0);
            File apkFile = new File(ai.sourceDir);
            if (apkFile.exists()) {
                Uri apkUri = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", apkFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/vnd.android.package-archive");
                shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ctx.startActivity(Intent.createChooser(shareIntent, "Share " + packageName));
                return;
            }
        } catch (Exception e) { Log.e(TAG, "Cannot share APK", e); }
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + packageName);
            ctx.startActivity(Intent.createChooser(shareIntent, "Share " + packageName));
        } catch (Exception e2) { Toast.makeText(ctx, "Cannot share app", Toast.LENGTH_SHORT).show(); }
    }

    public void installToUser(Context ctx, String packageName, UserHandle targetUser) {
        if (!Shell.getShell().isRoot) { Toast.makeText(ctx, "Root access required", Toast.LENGTH_SHORT).show(); return; }
        try {
            int userId = userManager.getSerialNumberForUser(targetUser);
            Shell.su("pm install-existing --user " + userId + " " + packageName).submit(result -> {
                if (result.isSuccess()) {
                    new android.os.Handler(ctx.getMainLooper()).post(() ->
                        Toast.makeText(ctx, "Installed to " + getUserLabel(targetUser), Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) { Toast.makeText(ctx, "Install failed", Toast.LENGTH_SHORT).show(); }
    }

    public void showUserSelectionDialog(Context ctx, String packageName) {
        List<UserHandle> users = getAllUserProfiles();
        UserHandle currentUser = getCurrentUser();
        List<UserHandle> targets = new ArrayList<>();
        for (UserHandle u : users) if (!u.equals(currentUser)) targets.add(u);
        if (targets.isEmpty()) { Toast.makeText(ctx, "No other users found", Toast.LENGTH_SHORT).show(); return; }
        String[] userLabels = new String[targets.size()];
        for (int i = 0; i < targets.size(); i++) userLabels[i] = getUserLabel(targets.get(i));
        new androidx.appcompat.app.AlertDialog.Builder(ctx)
            .setTitle("Install to User")
            .setItems(userLabels, (dialog, which) -> installToUser(ctx, packageName, targets.get(which)))
            .setNegativeButton(android.R.string.cancel, null).show();
    }

    public void showDataDirectory(Context ctx, String packageName) {
        openWithRootExplorer(ctx, "/data/data/" + packageName);
    }

    public void showAppDirectory(Context ctx, String packageName) {
        try {
            ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(packageName, 0);
            String appDir = new File(ai.sourceDir).getParent();
            openWithRootExplorer(ctx, appDir != null ? appDir : "/data/app/");
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(ctx, "App not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWithRootExplorer(Context ctx, String path) {
        String[][] explorers = {
            {"com.mixplorer", "com.mixplorer.activities.BrowseActivity"},
            {"com.mixplorer.silver", "com.mixplorer.activities.BrowseActivity"},
            {"bin.mt.plus", "bin.mt.plus.Main"},
            {"bin.mt.plus.canary", "bin.mt.plus.Main"},
            {"com.topjohnwu.magisk", "com.topjohnwu.magisk.ui.MainActivity"},
        };
        for (String[] explorer : explorers) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(explorer[0], explorer[1]);
                intent.setData(Uri.parse(path));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
                return;
            } catch (Exception e) {}
        }
        Toast.makeText(ctx, "No root explorer found. Install Mixplorer or MT Manager.", Toast.LENGTH_LONG).show();
    }

    public void loadAsync() {
        new Thread(() -> getAllAppsGroupedByUser()).start();
    }

    public void registerPackageListener(Context ctx) {
        MultiUserAppsHelper.getInstance(ctx).registerPackageListener(ctx);
    }
}
