package com.liz.neuroscale.android.activities;

import android.os.Environment;
import android.util.Log;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import ExtraUtils.HTTP.CommHTTP;

/**
 * Created by kunaalgoel on 4/18/16.
 */
public class Bulb
{
    private final String lifxToken = "cd93714d188bd179dbcad7ac943219ac2b3a6a9626e34410f0ad0e65446af5ab";
    private static final long TIME_OF_COMPARISON=3000;
    private static final long TIME_OF_CALIBRATION=20000;
    long time;
    private boolean firstRun = true;
    private boolean firstRunCalibration=true;
    long timeCal;
    private double cma_tot = 0;
    private double cma_sub = 0;
    private int count = 1;
    private int count_sub=1;
    CommHTTP mHttp = new CommHTTP();

    public Bulb()
    {
        SetBrightness(.5); // to test if method works.
    }

    /**
     * Receiving the input from NeuroScale, the data will be added to the objects internal data
     * structure for analysis
     * @param d
     */
    public void addData(double d)
    {
        if (isCalibrated()) {
            calculateSubCMA(d);
            calculateTotalCMA(d);
            //Log.d("MyActivity", "CMA " + cma_tot);
            if (firstRun) //startup
            {
                time = System.currentTimeMillis();
                Log.d("My Activity", "time" + time);
                Log.d("My Activity", "currentTimeMillis" + System.currentTimeMillis());
                firstRun = false;
                analyzeData();
            }
            if (System.currentTimeMillis() - time > TIME_OF_COMPARISON) {
                time = System.currentTimeMillis();
                analyzeData();
            }
        }
    }

    /**
     * The first 20seconds of the muse are calibration and thus nothing will be done by system.
     * @return Whether the Muse EEG sensor is calibrated
     */
    private boolean isCalibrated()
    {
        if (firstRunCalibration)
        {
            timeCal=System.currentTimeMillis();
            firstRunCalibration= false;
            return false;
        }
        else
        {
            if (System.currentTimeMillis()-timeCal>TIME_OF_CALIBRATION)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * Analyzes the data given that enough data is stored within the Bulb.
     */
    private void analyzeData()
    {
        double bright = 1 - cma_sub;
        if (bright < .1) {
            bright = .1;
        }
        SetBrightness(bright);
        count_sub = 1;
        cma_sub = 0;
    }

    /**
     * Calculates the CMA of all the data
     * @param currentData
     */
    private void calculateTotalCMA(double currentData)
    {
        cma_tot = (currentData + (count-1)* cma_tot)/count;
        count++;
    }

    /**
     * Calculates the CMA of the data from t to t+TIME OF COMPARISON
     * @param currentData
     */
    private void calculateSubCMA(double currentData)
    {
        cma_sub = (currentData + (count_sub-1)*cma_sub)/count_sub;
        count_sub++;
    }

//
//    /**
//     * Changes brightness works
//     */
//    private void changeBrightness(double br)
//    {
//        try
//        {
//            URL url = new URL("https://api.lifx.com/v1/lights/all/state");
//            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
//            httpCon.setDoOutput(true);
//
//            httpCon.setRequestMethod("PUT");
//            httpCon.setRequestProperty("Authorization", "Bearer " + lifxToken);
//            httpCon.setRequestProperty("accept", "application/json");
//            httpCon.setRequestProperty("Content-Type", "application/json");
//            Log.d("MyActivity", "Line 143");
//
//            String color = "blue";
//            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
//            out.write("{\n\t\"power\": \"on\"\n, \n\t\"brightness\": \""+br+"\"}"); //does it work?
//            Log.d("MyActivity", "Line 148");
//
//
//            System.out.println(out.toString());
//            Log.d("MyActivity", out.toString());
//
//            out.close();
//            Log.d("MyActivity", "Line 153");
//
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
//            Log.d("MyActivity", "Line 157");
//
//            String inputLine;
//            while ((inputLine = in.readLine()) != null)
//                System.out.println(inputLine);
//            in.close();
//            System.out.println("Sent API Request");
//        }
//        catch (Exception e)
//        {
//            System.out.println("LIFX API error");
//            e.printStackTrace();
//        }
//    }
    private int SetBrightness(double b)
    {
        String url = "https://api.lifx.com/v1/lights/all/state";
        Map<String, String> stateMap = new HashMap<>();
        stateMap.put("brightness", b+"");
        /**convert to json object*/
        JSONObject stateJson = new JSONObject(stateMap);
        /**put data to server and return response code*/
        return this.mHttp.AsynPUT(url, lifxToken, stateJson);
    }
//    /**
//     * Changes the color with the given, rgb values.
//     * @param r
//     * @param g
//     * @param b
//     */
//    private void changeColor(int r, int g, int b)
//    {
//        try
//        {
//            URL url = new URL("https://api.lifx.com/v1/lights/all/state");
//            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
//            httpCon.setDoOutput(true);
//
//            httpCon.setRequestMethod("PUT");
//            httpCon.setRequestProperty("Authorization", "Bearer " + lifxToken);
//            httpCon.setRequestProperty("accept", "application/json");
//            httpCon.setRequestProperty("Content-Type", "application/json");
//
//
//            String color = "blue";
//            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
//            out.write("{\n\t\"power\": \"on\"\n, \n\t\"color\": \"rgb:" + r + "," + g + ","+b + "\", \n\t\"brightness\": \"1\"}");
//
//            System.out.println(out.toString());
//            out.close();
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
//            String inputLine;
//            while ((inputLine = in.readLine()) != null)
//                System.out.println(inputLine);
//            in.close();
//            System.out.println("Sent API Request");
//        }
//        catch (Exception e)
//        {
//            System.out.println("LIFX API error");
//            e.printStackTrace();
//        }
//    }

}
