package com.gmail.durgaprasad2002bhat.wifip2pexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class WiFiDirectBroadcastReciever extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public WiFiDirectBroadcastReciever(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mactivity) {
        this.mActivity = mactivity;
        this.mChannel = mChannel;
        this.mManager = mManager;

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "WiFi is ON", Toast.LENGTH_SHORT).show();
            } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                Toast.makeText(context, "WiFi is OFF", Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {

                mManager.requestPeers(mChannel, mActivity.peerListListener);
            }

        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                  if(mManager==null){
                      return;
                  }
                  NetworkInfo networkInfo= intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                  if(networkInfo.isConnected()){
                          mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
                      }else{
                         mActivity.connectionStatus.setText("Device Disconnected");

                      }

        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }

    }
}
