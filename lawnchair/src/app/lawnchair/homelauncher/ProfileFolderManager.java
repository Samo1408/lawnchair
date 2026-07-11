package app.lawnchair.homelauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.R;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.FolderInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;

import java.util.ArrayList;
import java.util.List;

public class ProfileFolderManager {

    public static FolderInfo createProfileFolder(Launcher launcher, UserHandle user, String userLabel) {
        LawnchairProfileManager pm = LawnchairProfileManager.getInstance(launcher);
        List<AppInfo> userApps = pm.getAppsForUser(user);
        if (userApps.isEmpty()) {
            Toast.makeText(launcher, "No apps found for " + userLabel, Toast.LENGTH_SHORT).show();
            return null;
        }
        FolderInfo folder = new FolderInfo();
        folder.title = userLabel;
        folder.contents = new ArrayList<>();
        for (AppInfo app : userApps) {
            WorkspaceItemInfo item = new WorkspaceItemInfo(app);
            item.title = app.title;
            folder.contents.add(item);
        }
        launcher.getWorkspace().addFolder(folder);
        Toast.makeText(launcher, userLabel + " folder created (" + userApps.size() + " apps)", Toast.LENGTH_SHORT).show();
        return folder;
    }

    public static void refreshProfileFolders(Launcher launcher) {
        LauncherModel model = launcher.getModel();
        if (model != null) model.forceReload();
    }

    public static BroadcastReceiver registerRefreshReceiver(Context context, Launcher launcher) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context ctx, Intent intent) { refreshProfileFolders(launcher); }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        context.registerReceiver(receiver, filter);
        return receiver;
    }
}
