/*
 * Copyright 2022, Lawnchair
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

package app.lawnchair

import android.content.Context
import androidx.annotation.Keep
import androidx.arch.core.util.Function
import app.lawnchair.bugreport.LawnchairBugReporter
import app.lawnchair.homelauncher.HomeLauncherInit
import app.lawnchair.theme.color.ColorTokens
import com.android.launcher3.Utilities
import com.android.launcher3.icons.ThemedIconDrawable
import com.android.quickstep.QuickstepProcessInitializer

@Keep
class LawnchairProcessInitializer(context: Context) : QuickstepProcessInitializer(context) {
    override fun init(context: Context) {
        LawnchairBugReporter.INSTANCE.get(context)
        ThemedIconDrawable.COLORS_LOADER = Function {
            if (Utilities.isDarkTheme(it)) intArrayOf(ColorTokens.Neutral1_800.resolveColor(it), ColorTokens.Accent1_100.resolveColor(it))
            else intArrayOf(ColorTokens.Accent1_100.resolveColor(it), ColorTokens.Neutral2_700.resolveColor(it))
        }
        super.init(context)
        HomeLauncherInit.initialize(context)
    }
}
