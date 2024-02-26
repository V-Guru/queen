// Copyright 2018 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.wozart.aura.ui.wifisettings;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wozart.aura.R;
import com.wozart.aura.espProvision.Provision;
import com.wozart.aura.espProvision.security.Security;
import com.wozart.aura.espProvision.security.Security0;
import com.wozart.aura.espProvision.security.Security1;
import com.wozart.aura.espProvision.session.Session;
import com.wozart.aura.espProvision.transport.BLETransport;
import com.wozart.aura.espProvision.transport.SoftAPTransport;
import com.wozart.aura.espProvision.transport.Transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import espressif.Constants;
import espressif.WifiConstants;

public class ProvisionActivity extends AppCompatActivity {
    private static final String TAG = "Espressif::" + ProvisionActivity.class.getSimpleName();
    public static BLETransport BLE_TRANSPORT = null;

    private String ssid;
    private String passphrase;
    private TextView ssidInput;

    private Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provision);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView back = (ImageView) findViewById(R.id.back);
        toolbar.setTitle(R.string.provision_activity_title);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        runnable = new Runnable() {
            @Override
            public void run() {

            }
        };

        Intent intent = getIntent();
        final String WiFiSSID = intent.getStringExtra(Provision.PROVISIONING_WIFI_SSID);
        ssidInput = findViewById(R.id.ssid_input_layout);
        ssidInput.setText(WiFiSSID);
        ssid = WiFiSSID;
        Log.d("ProvisionActivity", "Selected AP -" + WiFiSSID);

        final String pop = intent.getStringExtra(Provision.CONFIG_PROOF_OF_POSSESSION_KEY);
        final String baseUrl = intent.getStringExtra(Provision.CONFIG_BASE_URL_KEY);
        final String transportVersion = intent.getStringExtra(Provision.CONFIG_TRANSPORT_KEY);
        final String securityVersion = intent.getStringExtra(Provision.CONFIG_SECURITY_KEY);
        final String deviceUUID = intent.getStringExtra(BLETransport.SERVICE_UUID_KEY);
        final String sessionUUID = intent.getStringExtra(BLETransport.SESSION_UUID_KEY);
        final String configUUID = intent.getStringExtra(BLETransport.CONFIG_UUID_KEY);
        final String deviceNamePrefix = intent.getStringExtra(BLETransport.DEVICE_NAME_PREFIX_KEY);

        final EditText passphraseInput = findViewById(R.id.password_input);
        passphraseInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passphrase = charSequence.toString().trim();
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        final Button provision = findViewById(R.id.provision_button);
        final Activity thisActivity = this;
        provision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFormState(false);

                final Security security;
                if (securityVersion.equals(Provision.CONFIG_SECURITY_SECURITY1)) {
                    security = new Security1(pop);
                } else {
                    security = new Security0();
                }
                final Transport transport;
                transport = new SoftAPTransport(baseUrl);
                provision(transport, security);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void provision(Transport transport, Security security) {
        final Activity thisActivity = this;
        final Session session = new Session(transport, security);
        session.sessionListener = new Session.SessionListener() {
            @Override
            public void OnSessionEstablished() {
                thisActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(thisActivity,
                                "Successful connected and ready to pair.",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });

                final Provision provision = new Provision(session);
                provision.provisioningListener = new Provision.ProvisioningListener() {
                    @Override
                    public void OnApplyConfigurationsSucceeded() {
                        thisActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(thisActivity,
                                        "Configuration successfull",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }

                    @Override
                    public void OnApplyConfigurationsFailed() {
                        thisActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(thisActivity,
                                        "Configuration successfull",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }

                    @Override
                    public void OnWifiConnectionStatusUpdated(final WifiConstants.WifiStationState newStatus,
                                                              final WifiConstants.WifiConnectFailedReason failedReason,
                                                              final Exception e) {
                        String statusText = "";
                        if (e != null) {
                            statusText = e.getMessage();
                        } else if (newStatus == WifiConstants.WifiStationState.Connected) {
                            //Handler handler = new Handler().postDelayed()
                            statusText = thisActivity.getResources().getString(R.string.success_text);
                        } else if (newStatus == WifiConstants.WifiStationState.Disconnected) {
                            statusText = thisActivity.getResources().getString(R.string.wifi_disconnected_text);
                        } else {
                            statusText = thisActivity.getResources().getString(R.string.device_connection_lost);
                        }
                        goToSuccessPage(statusText);
                    }

                    @Override
                    public void OnProvisioningFailed(Exception e) {
                        thisActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /*Toast.makeText(thisActivity,
                                        "Connection failed",
                                        Toast.LENGTH_LONG)
                                        .show();*/
                                toggleFormState(true);
                            }
                        });
                        thisActivity.setResult(RESULT_CANCELED);
                        thisActivity.finish();
                    }
                };
                provision.configureWifi(ssid, passphrase, new Provision.ProvisionActionListener() {
                    @Override
                    public void onComplete(Constants.Status status, Exception e) {
                        provision.applyConfigurations(null);
                    }
                });
            }

            @Override
            public void OnSessionEstablishFailed(Exception e) {
                thisActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*Toast.makeText(thisActivity,
                                "Connection failed",
                                Toast.LENGTH_LONG)
                                .show();*/
                        toggleFormState(true);
                    }
                });

            }
        };
        session.init(null);
    }

    private void validateForm() {
        Button provision = findViewById(R.id.provision_button);

        boolean enabled = this.ssid != null &&
                this.ssid.length() > 0;
        provision.setEnabled(enabled);
    }

    private void toggleFormState(boolean isEnabled) {
        final View loadingIndicator = findViewById(R.id.progress_indicator);
        final EditText passphraseInput = findViewById(R.id.password_input);
        final Button provision = findViewById(R.id.provision_button);

        if (isEnabled) {
            loadingIndicator.setVisibility(View.GONE);
            provision.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            provision.setEnabled(true);
            ssidInput.setEnabled(true);
            passphraseInput.setEnabled(true);
        } else {
            loadingIndicator.setVisibility(View.VISIBLE);
            provision.setBackgroundColor(getResources().getColor(R.color.gray));
            provision.setEnabled(false);
            ssidInput.setEnabled(false);
            passphraseInput.setEnabled(false);
        }
    }

    private void goToSuccessPage(String statusText) {
        Intent goToSuccessPage = new Intent(getApplicationContext(), ProvisionSuccessActivity.class);
        goToSuccessPage.putExtra("status", statusText);
        startActivity(goToSuccessPage);
        this.setResult(RESULT_OK);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
