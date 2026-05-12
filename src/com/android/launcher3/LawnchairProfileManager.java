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
package com.android.launcher3;

import android.content.Context;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.allapps.UserGroupAdapter;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.workspace.ProfileFolderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main entry point for managing profile-based features in Lawnchair.
 * Combines user grouping, work tab support, and desktop profile folders.
 */
public class LawnchairProfileManager {

    private static LawnchairProfileManager sInstance;
    
    private final Context mContext;
    private final UserManager mUserManager;
    private final UserGroupAdapter mUserGroupAdapter;
    private final ProfileFolderManager mProfileFolderManager;
    
    // Map to store apps by user
    private final Map<Integer, List<AppInfo>> mAppsByUser = new HashMap<>();

    public interface OnProfileChangeListener {
        void onProfileAdded(UserHandle user);
        void onProfileRemoved(int userId);
        void onDefaultProfileChanged(int userId);
    }

    private final List<OnProfileChangeListener> mListeners = new ArrayList<>();

    private LawnchairProfileManager(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mUserManager = context.getSystemService(Context.USER_SERVICE);
        mUserGroupAdapter = new UserGroupAdapter(context);
        mProfileFolderManager = new ProfileFolderManager(context);
    }

    /**
     * Gets the singleton instance.
     */
    @NonNull
    public static LawnchairProfileManager getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new LawnchairProfileManager(context);
        }
        return sInstance;
    }

    /**
     * Registers a listener for profile changes.
     */
    public void addOnProfileChangeListener(OnProfileChangeListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Unregisters a profile change listener.
     */
    public void removeOnProfileChangeListener(OnProfileChangeListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Gets the user group adapter for app grouping.
     */
    @NonNull
    public UserGroupAdapter getUserGroupAdapter() {
        return mUserGroupAdapter;
    }

    /**
     * Gets the profile folder manager for desktop folders.
     */
    @NonNull
    public ProfileFolderManager getProfileFolderManager() {
        return mProfileFolderManager;
    }

    /**
     * Gets all user profiles on the device.
     */
    @NonNull
    public List<UserHandle> getAllProfiles() {
        List<UserHandle> profiles = new ArrayList<>();
        
        if (mUserManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            profiles.addAll(mUserManager.getUserProfiles());
        }
        
        return profiles;
    }

    /**
     * Gets the primary user handle.
     */
    @Nullable
    public UserHandle getPrimaryUser() {
        List<UserHandle> profiles = getAllProfiles();
        return profiles.isEmpty() ? null : profiles.get(0);
    }

    /**
     * Gets secondary (work) profiles.
     */
    @NonNull
    public List<UserHandle> getSecondaryProfiles() {
        List<UserHandle> allProfiles = getAllProfiles();
        List<UserHandle> secondary = new ArrayList<>();
        
        for (int i = 1; i < allProfiles.size(); i++) {
            secondary.add(allProfiles.get(i));
        }
        
        return secondary;
    }

    /**
     * Checks if a user is the primary user.
     */
    public boolean isPrimaryUser(@NonNull UserHandle user) {
        UserHandle primary = getPrimaryUser();
        return primary != null && primary.equals(user);
    }

    /**
     * Gets the display name for a user profile.
     */
    @NonNull
    public String getProfileDisplayName(@NonNull UserHandle user) {
        int userId = user.hashCode();
        
        if (isPrimaryUser(user)) {
            return mContext.getString(R.string.personal_apps);
        }
        
        if (mUserManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            long serial = mUserManager.getSerialNumberForUser(user);
            return mContext.getString(R.string.user_format, serial);
        }
        
        return mContext.getString(R.string.user_format, userId);
    }

    /**
     * Gets the number of users on the device.
     */
    public int getUserCount() {
        return getAllProfiles().size();
    }

    /**
     * Checks if there are multiple users on the device.
     */
    public boolean hasMultipleUsers() {
        return getUserCount() > 1;
    }

    /**
     * Groups apps by user profile.
     */
    @NonNull
    public List<UserGroupAdapter.UserGroup> groupAppsByUser(@NonNull List<AppInfo> apps) {
        return mUserGroupAdapter.groupAppsByUser(apps);
    }

    /**
     * Gets the user ID from a UserHandle.
     */
    public int getUserId(@NonNull UserHandle user) {
        return user.hashCode();
    }

    /**
     * Gets the UserHandle for a user ID.
     */
    @Nullable
    public UserHandle getUserForId(int userId) {
        List<UserHandle> profiles = getAllProfiles();
        for (UserHandle profile : profiles) {
            if (profile.hashCode() == userId) {
                return profile;
            }
        }
        return null;
    }
}