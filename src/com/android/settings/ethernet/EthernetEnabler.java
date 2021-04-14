/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.settings.ethernet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.CompoundButton;
import android.net.NetworkInfo;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.net.EthernetInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.EthernetManager;
import java.util.List;


public class EthernetEnabler implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "EthernetEnabler";
    private final Context mContext;
    private SwitchPreference mSwitch;
    private int switchOn = 5;
    private int switchOff = 6;

    private final EthernetManager mEthernetManager;

    private final EthernetManager.Listener mEthernetListener =
	    new EthernetManager.Listener() {
        @Override
        public void onAvailabilityChanged(boolean isAvailable) {
            setSwitchChecked(isAvailable);
        }
    };

    private void setSwitchChecked(boolean checked) {
        if (checked != mSwitch.isChecked()) {
            mSwitch.setChecked(checked);
        }
    }

    public EthernetEnabler(Context context, SwitchPreference switch_) {
        mContext = context;
        mSwitch = switch_;

        mEthernetManager = (EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);

        EthernetInfo info = mEthernetManager.getEthernetInfo();
        final boolean isEnabled;
        if (info.getDetailedState() == DetailedState.CONNECTED)
                isEnabled = true;
        else
                isEnabled = false;

        mSwitch.setChecked(isEnabled);
        mSwitch.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final boolean enabled = (Boolean) value;
        setSwitchChecked(enabled);

        Log.d(TAG,"onPreferenceChange enabled = " + enabled);

        if (enabled) {
	    mEthernetManager.reconnect();
        } else {
            mEthernetManager.teardown();
        }

        return true;
    }

    public void resume() {
	mEthernetManager.addListener(mEthernetListener);
        mSwitch.setOnPreferenceChangeListener(this);
    }

    public void pause() {
        mEthernetManager.removeListener(mEthernetListener);
        mSwitch.setOnPreferenceChangeListener(null);
    }
}
