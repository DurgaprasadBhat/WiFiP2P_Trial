package com.gmail.durgaprasad2002bhat.wifip2pexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView read_msg_box, connectionStatus;
    EditText writeMsg;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReciever;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendRecieve sendRecieve;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage( Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0, msg.arg1 );
                    read_msg_box.setText(tempMsg);
                    break;

            }
            return true;
        }
    });

    private void exqListener() {
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("WiFi ON");
                } else {
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("WiFi OFF");
                }
            }
        });
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                        connectionStatus.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionStatus.setText("Discovery Failed");

                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               final  WifiP2pDevice  device = deviceArray[position];
                WifiP2pConfig config =  new WifiP2pConfig();
                config.deviceAddress= device.deviceAddress;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener(){
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to"+device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                          Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = writeMsg.getText().toString();
                sendRecieve.write(msg.getBytes());
            }
        });
    }

    private void initialWork() {
        btnOnOff= (Button) findViewById(R.id.onOff);
        btnDiscover = (Button) findViewById(R.id.discover);
        btnSend = (Button) findViewById(R.id.sendButton);
        listView = (ListView) findViewById(R.id.peerListView);
        read_msg_box = (TextView) findViewById(R.id.readMsg);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        writeMsg = (EditText) findViewById(R.id.writeMsg);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReciever = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peersList) {
            if(!peersList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peersList.getDeviceList());


                deviceNameArray= new String[peersList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peersList.getDeviceList().size()];
                int index=0;

                for(WifiP2pDevice device : peersList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;

                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);

                if(peers.size()==0){
                    Toast.makeText(getApplicationContext(), "NO Devices found", Toast.LENGTH_SHORT).show();;
                }
            }
        }
    };


    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pinfo) {
           final InetAddress groupOwnerAddress = wifiP2pinfo.groupOwnerAddress;

           if(wifiP2pinfo.groupFormed&& wifiP2pinfo.isGroupOwner){
               connectionStatus.setText("Host");
               serverClass = new ServerClass();
               serverClass.start();

           }else if(wifiP2pinfo.groupFormed){
               connectionStatus.setText("Client");
               clientClass = new ClientClass(groupOwnerAddress);
               clientClass.start();
           }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReciever, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReciever);
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class SendRecieve extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendRecieve(Socket skt){
            socket = skt;
            try {
                inputStream= socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket!= null){
                try {
                    bytes=inputStream.read(buffer);
                    if(bytes>0){
                          handler.obtainMessage(MESSAGE_READ,bytes, -1, buffer).sendToTarget();
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class ClientClass extends Thread{
        Socket socket;
        String hostadd;

        public ClientClass(InetAddress hostAddress){

            hostadd = hostAddress.getHostAddress();
            socket= new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostadd, 8888), 500);
                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}