package com.weefeesecure.wifisecure;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Formatter;
import java.util.List;

import android.net.ConnectivityManager;
import android.content.BroadcastReceiver;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class MainActivity extends AppCompatActivity {

    /*
    private TextView mOut;
    private InfoThread mThread;

    private boolean mIsConnected;
    private boolean mIsWifi;
    private boolean mScanResultReady = false;

    private NetworkInfo mActiveNetwork;
    private ConnectivityManager mConMan;

    private DhcpInfo mDhcpInfo;
    private WifiInfo mConInfo;
    */

    private List<ScanResult> mScan;
    private ListView mLV;
    private WifiManager mWFMan;
    WifiScanReceiver mWifiReceiver;
    String mWifis[];
    String mSSID[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLV = (ListView)findViewById(R.id.listView);

        mWFMan = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WifiScanReceiver();
        mWFMan.startScan();

        /*
        mConMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mActiveNetwork = mConMan.getActiveNetworkInfo();
        mDhcpInfo = mWFMan.getDhcpInfo();
        mConInfo = mWFMan.getConnectionInfo();
        mIsConnected = mActiveNetwork != null && mActiveNetwork.isConnectedOrConnecting();
        mIsWifi = mActiveNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        //mhandler.post(wifiRunnable);
       // mScan = findWifi(mWFMan);
        if (mIsConnected && mIsWifi) {
            Toast.makeText(MainActivity.this, "Wifi Connected", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(MainActivity.this,"Wifi Not Connected", Toast.LENGTH_LONG).show();
        */

    }

    /*
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
    */

    /*
    private class InfoThread extends Thread{
        private boolean mRunning = false;

        void Info (){
            if ( ! Thread.interrupted() && mRunning ){
                if ( !Thread.interrupted() ){

                    if (mWifis[0] != null)
                        //new DisplayTask().execute("\n" + mWifis[0] );
                        new DisplayTask().execute("\n"+mActiveNetwork.toString()+
                                "\n\n"+mDhcpInfo.toString() + "\n\n" +mConInfo.toString());
                    else

                        new DisplayTask().execute("\n"+mActiveNetwork.toString()+
                                "\n\n"+mDhcpInfo.toString() + "\n\n" +mConInfo.toString());
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
    */

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }
    */



    private class WifiScanReceiver extends BroadcastReceiver{
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = mWFMan.getScanResults();
            mWifis = new String[wifiScanList.size()];

            for(int i = 0; i < wifiScanList.size(); i++){
                mWifis[i] = ((wifiScanList.get(i)).SSID + '\n'
                        + (wifiScanList.get(i)).capabilities);
            }
            mLV.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,mWifis));
        }
    }

    protected void onPause() {
        unregisterReceiver(mWifiReceiver);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }
}

