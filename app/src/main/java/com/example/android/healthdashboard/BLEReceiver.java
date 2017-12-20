package com.example.android.healthdashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BLEReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("STARTING_BLE","trying to start ble scanning");
        Intent startServiceIntent = new Intent(context, BLEService.class);
        context.startService(startServiceIntent);

    }
}
