package com.example.android.healthdashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;

import static com.example.android.healthdashboard.R.id.graphHeart;
import static com.example.android.healthdashboard.R.id.graphTemp;

public class Graphs extends AppCompatActivity {
    private BroadcastReceiver receiver;
    private GraphView graph;
    private GraphView graph2;
    private Viewport v1;
    private Viewport v2;
    private LineGraphSeries<DataPoint> series;
    private LineGraphSeries<DataPoint> series2;
    private TextView heart;
    private TextView temp;

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
        setContentView(R.layout.activity_graphs);
        heart = (TextView)findViewById(graphHeart);
        temp = (TextView)findViewById(graphTemp);
        if(loadMap("HeartMap").lastEntry() != null)
            heart.setText(loadMap("HeartMap").lastEntry().getValue().toString() + " bpm");
        Lock l = ((MyApplication) getApplicationContext()).lock;
        l.lock();

        Map<String,Double> heartMap = loadMap("HeartMap");
        setTitle("Your Health Dashboard");
        final SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss", Locale.US);
        graph = (GraphView) findViewById(R.id.graph);
        graph.setTitle("Heartbeat");
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time");
        gridLabel.setVerticalAxisTitle("Heartbeat (bpm)");
        gridLabel.setVerticalAxisTitleColor(Color.parseColor("#3F51B5"));
        gridLabel.setHorizontalAxisTitleColor(Color.parseColor("#3F51B5"));
        graph.setTitleColor(Color.parseColor("#3F51B5"));
        series = new LineGraphSeries<>();
        for (Map.Entry<String, Double> entry : heartMap.entrySet()) {
            series.appendData(new DataPoint(Date.parse(entry.getKey()),entry.getValue()),true,100);
        }
        graph.addSeries(series);
        graph.getViewport().setMinX(Date.parse(loadMap("HeartMap").firstEntry().getKey()));
        graph.getViewport().setMaxX(Date.parse(loadMap("HeartMap").lastEntry().getKey()));
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return sdf.format(value);
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });
        if(loadMap("TempMap").lastEntry() != null)
            temp.setText(loadMap("TempMap").lastEntry().getValue().toString() + " °C");
        Map<String,Double> tempMap = loadMap("TempMap");
        graph2 = (GraphView) findViewById(R.id.graph2);
        graph2.setTitle("Temperature");
        gridLabel = graph2.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time");
        gridLabel.setVerticalAxisTitle("Temperature (degrees C)");
        gridLabel.setVerticalAxisTitleColor(Color.parseColor("#3F51B5"));
        gridLabel.setHorizontalAxisTitleColor(Color.parseColor("#3F51B5"));
        graph2.setTitleColor(Color.parseColor("#3F51B5"));
        series2 = new LineGraphSeries<>();
        for (Map.Entry<String, Double> entry : tempMap.entrySet()) {
            series2.appendData(new DataPoint(Date.parse(entry.getKey()),entry.getValue()),false,100);
        }
        graph2.addSeries(series2);
        graph2.getViewport().setMinX(Date.parse(loadMap("TempMap").firstEntry().getKey()));
        graph2.getViewport().setMaxX(Date.parse(loadMap("TempMap").lastEntry().getKey()));
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return sdf.format(value);
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });
        l.unlock();
        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATE_UI");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //update ui
                Lock l = ((MyApplication)getApplicationContext()).lock;
                l.lock();
                if(loadMap("HeartMap").lastEntry() != null){
                    heart.setText(loadMap("HeartMap").lastEntry().getValue().toString() + " bpm");
                    Map.Entry<String, Double> e = loadMap("HeartMap").lastEntry();
                    series.appendData(new DataPoint(Date.parse(e.getKey()), e.getValue()), true, 100);
                    graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                        @Override
                        public String formatLabel(double value, boolean isValueX) {
                            if (isValueX) {
                                return sdf.format(value);
                            } else {
                                return super.formatLabel(value, isValueX);
                            }
                        }
                    });
                    graph.getViewport().setMinX(Date.parse(loadMap("HeartMap").firstEntry().getKey()));
                    graph.getViewport().setMaxX(Date.parse(loadMap("HeartMap").lastEntry().getKey()));
                }

                if(loadMap("TempMap").lastEntry() != null){
                    temp.setText(loadMap("TempMap").lastEntry().getValue().toString() + " °C");
                    Map.Entry<String, Double> e = loadMap("TempMap").lastEntry();
                    series2.appendData(new DataPoint(Date.parse(e.getKey()), e.getValue()), false, 100);
                    graph2.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                        @Override
                        public String formatLabel(double value, boolean isValueX) {
                            if (isValueX) {
                                return sdf.format(value);
                            } else {
                                return super.formatLabel(value, isValueX);
                            }
                        }
                    });
                    graph2.getViewport().setMinX(Date.parse(loadMap("TempMap").firstEntry().getKey()));
                    graph2.getViewport().setMaxX(Date.parse(loadMap("TempMap").lastEntry().getKey()));
                }
                l.unlock();
            }
        };
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.graph_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.back:
                finish();
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
