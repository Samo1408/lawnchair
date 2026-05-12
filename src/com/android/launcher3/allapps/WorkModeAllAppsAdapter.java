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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;
import com.android.launcher3.model.data.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter for the Work tab that groups apps by user profile.
 * Each user group is displayed with a header and the apps below it.
 */
public class WorkModeAllAppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_APP = 1;

    private final Context mContext;
    private final AllAppsGridAdapter.LayoutConfiguration mLayoutConfig;
    private final AlphabeticalAppsList mApps;
    private final UserGroupAdapter mUserGroupAdapter;
    
    // The flattened list combining headers and apps
    private final List<Object> mItems = new ArrayList<>();
    
    // Reference to the main adapter for app views
    private AllAppsGridAdapter mMainAdapter;

    public interface OnAppClickListener {
        void onAppClick(View view, AppInfo appInfo);
    }
    
    private OnAppClickListener mClickListener;

    public WorkModeAllAppsAdapter(Context context, AlphabeticalAppsList apps, 
                                  UserGroupAdapter userGroupAdapter,
                                  AllAppsGridAdapter.LayoutConfiguration layoutConfig) {
        mContext = context;
        mApps = apps;
        mUserGroupAdapter = userGroupAdapter;
        mLayoutConfig = layoutConfig;
    }

    public void setOnAppClickListener(OnAppClickListener listener) {
        mClickListener = listener;
    }

    public void setMainAdapter(AllAppsGridAdapter adapter) {
        mMainAdapter = adapter;
    }

    /**
     * Updates the app list and regroups them by user.
     */
    public void updateApps(AlphabeticalAppsList apps) {
        mItems.clear();
        
        // Get work profile apps
        List<UserGroupAdapter.UserGroup> userGroups = mUserGroupAdapter.groupAppsByUser(apps.data);
        
        // Add headers and apps to the flattened list
        for (UserGroupAdapter.UserGroup group : userGroups) {
            mItems.add(group); // Header (UserGroup)
            mItems.addAll(group.apps); // Apps
        }
        
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = mItems.get(position);
        if (item instanceof UserGroupAdapter.UserGroup) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_APP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.user_group_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            // Delegate to main adapter
            return mMainAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        
        if (viewType == VIEW_TYPE_HEADER) {
            UserGroupAdapter.UserGroup group = (UserGroupAdapter.UserGroup) mItems.get(position);
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.bind(group);
        } else {
            AppInfo app = (AppInfo) mItems.get(position);
            if (mMainAdapter != null) {
                mMainAdapter.onBindViewHolder(holder, app);
            }
            
            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                if (mClickListener != null) {
                    mClickListener.onAppClick(v, app);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Gets the app at a given position.
     */
    public AppInfo getAppAt(int position) {
        Object item = mItems.get(position);
        if (item instanceof AppInfo) {
            return (AppInfo) item;
        }
        return null;
    }

    /**
     * ViewHolder for user group headers.
     */
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitleView;
        private final TextView mCountView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.user_group_title);
            mCountView = itemView.findViewById(R.id.user_group_count);
        }

        public void bind(UserGroupAdapter.UserGroup group) {
            mTitleView.setText(group.getFolderName());
            mCountView.setText(String.valueOf(group.apps.size()) + " apps");
        }
    }
}