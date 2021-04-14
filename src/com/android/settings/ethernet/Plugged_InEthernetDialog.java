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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.net.EthernetManager;
import com.android.settings.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Plugged_InEthernetDialog extends AlertDialog implements Plugged_InEthernetConfigUiBase {
    private static final String TAG = "Plugged_InEthernetDialog";
    private final DialogInterface.OnClickListener mListener;
    private View mView;
    private Plugged_InEthernetConfigController mController;
    private EthernetManager mEthernetManager;

    Plugged_InEthernetDialog(Context context, DialogInterface.OnClickListener listener,
            EthernetManager manager) {
        super(context, R.style.Theme_EthernetDialog);
        mListener = listener;
        mEthernetManager = manager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.ethernet_dialog, null);
        setView(mView);
        setInverseBackgroundForced(true);
        mController = new Plugged_InEthernetConfigController(this, mView, mEthernetManager);
        super.onCreate(savedInstanceState);
        /* During creation, the submit button can be unavailable to determine
         * visibility. Right after creation, update button visibility */
        mController.enableSubmitIfAppropriate();
    }

    @Override
    public Plugged_InEthernetConfigController getController() {
        return mController;
    }

    @Override
    public void setSubmitButton(CharSequence text) {
        setButton(BUTTON_POSITIVE, text, mListener);
    }

    @Override
    public void setCancelButton(CharSequence text) {
        setButton(BUTTON_NEGATIVE, text, mListener);
    }

    @Override
    public Button getSubmitButton() {
        return getButton(BUTTON_POSITIVE);
    }

    @Override
    public Button getCancelButton() {
        return getButton(BUTTON_NEGATIVE);
    }
}
