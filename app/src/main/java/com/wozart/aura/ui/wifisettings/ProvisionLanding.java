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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wozart.aura.R;
import com.wozart.aura.espProvision.Provision;
import com.wozart.aura.utilities.Constant;

import java.util.Objects;


public class ProvisionLanding extends AppCompatActivity {
    private static final String TAG = "Espressif::" + ProvisionLanding.class.getSimpleName();

    private String currentSSID;
    private String wifiAPPrefix;
    private TextView titleText;
    private String aura_sense_wifi_prefix;
    private String aura_plug_wifi;
    private String aura_curtain_wifi;
    private String aura_switch_pro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provision_landing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        titleText = (TextView) findViewById(R.id.textTitle);
        ImageView back = (ImageView) findViewById(R.id.back);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        wifiAPPrefix = getIntent().getStringExtra(Provision.CONFIG_WIFI_AP_KEY);
        aura_sense_wifi_prefix = getIntent().getStringExtra(Provision.CONFIG_AURA_SENSE_WIFI_KEY);
        aura_plug_wifi = getIntent().getStringExtra(Provision.CONFIG_AURA_PLUG_WIFI_KEY);
        aura_curtain_wifi = getString(R.string.aura_curtain_product);
        aura_switch_pro = getString(R.string.switch_pro);

        if (wifiAPPrefix == null) {
            wifiAPPrefix = "ESP-Alexa-";
        }

        TextView wifiInstructions = findViewById(R.id.start_provisioning_message);
        Button connectButton = findViewById(R.id.connect_button);
        TextView noInternetInstructions = findViewById(R.id.no_internet_note);

        currentSSID = this.fetchWifiSSID();
        if ((currentSSID != null) && (currentSSID.startsWith(wifiAPPrefix) || currentSSID.equals(wifiAPPrefix) || currentSSID.startsWith(aura_sense_wifi_prefix) || currentSSID.startsWith(aura_plug_wifi) || currentSSID.startsWith(aura_curtain_wifi) || currentSSID.startsWith(aura_switch_pro) || currentSSID.contains(Constant.DEVICE_WIFI_PREFIX))) {
            connectButton.setText(R.string.connected_to_device_action);
            titleText.setText("Connect to Wi-Fi");
            wifiInstructions.setText(R.string.connected_to_device_instructions);
            noInternetInstructions.setVisibility(View.VISIBLE);
        } else if ((currentSSID != null) && (currentSSID.contains("Android"))) {
            connectButton.setText(R.string.connected_to_device_action);
            titleText.setText("Connect to Wi-Fi");
            wifiInstructions.setText(R.string.connected_to_device_instructions);
        } else {
            connectButton.setText(R.string.connect_to_device_action);
            SpannableStringBuilder instructions = getWifiInstructions();
            wifiInstructions.setText(instructions);
            noInternetInstructions.setVisibility(View.GONE);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSSID = fetchWifiSSID();
                if ((currentSSID != null) && (currentSSID.startsWith(wifiAPPrefix) || currentSSID.equals(wifiAPPrefix) || currentSSID.startsWith(aura_sense_wifi_prefix) || currentSSID.startsWith(aura_plug_wifi) || currentSSID.startsWith(aura_curtain_wifi) || currentSSID.startsWith(aura_switch_pro) || currentSSID.contains(Constant.DEVICE_WIFI_PREFIX))) {
                    Intent wifiListIntent = new Intent(getApplicationContext(), WifiScan.class);
                    wifiListIntent.putExtras(getIntent());
                    startActivity(wifiListIntent);
                    finish();
                } else if ((currentSSID != null) && (currentSSID.contains("Android"))) {
                    Intent intent = new Intent(getApplicationContext(), WifiSettingsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 100);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Provision.REQUEST_PROVISIONING_CODE &&
                resultCode == RESULT_OK) {
            setResult(resultCode);
            finish();
        } else if (requestCode == 100) {
            Button connectButton = findViewById(R.id.connect_button);
            TextView wifiInstructions = findViewById(R.id.start_provisioning_message);
            TextView noInternetInstructions = findViewById(R.id.no_internet_note);
            currentSSID = this.fetchWifiSSID();
            if ((currentSSID != null) && (currentSSID.startsWith(wifiAPPrefix) || currentSSID.equals(wifiAPPrefix) || currentSSID.startsWith(aura_sense_wifi_prefix) || currentSSID.startsWith(aura_plug_wifi) || currentSSID.startsWith(aura_curtain_wifi) || currentSSID.startsWith(aura_switch_pro) || currentSSID.contains(Constant.DEVICE_WIFI_PREFIX))) {
                connectButton.setText(R.string.connected_to_device_action);
                titleText.setText("Connect to Wi-Fi");
                wifiInstructions.setText(R.string.connected_to_device_instructions);
                noInternetInstructions.setVisibility(View.VISIBLE);
            } else if ((currentSSID != null) && (currentSSID.contains("Android"))) {
                titleText.setText("Connect to Wi-Fi");
                connectButton.setText(R.string.connected_to_device_action);
                wifiInstructions.setText(R.string.connected_to_device_instructions);
                noInternetInstructions.setVisibility(View.VISIBLE);
            } else {
                connectButton.setText(R.string.connect_to_device_action);
                SpannableStringBuilder instructions = getWifiInstructions();
                wifiInstructions.setText(instructions);
                noInternetInstructions.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Provision.REQUEST_PERMISSIONS_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Button connectButton = findViewById(R.id.connect_button);
                    TextView wifiInstructions = findViewById(R.id.start_provisioning_message);
                    TextView noInternetInstructions = findViewById(R.id.no_internet_note);
                    currentSSID = this.fetchWifiSSID();
                    if ((currentSSID != null) && (currentSSID.startsWith(wifiAPPrefix) || currentSSID.equals(wifiAPPrefix) || currentSSID.startsWith(aura_sense_wifi_prefix) || currentSSID.startsWith(aura_plug_wifi) || currentSSID.startsWith(aura_curtain_wifi) || currentSSID.startsWith(aura_switch_pro) || currentSSID.contains(Constant.DEVICE_WIFI_PREFIX))) {
                        connectButton.setText(R.string.connected_to_device_action);
                        titleText.setText("Connect to Wi-Fi");
                        wifiInstructions.setText(R.string.connected_to_device_instructions);
                        noInternetInstructions.setVisibility(View.VISIBLE);
                    } else if ((currentSSID != null) && (currentSSID.contains("Android"))) {
                        connectButton.setText(R.string.connected_to_device_action);
                        titleText.setText("Connect to Wi-Fi");
                        wifiInstructions.setText(R.string.connected_to_device_instructions);
                        noInternetInstructions.setVisibility(View.VISIBLE);
                    } else {
                        connectButton.setText(R.string.connect_to_device_action);
                        SpannableStringBuilder instructions = getWifiInstructions();
                        wifiInstructions.setText(instructions);
                        noInternetInstructions.setVisibility(View.GONE);
                    }
                }
            }
            break;
        }
    }

    private String fetchWifiSSID() {
        String ssid = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo == null) {
                return null;
            }

            if (networkInfo.isConnected()) {
                final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null) {
                    ssid = connectionInfo.getSSID();
                    ssid = ssid.replaceAll("^\"|\"$", "");
                }
            }

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, Provision.REQUEST_PERMISSIONS_CODE);
        }
        return ssid;
    }


    private SpannableStringBuilder getWifiInstructions() {
        String instructions = getResources().getString(R.string.connect_to_device_instructions);
        SpannableStringBuilder str = new SpannableStringBuilder(instructions);
        return str;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
