/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.text.BidiFormatter;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import java.io.*;
import java.net.*;
import java.util.*;
import static java.lang.System.out;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

/**
 * {@link PreferenceControllerMixin} that updates MAC/IP address.
 */
public class WifiInfoPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {

    private static final String TAG = "WifiInfoPreferenceController";

    private static final String KEY_CURRENT_IP_ADDRESS = "current_ip_address";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    public String MACAddress = null;

    private final IntentFilter mFilter;
    private final WifiManager mWifiManager;

    private Preference mWifiMacAddressPref;
    private Preference mWifiIpAddressPref;

    public WifiInfoPreferenceController(Context context, Lifecycle lifecycle,
            WifiManager wifiManager) {
        super(context);
        mWifiManager = wifiManager;
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        lifecycle.addObserver(this);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        // Returns null because this controller contains more than 1 preference.
        return null;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mWifiMacAddressPref = screen.findPreference(KEY_MAC_ADDRESS);
        mWifiMacAddressPref.setSelectable(false);
        mWifiIpAddressPref = screen.findPreference(KEY_CURRENT_IP_ADDRESS);
        mWifiIpAddressPref.setSelectable(false);
    }

    @Override
    public void onResume() {
        mContext.registerReceiver(mReceiver, mFilter);
        updateWifiInfo();
    }

    @Override
    public void onPause() {
        mContext.unregisterReceiver(mReceiver);
    }

    public void updateWifiInfo() {
        if (mWifiMacAddressPref != null) {
            final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

       AsyncCaller info = new AsyncCaller();
                info.execute();
                try {

                        info.get();
                }
                catch(Exception e) {

                        Log.e(TAG, "Exception is " + e.getMessage());

                        e.printStackTrace();
                }



            final String macAddress = MACAddress ;

  Log.e(TAG, "mac address is  " + macAddress);

            mWifiMacAddressPref.setSummary(!TextUtils.isEmpty(macAddress)
                    ? macAddress
                    : mContext.getString(R.string.status_unavailable));
        }
        if (mWifiIpAddressPref != null) {
            final String ipAddress = Utils.getWifiIpAddresses(mContext);
            mWifiIpAddressPref.setSummary(ipAddress == null
                    ? mContext.getString(R.string.status_unavailable)
                    : BidiFormatter.getInstance().unicodeWrap(ipAddress));
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION) ||
                    action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                updateWifiInfo();
            }
        }
    };
      private class AsyncCaller extends AsyncTask<Void, Void, Void>
        {

                @Override
                        protected void onPreExecute() {
                                super.onPreExecute();
                        }

                @Override
                        protected Void doInBackground(Void... params) {
                                InetAddress ip;
                                try {

                                        Enumeration<NetworkInterface> nie = NetworkInterface.getNetworkInterfaces();
                                        for (NetworkInterface netint : Collections.list(nie)) {
                                                Log.e(TAG,"Interface name is : " + netint.getDisplayName());
                                                Log.e(TAG,"Interface name is : " + netint.getName());

                                                if ((netint.getName().compareTo("wlan0") == 0) ||
                                                    (netint.getName().compareTo("mlan0") == 0))
                                                {

                                                        byte[] mac = netint.getHardwareAddress();
                                                        StringBuilder sb = new StringBuilder();

                                                        if (mac == null)
                                                        {
                                                                Log.e(TAG,"mac is null");
                                                                return null;
                                                        }
                                                        for (int i = 0; i < mac.length; i++) {
                                                                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                                                        }

                                                        MACAddress = sb.toString();
                                                        break;
                                                }

                                        }

                                } catch (Exception e) {

                                    Log.e(TAG, "Exception is " + e.getMessage());

                                    e.printStackTrace();
                             }
        return null;
        }

        @Override
                protected void onPostExecute(Void result) {
                }
        //this method will be running on UI thread
   }
}
