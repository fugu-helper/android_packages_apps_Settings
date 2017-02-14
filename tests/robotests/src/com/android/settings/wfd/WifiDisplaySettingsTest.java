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

package com.android.settings.wfd;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRouter;

import com.android.settings.R;
import com.android.settings.SettingsRobolectricTestRunner;
import com.android.settings.TestConfig;
import com.android.settings.dashboard.SummaryLoader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class WifiDisplaySettingsTest {

    @Mock
    private Activity mActivity;
    @Mock
    private SummaryLoader mSummaryLoader;
    @Mock
    private MediaRouter mMediaRouter;

    private SummaryLoader.SummaryProvider mSummaryProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mActivity.getSystemService(Context.MEDIA_ROUTER_SERVICE))
                .thenReturn(mMediaRouter);

        mSummaryProvider = WifiDisplaySettings.SUMMARY_PROVIDER_FACTORY.createSummaryProvider(
                mActivity, mSummaryLoader);
    }

    @Test
    public void listenToSummary_disconnected_shouldProvideDisconnectedSummary() {
        mSummaryProvider.setListening(true);

        verify(mActivity).getString(R.string.disconnected);
        verify(mActivity, never()).getString(R.string.wifi_display_status_connected);
    }

    @Test
    public void listenToSummary_connected_shouldProvideConnectedSummary() {
        final MediaRouter.RouteInfo route = mock(MediaRouter.RouteInfo.class);
        when(mMediaRouter.getRouteCount()).thenReturn(1);
        when(mMediaRouter.getRouteAt(0)).thenReturn(route);
        when(route.matchesTypes(MediaRouter.ROUTE_TYPE_REMOTE_DISPLAY)).thenReturn(true);
        when(route.isSelected()).thenReturn(true);
        when(route.isConnecting()).thenReturn(false);

        mSummaryProvider.setListening(true);

        verify(mActivity).getString(R.string.wifi_display_status_connected);
    }

}