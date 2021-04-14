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

 /* This Class is used for showing the Pluggedin Ethernet UI in Setting application.
 *
 * It allow the user to enable/disable pluggedin ethernet.
 *
 * It show the Plugged in ethernet UI status(enabled/disabled),IP address and other parameters.
 *
 */
package com.android.settings.ethernet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Context;
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
import com.android.settings.core.instrumentation.Instrumentable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import android.content.BroadcastReceiver;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;

public class Plugged_InEthernetSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener,
            DialogInterface.OnClickListener {
    private static final String TAG = "PluggedInEthernetSettings";
    private static final int ETHERNET_DIALOG_ID = 1;
    private static final String KEY_TOGGLE_PLUGETHERNET = "toggle_plugethernet";
    public static final int ETHERNET = 100001;

    private EthernetManager mEthernetManager;
    private Plugged_InEthernetDialog mDialog;
    private TextView mEmptyView;
    private SwitchPreference plugethernet_switch;
    private Plugged_InEthernetEnabler mEthernetEnabler;

    private final EthernetManager.Listener mEth1Listeners = new EthernetManager.Listener() {
    @Override
    public void onAvailabilityChanged(boolean isPluggedInEthAvailable) {
        updatePreferences(isPluggedInEthAvailable);
    }
    };

    public Plugged_InEthernetSettings() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // After confirming PreferenceScreen is available, we call super.
        super.onActivityCreated(savedInstanceState);
    }

    public boolean onPreferenceClick(Preference preference) {
        Log.d(TAG, "Plugged_InEthernetSettings onPreferenceClick + " + preference.getKey());
        if (preference.getKey().equals("pluggedinethernet_change_settings")) {
            showDialog(ETHERNET_DIALOG_ID);
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.plugged_inethernetsettings);
        mEthernetManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);
        plugethernet_switch = (SwitchPreference) findPreference(KEY_TOGGLE_PLUGETHERNET);
        mEthernetEnabler = new Plugged_InEthernetEnabler(getActivity(), plugethernet_switch);
        updatePreferences(plugethernet_switch.isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume enter _plugged in");
        mEthernetManager.addPluggedinEthListener(mEth1Listeners);
        mEthernetEnabler.resume();
        updatePreferences(plugethernet_switch.isChecked());
        Log.d(TAG, "onResume exit _plugged in");
    }

    @Override
    public void onPause() {
        super.onPause();
        mEthernetManager.removePluggedinEthListener(mEth1Listeners);
        mEthernetEnabler.pause();
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == ETHERNET_DIALOG_ID) {
            mDialog = new Plugged_InEthernetDialog(getActivity(),
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

            if (!preference.getKey().equals("toggle_plugethernet")) {
                preference.setEnabled(false);

                if (!preference.getKey().equals("pluggedinethernet_change_settings")) {
                    preference.setSummary("Waiting for connection");
                }
            }
        }
    }

    private void updatePreferences(boolean isPluggedInEthAvailable)
    {
        setEnabled(isPluggedInEthAvailable);

        if(isPluggedInEthAvailable) {
            updatePreferences();
        }
    }

    private void setEnabled(boolean isPluggedInEthAvailable)
    {

    int count = getPreferenceScreen().getPreferenceCount();

    for (int i = 0; i < count; i++) {
        Preference preference = getPreferenceScreen().getPreference(i);

        if (!preference.getKey().equals("toggle_plugethernet")) {
            preference.setEnabled(isPluggedInEthAvailable);

            if (!isPluggedInEthAvailable) {
            preference.setSummary("");
            }

        }
    }
    }

    private void updatePreferences() {
        // Safeguard from some delayed event handling
        if (getActivity() == null) return;

        EthernetInfo info = mEthernetManager.getPluggedInEthernetInfo();
        if (info != null) {
            Preference changeSettings = findPreference("pluggedinethernet_change_settings");
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
            LinkProperties linkProperties = info.getLinkProperties();
            IpConfiguration ipCfg = mEthernetManager.getPluggedInEthernetConfiguration();
            IpAssignment ipAssin = ipCfg.getIpAssignment();
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
            return null;
        }
        IpConfiguration ipconfig = mEthernetManager.getPluggedInEthernetConfiguration();
        StaticIpConfiguration staticIpConfig = new StaticIpConfiguration();
        LinkProperties mLinkPro = info.getLinkProperties();
        Log.i(TAG, "convert ipconfig = "+ipconfig+"  staticIpconfig = " + staticIpConfig + " mLinkPro = "+mLinkPro);
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
            } else if (mDialog.getController().getPluggedInEthernetInfo() == null) {
            }
            EthernetInfo info = mDialog.getController().getPluggedInEthernetInfo();
            Log.i(TAG," BUTTON_POSITIVE " + info);
            IpConfiguration ipconfig = convertEthernetInfoToIpConfigration(info);
            if (ipconfig == null) {
                return;
            }
            mEthernetManager.setPluggedInEthernetConfiguration(ipconfig);
        }
    }
}

