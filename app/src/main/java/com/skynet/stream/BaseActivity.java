package com.skynet.stream;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.blankj.utilcode.util.NetworkUtils;
import com.skynet.stream.network.socket.SocketClient;
import com.skynet.stream.network.socket.SocketConstants;


/**
 * Created by thaopt on 12/1/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected abstract int initLayout();

    protected abstract void initVariables();

    protected abstract void initViews();

    protected SocketClient mSocket;
    private boolean mBounded;

    public SocketClient getmSocket() {
        return mSocket;
    }

    public void setmSocket(SocketClient mSocket) {
        this.mSocket = mSocket;
    }

    AlertDialog dialogNetwork;
    BroadcastReceiver receiverConnectionNetwork = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (!NetworkUtils.isConnected()) {
//                if (dialogNetwork == null)
//                dialogNetwork.show();
//            } else {
//                if (dialogNetwork != null) dialogNetwork.dismiss();
//                if (getmSocket() != null) getmSocket().initSocket();
//            }
        }
    };
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BaseActivity.this.mBounded = true;
            SocketClient.LocalBinder mLocalBinder = (SocketClient.LocalBinder) service;
            BaseActivity.this.mSocket = mLocalBinder.getServerInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            BaseActivity.this.mBounded = false;
            BaseActivity.this.mSocket = null;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initLayout());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().setStatusBarColor( ContextCompat.getColor(this,R.color.white));
//        }else {
//            StatusBarUtil.setLightMode(this);
//        }
        initViews();
        initVariables();
//        dialogError = new MaterialDialog.Builder(this).title(R.string.error)
//                .content(getString(R.string.unknow_error))
//                .positiveText(R.string.dismis)
//                .positiveColor(Color.GRAY)
//                .build();
//        IntentFilter intentFilter = new IntentFilter(GPSService.ACTION_LOCATION_UPDATE);
//        registerReceiver(receiverGPS, intentFilter);

        Intent mIntent = new Intent(this, SocketClient.class);
        startService(mIntent);
        bindService(mIntent, this.mConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.receiverConnectionNetwork);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter i = new IntentFilter();
        i.addAction(SocketConstants.EVENT_CONNECTION);
        IntentFilter iConnection = new IntentFilter();
        iConnection.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(this.receiverConnectionNetwork, iConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mBounded) {
            unbindService(this.mConnection);
            this.mBounded = false;
        }
    }




}
