package com.weefeesecure.wifisecure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView mOut;
    private InfoThread mThread;
    private boolean mIsConnected;
    private boolean mIsWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mIsConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        mIsWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if (mIsConnected && mIsWifi) {
            Toast.makeText(MainActivity.this, "Wifi Connected", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(MainActivity.this,"Wifi Not Connected", Toast.LENGTH_LONG).show();

    }

    //AsyncTask to display String given on TextView
    private class DisplayTask extends AsyncTask< String, Void, String> {
        protected String doInBackground(String... givenString){
            return givenString[0];
        }

        protected void onPostExecute( String outString ){
            mOut = (TextView) findViewById(R.id.results);
            mOut.append(outString);
        }
    }

    public void startButton(View v){
        Button button = (Button) v;
        //Referencing EditText and TextView
        mOut = (TextView) findViewById(R.id.results);
        //Enable scrolling in TextView for more results
        mOut.setMovementMethod(new ScrollingMovementMethod());

        //Starting thread
        mThread = new InfoThread();
        mThread.start();
    }

    private class InfoThread extends Thread{

        private boolean mRunning = false;

        void Info (){
            if ( ! Thread.interrupted() && mRunning ){
                String display= "\n Hello World" ;
                if ( !Thread.interrupted() ){
                    //Start Monte Carlo polling
                    new DisplayTask().execute(display);
                }

            }
        }
        //Override run() with Monte Carlo task
        @Override
        public void run(){
            mRunning = true;
            Info();
        }
        public void close() {
            mRunning = false;
        }
    }
    protected class WifiStateChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent) {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            mIsConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            mIsWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if (mIsConnected && mIsWifi) {
                Toast.makeText(context.getApplicationContext(), "Wifi Connected", Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(context.getApplicationContext(),"Wifi Not Connected", Toast.LENGTH_LONG).show();
        }
    }
}