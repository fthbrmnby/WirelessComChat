package com.example.fatih.wirelesscomchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by fatih on 02.12.2016.
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private final String LOG_TAG = this.toString();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    // Wifi durumunun değişikliği durumunda fire edilen trigger'ları burada catch ediyoruz ve gerekli
    // işlemleri uyguluyoruz.
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Wifi açık mı kapalı mı burada kontrol edilebiliyor.
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.i(LOG_TAG, "Wifi Direct is enabled");
            } else {
                Log.i(LOG_TAG, "Wifi Direct is not enabled");
            }

            // Elimizdeki arama sonucunda bir değişiklik olduğunda (yeni cihaz bulunması ya da
            // varolan bir cihaza artık ulaşamama gibi) fire edilen trigger.
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Arama sonucunda bulunan cihazlar wifi p2p manager üzerinden alınır.
            // Bu metot asenkron bir çağrı ile PeerListListener.onPeersAvailable()
            // metodunu da çağırır.
            if (mManager != null) {
                mManager.requestPeers(mChannel, mActivity);
            }
            // Bağlantı işlemlerinde değişiklik olduğunda ( bir cihaza bağlanma ya da bağlantının
            // kopması durumunda) fire edilen trigger.
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                mManager.requestConnectionInfo(mChannel, mActivity);
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
    }
}
