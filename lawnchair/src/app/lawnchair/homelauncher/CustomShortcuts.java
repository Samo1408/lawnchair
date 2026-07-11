/*
 * Copyright 2026, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair.homelauncher

import android.view.View

import com.android.launcher3.BaseDraggingActivity
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.popup.SystemShortcut

import java.util.ArrayList
import java.util.List
import java.util.stream.Stream

public class CustomShortcuts {

    public static Stream<SystemShortcut.Factory<Launcher>> getShortcutStream() {
        List<SystemShortcut.Factory<Launcher>> factories = new ArrayList<>()

        factories.add((launcher, itemInfo, originalView) -> {
            return new SystemShortcut<Launcher>(
                R.drawable.ic_info_no_shadow,
                R.string.app_info_drop_target_label,
                launcher, itemInfo, originalView
            ) {
                @Override
                public void onClick(View view) {
                    // TODO: Implement after core build is stable
                }
            };
        })

        return factories.stream()
    }
}
