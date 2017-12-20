package com.example.android.healthdashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private TextView heartBeat;
    private TextView temp;

    private BroadcastReceiver receiver;
    private TreeMap<String,Double> loadMap(String whichMap){
        TreeMap<String,Double> outputMap = new TreeMap<>();
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("MAPS", Context.MODE_PRIVATE);
        try{
            if (pSharedPref != null){
                String jsonString = pSharedPref.getString(whichMap, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    Double value = jsonObject.getDouble(key);
                    outputMap.put(key, value);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Your Health Dashboard");
        //adapter = BluetoothAdapter.getDefaultAdapter();
        heartBeat = (TextView) findViewById(R.id.heartbeat);
        temp = (TextView) findViewById(R.id.temperature);
        if (loadMap("HeartMap").lastEntry() != null)
            heartBeat.setText(loadMap("HeartMap").lastEntry().getValue().toString()+" bpm");
        if(loadMap("TempMap").lastEntry() != null)
            temp.setText(loadMap("TempMap").lastEntry().getValue().toString() + " °C");
        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATE_UI");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //update ui
                Log.d("UPDATING UI","UPDATING UI TEST");
                Lock l = ((MyApplication)getApplicationContext()).lock;
                l.lock();
                if(loadMap("HeartMap").lastEntry() != null)
                    heartBeat.setText(loadMap("HeartMap").lastEntry().getValue().toString()+" bpm");
                if(loadMap("TempMap").lastEntry() != null)
                    temp.setText(loadMap("TempMap").lastEntry().getValue().toString() + " °C");
                //Testing Shared Preferences
                TreeMap<String,Double> temps = loadMap("TempMap");
                TreeMap<String, Double> hearts = loadMap("HeartMap");
                Log.d("heartsSP",hearts.toString());
                Log.d("tempsSP",temps.toString());
                l.unlock();
            }
        };
        registerReceiver(receiver, filter);
        if (!((MyApplication)getApplicationContext()).serviceRunning){
            Intent intent = new Intent(this,BLEService.class);
            startService(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (((MyApplication)getApplicationContext()).heartMap.size() != 0){
            heartBeat.setText(loadMap("HeartMap").lastEntry().getValue().toString()+" bpm");
        }
        if (((MyApplication)getApplicationContext()).tempMap.size() != 0){
            temp.setText(loadMap("TempMap").lastEntry().getValue().toString() + " °C");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.main_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.history:
                Intent intent = new Intent(this, Graphs.class);
                this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}
