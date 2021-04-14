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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.net.LinkAddress;
import android.net.NetworkInfo.DetailedState;
import android.net.ProxyInfo;
import android.net.EthernetInfo;
import android.net.EthernetManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.NetworkInfo;
import android.net.StaticIpConfiguration;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.instrumentation.Instrumentable;
import com.android.settings.SettingsPreferenceFragment;

import android.content.BroadcastReceiver;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.util.List;

public class EthernetSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener,
            DialogInterface.OnClickListener {
    private static final String TAG = "EthernetSettings";
    private static final int ETHERNET_DIALOG_ID = 1;
    private static final String KEY_TOGGLE_ETHERNET = "toggle_ethernet";
    public static final int ETHERNET = 100001;

    private EthernetManager mEthernetManager;
    private EthernetDialog mDialog;
    private TextView mEmptyView;
    private SwitchPreference ethernet_switch;
    private EthernetEnabler mEthernetEnabler;
    private final EthernetManager.Listener mEthernetListener = new EthernetManager.Listener() {
        @Override
        public void onAvailabilityChanged(boolean isAvailable) {
		Log.d(TAG, "on Availability Changed " + isAvailable);
		updatePreferences(isAvailable);
        }
    };

    public EthernetSettings() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // After confirming PreferenceScreen is available, we call super.
        super.onActivityCreated(savedInstanceState);
    }

    public boolean onPreferenceClick(Preference preference) {
        Log.d(TAG, "EthernetSetting onPreferenceClick + " + preference.getKey());
        if (preference.getKey().equals("ethernet_change_settings")) {
            showDialog(ETHERNET_DIALOG_ID);
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ethernet_settings);
        mEthernetManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);
        ethernet_switch = (SwitchPreference) findPreference(KEY_TOGGLE_ETHERNET);
        mEthernetEnabler = new EthernetEnabler(getActivity(), ethernet_switch);
        updatePreferences(ethernet_switch.isChecked());
    }


    @Override
    public void onResume() {
        super.onResume();
        mEthernetManager.addListener(mEthernetListener);
        mEthernetEnabler.resume();
        updatePreferences(ethernet_switch.isChecked());
    }

    @Override
    public void onPause() {
        super.onPause();
        mEthernetManager.removeListener(mEthernetListener);
        mEthernetEnabler.pause();
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == ETHERNET_DIALOG_ID) {
            mDialog = new EthernetDialog(getActivity(),
                    (DialogInterface.OnClickListener)this,
                    mEthernetManager);
            return mDialog;
        }
        return super.onCreateDialog(dialogId);
    }

    @Override
    public int getMetricsCategory() {
        return ETHERNET;
    }
    @Override
    public int getDialogMetricsCategory(int dialogId) {
        return ETHERNET;
    }

    private void updatePreferencesScanning(EthernetInfo info) {
	int count = getPreferenceScreen().getPreferenceCount();

	for (int i = 0; i < count; i++) {
		Preference preference = getPreferenceScreen().getPreference(i);

		if (!preference.getKey().equals("toggle_ethernet")) {
			preference.setEnabled(false);

			if (!preference.getKey().equals("ethernet_change_settings")) {
				preference.setSummary("Waiting for connection");
			}
		}
	}
    }

    private void updatePreferences(boolean isAvailable)
    {
	    setEnabled(isAvailable);

	    if(isAvailable) {
		    updatePreferences();
	    }
    }

    private void setEnabled(boolean isAvailable)
    {

	int count = getPreferenceScreen().getPreferenceCount();

	for (int i = 0; i < count; i++) {
		Preference preference = getPreferenceScreen().getPreference(i);

		if (!preference.getKey().equals("toggle_ethernet")) {
			preference.setEnabled(isAvailable);

			if (!isAvailable) {
				preference.setSummary("");
			}

		}
	}
    }

    private void updatePreferences() {
        // Safeguard from some delayed event handling
        if (getActivity() == null) return;

        EthernetInfo info = mEthernetManager.getEthernetInfo();
        Log.e(TAG,"state = :" + info.getDetailedState());
        if (info != null) {
            Preference changeSettings = findPreference("ethernet_change_settings");
            changeSettings.setOnPreferenceClickListener(this);

	    if (info.getDetailedState() != DetailedState.CONNECTED) {
                updatePreferencesScanning(info);
		return;
            }

            String ipAssignment = "Static";
            String ipAddress = "";
            String netmask = "";
            String proxyHost = "";
            String proxyPort = "";
            String proxyExclusionList[];
            String proxyExclusion = "";
            String defaultGateway = "";
            String dns = "";

            IpConfiguration ipCfg = mEthernetManager.getConfiguration();
            IpAssignment ipAssin = ipCfg.getIpAssignment();
            LinkProperties linkProperties = info.getLinkProperties();
            if (ipAssin == IpAssignment.STATIC) {
                ipAssignment = "STATIC";
            }
            if (ipAssin == IpAssignment.DHCP) {
                ipAssignment = "DHCP";
            }
            if(linkProperties != null && linkProperties.getLinkAddresses() != null ) {
                List<LinkAddress> linkAddresses = linkProperties.getLinkAddresses();
                /* Display Address only if we get the IPV4 Address otherwise keep it empty */
                for(LinkAddress linkAddress : linkAddresses) {
                    if(linkAddress.getAddress() instanceof Inet4Address){
                        ipAddress = linkAddress.getAddress().getHostAddress();
                        netmask = Integer.toString(linkAddress.getNetworkPrefixLength());
                    }
                }
            } else {
                ipAddress = "";
                netmask = "";
            }

            InetAddress dgw = info.getDefaultGateway();
            if (dgw != null) {
                defaultGateway = dgw.getHostAddress();
            }
            InetAddress dnsIa = info.getDNS1();
            if (dnsIa != null) {
                dns = dnsIa.getHostAddress();
                dnsIa = info.getDNS2();
                if (dnsIa != null) {
                    dns += ", " + dnsIa.getHostAddress();
                }
            }


            if (info.getProxySettings() == ProxySettings.STATIC) {
                ProxyInfo pp = info.getLinkProperties().getHttpProxy();
                if(pp != null) {
                    proxyHost = pp.getHost();
                    proxyPort = Integer.toString(pp.getPort());
                    proxyExclusionList = pp.getExclusionList();
                    proxyExclusion = proxyExclusionList[0];
                }
            }

            findPreference("ethernet_ip_settings").setSummary(ipAssignment);
            findPreference("ethernet_ip_address").setSummary(ipAddress);
            findPreference("ethernet_netmask").setSummary(netmask);
            findPreference("ethernet_default_gateway").setSummary(defaultGateway);
            findPreference("ethernet_dns").setSummary(dns);
            findPreference("ethernet_proxy_server").setSummary(proxyHost);
            findPreference("ethernet_proxy_port").setSummary(proxyPort);
            findPreference("ethernet_proxy_exclusion").setSummary(proxyExclusion);
            findPreference("ethernet_interface_name").setSummary(info.getName());
            findPreference("ethernet_mac_address").setSummary(info.getHwAddress());
        }
    }

    private IpConfiguration convertEthernetInfoToIpConfigration(EthernetInfo info){
        if (info == null) {
            Log.d(TAG, "EthernetInfo == null");
            return null;
        }

        IpConfiguration ipconfig = mEthernetManager.getConfiguration();
        StaticIpConfiguration staticIpConfig = new StaticIpConfiguration();
        LinkProperties mLinkPro = info.getLinkProperties();

        Log.d(TAG, "convert ipconfig = "+ipconfig+"  staticIpconfig = " + staticIpConfig + " mLinkPro = "+mLinkPro);

        if (info.isStaticIpAssignment()) {
            staticIpConfig.ipAddress = mLinkPro.getLinkAddresses().get(0);
            staticIpConfig.gateway = mLinkPro.getRoutes().get(0).getGateway();
            staticIpConfig.dnsServers.addAll(mLinkPro.getDnsServers());
            staticIpConfig.domains = mLinkPro.getDomains();

            ipconfig.setIpAssignment(IpAssignment.STATIC);
            ipconfig.setStaticIpConfiguration(staticIpConfig);
        } else {
            ipconfig.setIpAssignment(IpAssignment.DHCP);
        }


        ipconfig.setProxySettings(info.getProxySettings());
        ipconfig.setHttpProxy(info.getHttpProxy());

        return ipconfig;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == AlertDialog.BUTTON_POSITIVE) {
            if (mDialog == null) {
                Log.d(TAG, "mDialog == null");
            } else if (mDialog.getController() == null) {
                Log.d(TAG, "mDialog.getController() == null");
            } else if (mDialog.getController().getInfo() == null) {
                Log.d(TAG, "mDialog.getController().getInfo()");
            }
            EthernetInfo info = mDialog.getController().getInfo();
            IpConfiguration ipconfig = convertEthernetInfoToIpConfigration(info);

            if (ipconfig == null) {
                Log.d(TAG, "ipconfig == null");
                return;
            }

            mEthernetManager.setConfiguration(ipconfig);
        }
    }
}
