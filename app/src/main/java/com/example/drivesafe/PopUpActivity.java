package com.example.drivesafe;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.location.LocationManager;

public class PopUpActivity extends AppCompatActivity implements LocationListener {
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;
    LocationManager locationManager;

    String userId;
    String lon;
    String lat;

    public void sendBtMsg(String msg2send) {
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID
        try {

            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            if (!mmSocket.isConnected()) {
                mmSocket.connect();
            }

            String msg = msg2send;
            //msg += "\n";
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void callServer() {
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", userId);
        params.put("latitude",lat );
        params.put("longitude",lon);
                Toast.makeText(PopUpActivity.this,"sending", Toast.LENGTH_SHORT).show();
        JsonObjectRequest req = new JsonObjectRequest(URLs.URL_HELP, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            Toast.makeText(PopUpActivity.this,response.toString(), Toast.LENGTH_SHORT).show();
                            VolleyLog.v("Response:%n %s", response.toString(4));
                            Intent  intent = new Intent(getApplicationContext(), PopUpActivity.class);
                            intent.putExtra("id", response.getJSONObject("data").get("id").toString());
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PopUpActivity.this,error.getMessage(), Toast.LENGTH_LONG).show();
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        mRequestQueue.add(req);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_up);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

        userId = getIntent().getExtras().getString("id");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);

        final Handler handler = new Handler();

        final TextView myLabel = (TextView) findViewById(R.id.btResult);
        final Button tempButton = (Button) findViewById(R.id.tempButton);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final class workerThread implements Runnable {

            private String btMsg;

            public workerThread(String msg) {
                btMsg = msg;
            }

            public void run()
            {
                sendBtMsg(btMsg);
                while(!Thread.currentThread().isInterrupted())
                {
                    int bytesAvailable;
                    boolean workDone = false;

                    try {



                        final InputStream mmInputStream;
                        mmInputStream = mmSocket.getInputStream();
                        bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {

                            byte[] packetBytes = new byte[bytesAvailable];
                            byte[] readBuffer = new byte[1024];
                            mmInputStream.read(packetBytes);

                            final String data = new String(packetBytes, "US-ASCII");
                            handler.post(new Runnable()
                            {
                                public void run()
                                {

                                    myLabel.setText(data);
                                    if(data.equals("1")) {
                                        callServer();
                                    }

                                }
                            });

                            workDone = true;

                            if (workDone == true){
                                mmSocket.close();
                                break;
                            }

                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        };


        // start temp button handler

        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on temp button click

                (new Thread(new workerThread("temp"))).start();

            }
        });


        //end temp button handler


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("raspberrypi")) //Note, you will need to change this to match the name of your device
                {
                    Log.e("SafeDrive",device.getName());
                    mmDevice = device;
                    break;
                }
            }
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        lon = "" + location.getLongitude();
        lat = "" + location.getLatitude();
        Toast.makeText(PopUpActivity.this,"" +location.getLongitude() , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(PopUpActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }
}

