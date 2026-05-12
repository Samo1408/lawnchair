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
package com.android.launcher3.workspace;

import android.content.Context;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.model.data.FolderInfo;
import com.android.launcher3.model.data.ItemInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages profile folders on the desktop.
 * Each profile gets a folder on the desktop containing all apps for that profile.
 */
public class ProfileFolderManager {

    public interface OnProfileFolderListener {
        void onProfileFoldersCreated(List<FolderInfo> folders);
        void onProfileFolderUpdated(FolderInfo folder);
    }

    private final Context mContext;
    private final UserManager mUserManager;
    private final Map<Integer, FolderInfo> mProfileFolders = new HashMap<>();
    private OnProfileFolderListener mListener;

    public ProfileFolderManager(@NonNull Context context) {
        mContext = context;
        mUserManager = context.getSystemService(Context.USER_SERVICE);
    }

    public void setOnProfileFolderListener(OnProfileFolderListener listener) {
        mListener = listener;
    }

    /**
     * Creates or updates profile folders on the desktop.
     */
    public void updateProfileFolders() {
        List<UserHandle> profiles = getUserProfiles();
        List<AppInfo> allApps = getAllApps();
        
        // Group apps by user
        Map<Integer, List<AppInfo>> appsByUser = groupAppsByUser(allApps);
        
        // Create or update folders for each profile
        for (Map.Entry<Integer, List<AppInfo>> entry : appsByUser.entrySet()) {
            int userId = entry.getKey();
            List<AppInfo> userApps = entry.getValue();
            
            FolderInfo folder = mProfileFolders.get(userId);
            if (folder == null) {
                folder = createProfileFolder(userId, userApps);
                mProfileFolders.put(userId, folder);
            } else {
                updateProfileFolder(folder, userApps);
            }
        }
        
        if (mListener != null) {
            mListener.onProfileFoldersCreated(new ArrayList<>(mProfileFolders.values()));
        }
    }

    /**
     * Gets all user profiles on the device.
     */
    @NonNull
    private List<UserHandle> getUserProfiles() {
        List<UserHandle> profiles = new ArrayList<>();
        
        if (mUserManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            profiles.addAll(mUserManager.getUserProfiles());
        }
        
        return profiles;
    }

    /**
     * Gets all installed apps.
     */
    @NonNull
    private List<AppInfo> getAllApps() {
        List<AppInfo> apps = new ArrayList<>();
        
        try {
            InvariantDeviceProfile idp = LauncherAppState.getInstance(mContext)
                    .getInvariantDeviceProfile();
            
            // This would typically be retrieved from the model
            // For now, we'll return an empty list as a placeholder
            // The actual implementation would query the model for all apps
        } catch (Exception e) {
            // Handle exception
        }
        
        return apps;
    }

    /**
     * Groups apps by user ID.
     */
    @NonNull
    private Map<Integer, List<AppInfo>> groupAppsByUser(@NonNull List<AppInfo> apps) {
        Map<Integer, List<AppInfo>> grouped = new HashMap<>();
        
        for (AppInfo app : apps) {
            int userId = app.user.hashCode();
            grouped.computeIfAbsent(userId, k -> new ArrayList<>()).add(app);
        }
        
        return grouped;
    }

    /**
     * Creates a new profile folder.
     */
    @NonNull
    private FolderInfo createProfileFolder(int userId, @NonNull List<AppInfo> apps) {
        FolderInfo folder = new FolderInfo();
        folder.title = getProfileDisplayName(userId);
        folder.user = apps.isEmpty() ? null : apps.get(0).user;
        
        // Add apps to folder
        for (AppInfo app : apps) {
            folder.add(app);
        }
        
        return folder;
    }

    /**
     * Updates an existing profile folder with new apps.
     */
    private void updateProfileFolder(@NonNull FolderInfo folder, @NonNull List<AppInfo> apps) {
        // Clear existing items
        folder.clear();
        
        // Add new apps
        for (AppInfo app : apps) {
            folder.add(app);
        }
        
        // Update icon if needed
        folder.updateIcon();
        
        if (mListener != null) {
            mListener.onProfileFolderUpdated(folder);
        }
    }

    /**
     * Gets the display name for a profile.
     */
    @NonNull
    private String getProfileDisplayName(int userId) {
        if (userId == 0) {
            return "Personal";
        }
        
        if (mUserManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for (UserHandle profile : mUserManager.getUserProfiles()) {
                if (profile.hashCode() == userId) {
                    long serial = mUserManager.getSerialNumberForUser(profile);
                    return "User " + serial;
                }
            }
        }
        
        return "User " + userId;
    }

    /**
     * Gets all profile folders.
     */
    @NonNull
    public List<FolderInfo> getProfileFolders() {
        return new ArrayList<>(mProfileFolders.values());
    }

    /**
     * Gets a profile folder by user ID.
     */
    @Nullable
    public FolderInfo getProfileFolder(int userId) {
        return mProfileFolders.get(userId);
    }

    /**
     * Removes a profile folder.
     */
    public void removeProfileFolder(int userId) {
        FolderInfo removed = mProfileFolders.remove(userId);
        if (removed != null && mListener != null) {
            // Notify that folder was removed
        }
    }
}