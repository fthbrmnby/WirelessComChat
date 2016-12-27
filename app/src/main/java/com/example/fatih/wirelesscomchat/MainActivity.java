package com.example.fatih.wirelesscomchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
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

public class MainActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

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

        //Wifi direct kullanarak cihaz araması yapabiliyor muyuz?
        btn_discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    // Eğer discover tuşuna bastığımızda yakınlardaki cihazları başarıyla
                    // arayabiliyorsak onSuccess() metodu çalışır. Ancak bu metot içerisinde bir
                    // işlem yapmayız. Çünkü WifiDirectBroadcastReceiver içerindeki trigger'da
                    // işlem yaparız.
                    @Override
                    public void onSuccess() {
                        /*Toast.makeText(getApplicationContext(), "Discovery is a success.",
                                Toast.LENGTH_SHORT).show();*/
                        //startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }

                    // Eğer cihaz araması yapılamazsa buradan hata mesajı ve hatanın nedenini ekrana
                    // yazdırabiliriz.
                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(getApplicationContext(), "Discovery is a failure " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Cihaz listesinde seçilen cihazı kontrol edip o cihaza bağlanmak için gerekli
        // metodu çağırıyoruz.
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                connectToDevice(i);
            }
        });
    }

    // Uygulama minimize edilip tekrar açıldığında trigger'ları intent'e bağlıyoruz.
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    // Uygulama minimize edildiğinde kaynakları kullanmaya devam etmemesi için trigger'ları unbound
    // ediyoruz.
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    // Discover tuşuna basıldığında eğer yakınlarda cihaz bulunursa bulunan cihazları ekranda
    // gösterebilmek için WifiP2pDeviceList'i ArrayList'e çeviriyoruz. Daha sonra bu ArrayList
    // içerindeki cihazlar ekrana liste olarak yazdırılır.
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

    // Bulunan cihazlar listesinden seçilen cihazın bilgileri elimizde varolan deviceList'den alınır
    // ve bu cihazın konfigurasyon bilgileri kullanılarak cihaza bağlanılır.
    void connectToDevice(int position) {
        final WifiP2pDevice toConnect = deviceList.get(position);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = toConnect.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            // Bağlantı başarılı olursa chat ekranına geçiyoruz.
            @Override
            public void onSuccess() {
                Log.i(TAG, "Successfully connectted to "+toConnect.deviceName);
                Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                MainActivity.this.startActivity(chatIntent);
            }

            // Bağlantı kurulamazsa hata kodu burada yazdırılır.
            @Override
            public void onFailure(int reason) {
               Log.i(TAG, "Error while connecting to device. Reason code: "+reason);
            }
        });

    }

    // Burası daha bitmedi. Deneysel takılıyoruz.
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.isGroupOwner) {
            Log.i(TAG, "I am the group owner");
        } else {
            Log.i(TAG, "Group owner address" + info.groupOwnerAddress.getHostAddress());
        }
    }
}
