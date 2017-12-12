package com.htw.s0551733.bvg;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private Button button_stop;
    private TextView text;
    //Location service
    private LocationManager locationManager;
    //Listen for location changes
    private LocationListener listener;
    private String URL_ADDRESS = "hallo webserver";
    private int ID = 1;
    private Date time = Calendar.getInstance().getTime();




    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.Start_button);
        button_stop = (Button) findViewById(R.id.Stop_button);

        //Location via GPS anfragen ist ein System Service
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {

            //Immer wenn de Location upgedatet wird
            @Override
            public void onLocationChanged(Location location) {
                text.append("\n "  + location.getLatitude()+" " + location.getLongitude());
                //text.setText("\n " + location.getLongitude() + " " + location.getLatitude());
                //sendPost(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            //Falls GPS aus ist wird der user zu den Location settings geführt
            @Override
            public void onProviderDisabled(String s) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        configure_button();
    }

    //Locationmanager abzuschalten
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button(){


        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDestroy();
            }
        });


        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //String array mit Permission die wir in die AndroidManifest datei geschrieben haben, request code kann eine randomnummer sein, wichtig aber fuer "onRequestPermissionResult
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                //minTime = wie oft sollen neue GPS daten geholt werden, minDistance bei welcher entfernung sollen neue GPS daten geholt werden
                locationManager.requestLocationUpdates("gps", 10000, 0, listener);
            }
        });
    }

    public void sendPost(final Location location) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(URL_ADDRESS);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("latitude", location.getLatitude());
                    jsonParam.put("longitude", location.getLongitude());
                    jsonParam.put("ID", ID);
                    jsonParam.put("TimeStamp", time);


                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();


                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

}