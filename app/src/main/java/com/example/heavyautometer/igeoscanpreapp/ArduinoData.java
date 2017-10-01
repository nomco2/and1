package com.example.heavyautometer.igeoscanpreapp;


/**
 * Created by jinbro on 2016. 8. 12..
 */
public class ArduinoData {

    int[] each_data = null;

    public void setEachData(int distance, int height) {
            each_data = new int[]{distance, height};
    }

}