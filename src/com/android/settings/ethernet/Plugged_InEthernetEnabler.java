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
 
 /* This class is used to enable/disable the Pluggedin ethernet. It communicates with Ethernet Manager to enable/disable Pluggedin ethernet.*/

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


public class Plugged_InEthernetEnabler implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "Plugged_InEthernetEnabler";
    private final Context mContext;
    private SwitchPreference mplugSwitch;
    private int switchOn = 5;
    private int switchOff = 6;

    private final EthernetManager mEthernetManager;

    private final EthernetManager.Listener mEth1Listeners =
        new EthernetManager.Listener() {
        @Override
        public void onAvailabilityChanged(boolean isPluggedInEthAvailable) {
            Log.d(TAG, "ETH1 MSG_AVAILABILITY_CHANGED_ETH1 isPluggedInEthAvailable" + isPluggedInEthAvailable);
            setSwitchChecked(isPluggedInEthAvailable);
        }
    };

    private void setSwitchChecked(boolean checked) {
        if (checked != mplugSwitch.isChecked()) {
            mplugSwitch.setChecked(checked);
        }
    }

    public Plugged_InEthernetEnabler(Context context, SwitchPreference switch_) {
        mContext = context;
        mplugSwitch = switch_;

        mEthernetManager = (EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);

        EthernetInfo info = mEthernetManager.getPluggedInEthernetInfo();
        final boolean isEnabled;
        if (info.getDetailedState() == DetailedState.CONNECTED)
            isEnabled = true;
        else
            isEnabled = false;

        mplugSwitch.setChecked(isEnabled);
        mplugSwitch.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final boolean enabled = (Boolean) value;
        setSwitchChecked(enabled);

        Log.d(TAG,"onPreferenceChange enabled = " + enabled);

        if (enabled) {
            mEthernetManager.connectPluggedinEth();
        } else {
            mEthernetManager.teardownPluggedinEth();
        }

        return true;
    }

    public void resume() {
        mEthernetManager.addPluggedinEthListener(mEth1Listeners);
        mplugSwitch.setOnPreferenceChangeListener(this);
    }

    public void pause() {
        mEthernetManager.removePluggedinEthListener(mEth1Listeners);
        mplugSwitch.setOnPreferenceChangeListener(null);
    }
}
