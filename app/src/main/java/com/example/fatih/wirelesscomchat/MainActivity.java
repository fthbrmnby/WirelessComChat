package com.example.fatih.wirelesscomchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener {

    private final String TAG = this.getClass().toString();

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    ArrayList<WifiP2pDevice> deviceList;
    ArrayList deviceNames;
    ListView deviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceNames = new ArrayList();
        deviceListView = (ListView) findViewById(R.id.device_list);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Button btn_discover = (Button) findViewById(R.id.btn_discover);
        btn_discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        /*Toast.makeText(getApplicationContext(), "Discovery is a success.",
                                Toast.LENGTH_SHORT).show();*/
                        //startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(getApplicationContext(), "Discovery is a failure " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                connectToDevice(i);
            }
        });
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        if (wifiP2pDeviceList.getDeviceList().size() > 0) {
            deviceList = new ArrayList(wifiP2pDeviceList.getDeviceList());
            for (WifiP2pDevice device : deviceList) {
                deviceNames.add(device.deviceName);
            }
            ArrayAdapter adapter = new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1, deviceNames);


            deviceListView.setAdapter(adapter);
        }
    }

    void connectToDevice(int position) {
        final WifiP2pDevice toConnect = deviceList.get(position);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = toConnect.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG, "Successfully connectted to "+toConnect.deviceName);
                Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                MainActivity.this.startActivity(chatIntent);
            }

            @Override
            public void onFailure(int reason) {
               Log.i(TAG, "Error while connecting to device. Reason code: "+reason);
            }
        });

    }
}
