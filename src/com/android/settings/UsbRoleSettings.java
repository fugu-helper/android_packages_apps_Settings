/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.os.Bundle;
import android.os.UserManager;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.instrumentation.Instrumentable;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import android.util.Log;

public class UsbRoleSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.usb_role_settings);
        final ListPreference usbRole = (ListPreference) findPreference("USB_ROLE");
        usbRole.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
           public boolean onPreferenceChange(Preference preference, Object newValue) {
               SystemProperties.set("sys.usb.role", newValue.toString());
               Log.i("UsbRoleSettings", newValue.toString());
               return true;
           }
         });
    }

    @Override
    public int getMetricsCategory() {
        return Instrumentable.METRICS_CATEGORY_UNKNOWN;
    }
}

