package com.example.android.healthdashboard;

import android.app.Application;

import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MyApplication extends Application{
    public TreeMap<Date,Double> tempMap = new TreeMap<>();
    public TreeMap<Date,Double> heartMap = new TreeMap<>();
    public Lock lock = new ReentrantLock();
    public boolean serviceRunning = false;
    public boolean connected = false;

}
