package app.lawnchair.homelauncher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.model.data.AppInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages multi-select mode in the app drawer.
 */
public class AllAppsSelectionManager {

    private static AllAppsSelectionManager instance;
    private final Context context;
    private final Set<AppInfo> selectedApps = new HashSet<>();
    private boolean selectionMode = false;
    private SelectionListener listener;

    public interface SelectionListener {
        void onSelectionChanged(int count);
        void onSelectionModeChanged(boolean active);
    }

    private AllAppsSelectionManager(Context context) {
        this.context = context;
    }

    public static synchronized AllAppsSelectionManager getInstance(Context context) {
        if (instance == null) instance = new AllAppsSelectionManager(context.getApplicationContext());
        return instance;
    }

    public void setListener(SelectionListener listener) { this.listener = listener; }

    public void startSelection(AppInfo initialApp) {
        selectionMode = true;
        selectedApps.clear();
        selectedApps.add(initialApp);
        if (listener != null) { listener.onSelectionModeChanged(true); listener.onSelectionChanged(selectedApps.size()); }
    }

    public void toggleSelection(AppInfo app) {
        if (!selectionMode) { startSelection(app); return; }
        if (selectedApps.contains(app)) selectedApps.remove(app);
        else selectedApps.add(app);
        if (selectedApps.isEmpty()) exitSelectionMode();
        else if (listener != null) listener.onSelectionChanged(selectedApps.size());
    }

    public boolean isSelected(AppInfo app) { return selectedApps.contains(app); }
    public boolean isSelectionMode() { return selectionMode; }
    public int getSelectionCount() { return selectedApps.size(); }
    public List<AppInfo> getSelectedApps() { return new ArrayList<>(selectedApps); }

    public void applySelectionHighlight(BubbleTextView icon, boolean selected) {
        if (selected) icon.setColorFilter(Color.argb(80, 79, 195, 247), PorterDuff.Mode.SRC_ATOP);
        else icon.clearColorFilter();
    }

    public void createFolderFromSelection(Launcher launcher) {
        if (selectedApps.isEmpty()) return;
        new AllAppsFolderManager(context).createFolderFromSelection(launcher, new ArrayList<>(selectedApps));
        exitSelectionMode();
    }

    public void batchUninstall(Launcher launcher) {
        if (selectedApps.isEmpty()) return;
        new androidx.appcompat.app.AlertDialog.Builder(launcher)
            .setTitle("Uninstall " + selectedApps.size() + " apps?")
            .setMessage("This will uninstall the selected apps.")
            .setPositiveButton("Uninstall", (dialog, which) -> {
                for (AppInfo app : selectedApps) {
                    launcher.getPackageManager().getPackageInstaller()
                        .uninstall(app.componentName.getPackageName(), null, 0, null, null);
                }
                Toast.makeText(context, "Uninstalling...", Toast.LENGTH_SHORT).show();
                exitSelectionMode();
            })
            .setNegativeButton(android.R.string.cancel, null).show();
    }

    public void exitSelectionMode() {
        selectionMode = false; selectedApps.clear();
        if (listener != null) listener.onSelectionModeChanged(false);
    }

    public void inflateMenu(Menu menu) {
        menu.clear();
        menu.add(0, R.id.action_create_folder, 0, R.string.hl_action_create_folder).setIcon(R.drawable.ic_folder).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, R.id.action_uninstall, 0, "Uninstall (" + selectedApps.size() + ")").setIcon(R.drawable.ic_uninstall_no_shadow).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, R.id.action_select_all, 0, "Close").setIcon(R.drawable.ic_check_box).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    public boolean onMenuItemClick(Launcher launcher, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_create_folder) { createFolderFromSelection(launcher); return true; }
        else if (id == R.id.action_uninstall) { batchUninstall(launcher); return true; }
        else if (id == R.id.action_select_all) { exitSelectionMode(); return true; }
        return false;
    }
}
