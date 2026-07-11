package app.lawnchair.homelauncher;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.FolderInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.android.launcher3.util.ItemInfoMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages folder creation in the app drawer and desktop.
 * Supports grouping apps into named folders with drag-and-drop.
 */
public class AllAppsFolderManager {

    private final Context context;
    private final Map<Long, FolderInfo> folderMap = new HashMap<>();
    private FolderInfo currentFolder;

    public AllAppsFolderManager(Context context) {
        this.context = context;
    }

    public void createFolderFromSelection(Launcher launcher, List<AppInfo> selectedApps) {
        EditText input = new EditText(context);
        input.setHint("Folder name");
        input.setSingleLine(true);
        new AlertDialog.Builder(context)
            .setTitle("Create Folder")
            .setView(input)
            .setPositiveButton("Create", (dialog, which) -> {
                String folderName = input.getText().toString().trim();
                if (folderName.isEmpty()) folderName = "New Folder";
                createFolderOnDesktop(launcher, folderName, selectedApps);
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void createFolderOnDesktop(Launcher launcher, String name, List<AppInfo> apps) {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = name;
        folderInfo.contents = new ArrayList<>();
        for (AppInfo app : apps) {
            WorkspaceItemInfo item = new WorkspaceItemInfo(app);
            item.title = app.title;
            folderInfo.contents.add(item);
        }
        launcher.getWorkspace().addFolder(folderInfo);
        Toast.makeText(context, "Folder \"" + name + "\" created", Toast.LENGTH_SHORT).show();
    }

    public void addToFolder(Launcher launcher, AppInfo app, FolderInfo folder) {
        WorkspaceItemInfo item = new WorkspaceItemInfo(app);
        folder.contents.add(item);
        launcher.getWorkspace().updateFolderIcon(folder);
    }

    public void setCurrentFolder(FolderInfo folder) { this.currentFolder = folder; }
    public FolderInfo getCurrentFolder() { return currentFolder; }
}
