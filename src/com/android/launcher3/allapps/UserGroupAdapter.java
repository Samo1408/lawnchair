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
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.model.data.AppInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Groups apps by their user profile for the Work tab.
 * Each secondary user's apps are grouped into a folder named after that user.
 */
public class UserGroupAdapter {

    public static final int PERSONAL_USER_ID = 0;
    
    /**
     * Represents a group of apps belonging to a specific user.
     */
    public static class UserGroup {
        public final UserHandle userHandle;
        public final int userId;
        public final String displayName;
        public final List<AppInfo> apps;
        
        public UserGroup(UserHandle userHandle, int userId, String displayName, List<AppInfo> apps) {
            this.userHandle = userHandle;
            this.userId = userId;
            this.displayName = displayName;
            this.apps = apps;
        }
        
        public String getFolderName() {
            return "User " + userId;
        }
    }

    private final Context mContext;
    private final UserManager mUserManager;
    private final List<UserGroup> mUserGroups = new ArrayList<>();
    
    // Map to store user IDs that are work profiles
    private final Map<Integer, UserHandle> mWorkProfiles = new HashMap<>();

    public UserGroupAdapter(@NonNull Context context) {
        mContext = context;
        mUserManager = context.getSystemService(Context.USER_SERVICE);
        initializeWorkProfiles();
    }
    
    private void initializeWorkProfiles() {
        if (mUserManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            List<UserHandle> profiles = mUserManager.getUserProfiles();
            for (UserHandle profile : profiles) {
                int userId = profile.hashCode();
                // Skip the primary user
                if (userId != PERSONAL_USER_ID) {
                    mWorkProfiles.put(userId, profile);
                }
            }
        }
    }

    /**
     * Groups apps by user profile.
     * 
     * @param apps List of apps to group
     * @return List of UserGroup objects, one per user profile with apps
     */
    @NonNull
    public List<UserGroup> groupAppsByUser(@NonNull List<AppInfo> apps) {
        mUserGroups.clear();
        
        // Group apps by user
        Map<Integer, List<AppInfo>> appsByUser = new HashMap<>();
        for (AppInfo app : apps) {
            int userId = app.user.hashCode();
            appsByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(app);
        }
        
        // Create UserGroup for each user with apps
        for (Map.Entry<Integer, List<AppInfo>> entry : appsByUser.entrySet()) {
            int userId = entry.getKey();
            List<AppInfo> userApps = entry.getValue();
            
            UserHandle userHandle = userApps.get(0).user;
            String displayName = getUserDisplayName(userId);
            
            // Sort apps alphabetically
            userApps.sort(Comparator.comparing(a -> a.title.toString(), String.CASE_INSENSITIVE_ORDER));
            
            mUserGroups.add(new UserGroup(userHandle, userId, displayName, userApps));
        }
        
        // Sort user groups by user ID
        mUserGroups.sort(Comparator.comparingInt(g -> g.userId));
        
        return mUserGroups;
    }

    /**
     * Gets the display name for a user.
     */
    @NonNull
    private String getUserDisplayName(int userId) {
        if (userId == PERSONAL_USER_ID) {
            return "Personal";
        }
        
        // Try to get user info from UserManager
        if (mUserManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                // Get the serial number for this user
                long serialNumber = getUserSerial(userId);
                if (serialNumber >= 0) {
                    return "User " + serialNumber;
                }
            } catch (Exception e) {
                // Fallback to hash code
            }
        }
        
        return "User " + userId;
    }
    
    /**
     * Gets the serial number for a user by their hash code.
     */
    private long getUserSerial(int userHashCode) {
        if (mUserManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for (UserHandle profile : mUserManager.getUserProfiles()) {
                if (profile.hashCode() == userHashCode) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        return mUserManager.getSerialNumberForUser(profile);
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Gets all work profile user handles.
     */
    @NonNull
    public List<UserHandle> getWorkProfiles() {
        return new ArrayList<>(mWorkProfiles.values());
    }

    /**
     * Checks if a user is a secondary (work) profile.
     */
    public boolean isWorkProfile(int userId) {
        return userId != PERSONAL_USER_ID;
    }

    /**
     * Gets all user groups.
     */
    @NonNull
    public List<UserGroup> getUserGroups() {
        return mUserGroups;
    }

    /**
     * Gets the total number of apps across all user groups.
     */
    public int getTotalAppCount() {
        int count = 0;
        for (UserGroup group : mUserGroups) {
            count += group.apps.size();
        }
        return count;
    }
}