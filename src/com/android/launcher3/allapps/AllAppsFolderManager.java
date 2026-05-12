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
package com.android.launcher3.allapps;

import android.content.Context;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.FolderInfo;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.provider.LauncherDbUtils;
import com.android.launcher3.util.ItemInfoMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles folder creation and management in the All Apps view.
 * Allows users to select multiple apps and create a folder from them.
 */
public class AllAppsFolderManager {

    public interface FolderCreationListener {
        void onFolderCreated(FolderInfo folderInfo);
        void onFolderCreationFailed(String error);
    }

    private final Context mContext;
    private final Launcher mLauncher;
    private final List<AppInfo> mSelectedApps = new ArrayList<>();
    private boolean mSelectionMode = false;
    private FolderCreationListener mListener;

    public AllAppsFolderManager(@NonNull Context context, @NonNull Launcher launcher) {
        mContext = context;
        mLauncher = launcher;
    }

    public void setFolderCreationListener(FolderCreationListener listener) {
        mListener = listener;
    }

    /**
     * Enters selection mode for creating a folder.
     */
    public void enterSelectionMode() {
        mSelectionMode = true;
        mSelectedApps.clear();
        notifySelectionChanged();
    }

    /**
     * Exits selection mode.
     */
    public void exitSelectionMode() {
        mSelectionMode = false;
        mSelectedApps.clear();
        notifySelectionChanged();
    }

    /**
     * Checks if selection mode is active.
     */
    public boolean isSelectionModeActive() {
        return mSelectionMode;
    }

    /**
     * Toggles selection of an app.
     */
    public void toggleAppSelection(@NonNull AppInfo app) {
        if (!mSelectionMode) return;
        
        if (mSelectedApps.contains(app)) {
            mSelectedApps.remove(app);
        } else {
            mSelectedApps.add(app);
        }
        notifySelectionChanged();
    }

    /**
     * Checks if an app is selected.
     */
    public boolean isAppSelected(@NonNull AppInfo app) {
        return mSelectedApps.contains(app);
    }

    /**
     * Gets the number of selected apps.
     */
    public int getSelectedCount() {
        return mSelectedApps.size();
    }

    /**
     * Gets the list of selected apps.
     */
    @NonNull
    public List<AppInfo> getSelectedApps() {
        return new ArrayList<>(mSelectedApps);
    }

    /**
     * Creates a folder from the selected apps.
     */
    public void createFolderFromSelection(@Nullable String folderName) {
        if (mSelectedApps.size() < 2) {
            if (mListener != null) {
                mListener.onFolderCreationFailed("Select at least 2 apps to create a folder");
            }
            return;
        }

        // Get the first app's position for placing the folder
        AppInfo firstApp = mSelectedApps.get(0);
        
        // Create folder info
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = folderName != null ? folderName : mContext.getString(
                com.android.launcher3.R.string.folder_name_format, mSelectedApps.size());
        
        // Add all selected apps to the folder
        for (AppInfo app : mSelectedApps) {
            folderInfo.add(app);
        }

        // Save to database
        long containerId = LauncherDbUtils.createNewFolder(mContext, folderInfo);
        
        if (containerId > 0) {
            folderInfo.id = containerId;
            
            // Remove apps from all apps list
            LauncherAppState.getInstance(mContext).getModel().removeApps(mSelectedApps);
            
            if (mListener != null) {
                mListener.onFolderCreated(folderInfo);
            }
            
            exitSelectionMode();
        } else {
            if (mListener != null) {
                mListener.onFolderCreationFailed("Failed to create folder");
            }
        }
    }

    /**
     * Unselects all apps.
     */
    public void clearSelection() {
        mSelectedApps.clear();
        notifySelectionChanged();
    }

    private void notifySelectionChanged() {
        // This would typically notify the UI to update selection states
        // The actual implementation depends on how the UI is structured
    }
}