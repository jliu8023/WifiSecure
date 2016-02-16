package com.weefeesecure.wifisecure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
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

import java.util.Formatter;

public class MainActivity extends AppCompatActivity {

    private TextView mOut;
    private InfoThread mThread;
    private boolean mIsConnected;
    private boolean mIsWifi;
    private NetworkInfo mActiveNetwork;
    private ConnectivityManager mConMan;
    private WifiManager mWFMan;
    private DhcpInfo mDhcpInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mWFMan = (WifiManager)getSystemService(WIFI_SERVICE);
        mActiveNetwork = mConMan.getActiveNetworkInfo();
        mDhcpInfo = mWFMan.getDhcpInfo();
        mIsConnected = mActiveNetwork != null && mActiveNetwork.isConnectedOrConnecting();
        mIsWifi = mActiveNetwork.getType() == ConnectivityManager.TYPE_WIFI;
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
                if ( !Thread.interrupted() ){
                    new DisplayTask().execute("\n"+mActiveNetwork.toString()+"\n"+mDhcpInfo.toString());
                }

            }
        }
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
            mConMan = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            mActiveNetwork = mConMan.getActiveNetworkInfo();
            mIsConnected = mActiveNetwork != null && mActiveNetwork.isConnectedOrConnecting();
            mIsWifi = mActiveNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            if (mIsConnected && mIsWifi) {
                new showTextThread("Wifi Connected").run();
            }
            else
                new showTextThread("Wifi Not Connected").run();
        }
        protected class showTextThread extends Thread {
            private String text;
            showTextThread(String text) {
                this.text = text;
            };
            private void showToastText() {
                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
            }
            @Override
            public void run() {
                showToastText();
            }
        }
    }
}