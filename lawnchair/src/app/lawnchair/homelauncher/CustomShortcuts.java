package app.lawnchair.homelauncher;

import com.android.launcher3.Launcher;
import com.android.launcher3.popup.SystemShortcut;

import java.util.Collections;
import java.util.List;

public class CustomShortcuts {

    public static List<SystemShortcut.Factory<Launcher>> getFactoryList(Launcher launcher) {
        return Collections.emptyList();
    }
}
