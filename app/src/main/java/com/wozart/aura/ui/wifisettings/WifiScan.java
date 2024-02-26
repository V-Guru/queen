package com.wozart.aura.ui.wifisettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.wozart.aura.R;
import com.wozart.aura.espProvision.Provision;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WifiScan extends AppCompatActivity {

    private WifiManager wifiManager;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private ProgressBar progressBar;
    private ListView listView;
    String wifis[];
    String filtered[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_scan_);
        Button buttonScan = findViewById(R.id.scanBtn);
        progressBar = findViewById(R.id.wifi_progress_indicator);
        progressBar.setVisibility(View.VISIBLE);

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scanWifi();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView back = (ImageView) findViewById(R.id.back);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!Objects.requireNonNull(wifiManager).isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled, Enable WiFi..", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                progressBar.setVisibility(View.VISIBLE);
                callProvision(arrayList.get(pos));
            }

        });
        scanWifi();
    }

    private void callProvision(String ssid) {
        finish();
        Intent launchProvisionInstructions = new Intent(getApplicationContext(), ProvisionActivity.class);
        launchProvisionInstructions.putExtras(getIntent());
        launchProvisionInstructions.putExtra(Provision.PROVISIONING_WIFI_SSID, ssid);
        startActivity(launchProvisionInstructions);
        finish();

    }

    private void scanWifi() {
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            unregisterReceiver(this);
            for (ScanResult scanResult : results) {
                if (!scanResult.SSID.isEmpty()) {
                    arrayList.add(scanResult.SSID);
                }
            }
            adapter.notifyDataSetChanged();
        }
    };
}
