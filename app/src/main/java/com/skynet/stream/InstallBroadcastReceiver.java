package com.skynet.stream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.LogUtils;

public class InstallBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e("có app đc add");
        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }

        if (action != null && Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            String dataString = intent.getDataString();
            if (dataString != null
                    && dataString.equals("com.skynet.lian")) {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.skynet.lian");
                context.startActivity( launchIntent );
                //Launch your service :)
            }
        }
    }
}