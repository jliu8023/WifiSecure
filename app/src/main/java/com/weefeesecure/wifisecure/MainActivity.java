package com.weefeesecure.wifisecure;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;

import android.util.Base64;

public class MainActivity extends AppCompatActivity {

    final private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 255;
    private boolean mIsConnected;
    private boolean mIsWifi;

    private NetworkInfo mActiveNetwork;
    private ConnectivityManager mConMan;

    private DhcpInfo mDhcpInfo;
    private WifiInfo mConInfo;

    private TextView mOut;
    private List<ScanResult> mScan;
    private WifiManager mWFMan;
    WifiScanReceiver mWifiReceiver;
    String mWifis[];
    String mInfo[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWFMan = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WifiScanReceiver();

        mConMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mActiveNetwork = mConMan.getActiveNetworkInfo();
        mDhcpInfo = mWFMan.getDhcpInfo();
        mConInfo = mWFMan.getConnectionInfo();
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
        //Appending TextView with new string
        protected void onPostExecute( String outString ){
            mOut = (TextView) findViewById(R.id.results);
            mOut.append(outString);
        }
    }


    //AsyncTask to display String given on TextView
    private class ClearTask extends AsyncTask< String, Void, String> {
        protected String doInBackground(String... givenString){
            return givenString[0];
        }
        //Appending TextView with new string
        protected void onPostExecute( String outString ){
            mOut = (TextView) findViewById(R.id.results);
            mOut.setText("");
        }
    }


    public void startButton(View v){
        Button button = (Button) v;
        //Referencing EditText and TextView
        runScans();
        new runJsoup().execute();
    }

    private class runJsoup extends AsyncTask<String, Void, String> {
        private String result;
        private String encodedAuth;

        @Override
        protected String doInBackground(String... params) {

            try {
                // Connect to the web site
                encodedAuth = Base64.encodeToString("admin:password".getBytes(),Base64.DEFAULT);
                Document document = Jsoup.connect("http://" + mInfo[2]).header("Authentication",encodedAuth).get();
                // Get the html document title
                result = document.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute( String outString ){
            if (result != null)
                new DisplayTask().execute("\n\n"+outString);
            else
                new DisplayTask().execute("\n\n"+"could not connect to router settings");
        }
    }

    private void runScans() {
        mOut = (TextView) findViewById(R.id.results);
        //Enable scrolling in TextView for more results
        mOut.setMovementMethod(new ScrollingMovementMethod());

        mActiveNetwork = mConMan.getActiveNetworkInfo();
        mDhcpInfo = mWFMan.getDhcpInfo();
        mConInfo = mWFMan.getConnectionInfo();
        new ClearTask().execute("");
        Toast.makeText(MainActivity.this, "Starting Scan", Toast.LENGTH_LONG).show();
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CALENDAR);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (!mWFMan.startScan())
            Toast.makeText(MainActivity.this, "Scan failed", Toast.LENGTH_LONG).show();
    }

    private ScanResult getCurrentWifi(List<ScanResult> scanList){
        ScanResult found = null;
        if (scanList != null){
            for (ScanResult currentNetwork : scanList){
                //new DisplayTask().execute("\n\n" + currentNetwork.SSID);
                if (mConInfo.getSSID().contains(currentNetwork.SSID)){
                    found = currentNetwork;
                    break;
                }
            }
        }
        return found;
    }

    private String checkSecType(ScanResult result){
        String advice = null;
        if ((result.capabilities.contains("WPA2")
                ||result.capabilities.contains("WPA2-Personal"))
                && !result.capabilities.contains("TKIP")){
            advice = "Security type is good";
        }
        else
            advice = "The Security type should be WPA2 + AES";
        return advice;
    }

    //Parsing DHCP info for relevant info
    private String getDHCPStr(){
        String ans = null;
        mInfo = new String[6];
        String given[] = mDhcpInfo.toString().split(" ");
        mInfo[0] = given[1]; // Ip Address
        mInfo[1] = given[5]; // Subnet Mask
        mInfo[2] = given[3]; // Gateway
        mInfo[3] = given[12]; // DHCP Server
        mInfo[4] = given[7]; // DNS Server 1
        mInfo[5] = given[9]; // DNS Server 2
        ans = "IP Address" + "\t\t" + given[1] ;
        ans +=  "\n\n" + "Subnet Mask" + "\t\t" + given[5];
        ans +=  "\n\n" + "Gateway" + Html.fromHtml("<html><a href=\"http://" + given[3] + "\">" + given[3] + "</a></html>").toString() + "\t\t";
        ans +=  "\n\n" + "DHCP Server" + "\t\t" + given[12];
        ans +=  "\n\n" + "DNS Server 1" + "\t\t" + given[7];
        ans +=  "\n\n" + "DNS Server 2" + "\t\t" + given[9];
        return ans;
    }

    private String readCapabilities(String given){
        String found = "Unidentified Security type";
        if (given.contains("WPA")){
            found = "WPA";
            if (given.contains("WPA2"))
                found += "/WPA2";
        }
        else if (given.contains("WEP"))
            found = "WEP";
        if (given.contains("TKIP"))
            found += " + TKIP";
        if (given.contains("AES"))
            found += " + AES";
        return found;
    }

    //Broadcast Receiver for finding available WiFi connections
    private class WifiScanReceiver extends BroadcastReceiver{
        //Method to run when receiving scan
        public void onReceive(Context c, Intent intent) {


            //Getting ScanResults
            List<ScanResult> wifiScanList = mWFMan.getScanResults();
            //new DisplayTask().execute("\n\n"+Integer.toString(wifiScanList.size())); //checking scan result size
            mWifis = new String[wifiScanList.size()];

            //Obtaining relevant info from ScanResults
            for(int i = 0; i < wifiScanList.size(); i++){
                mWifis[i] = ((wifiScanList.get(i)).SSID + '\n' );
            }

            //Display ScanResult info
            //new DisplayTask().execute( mWifis[0]);

            ScanResult currentNetwork = getCurrentWifi(wifiScanList);
            if (currentNetwork != null){
                String wifiStr = "\n\n" + currentNetwork.SSID
                        + '\n' + readCapabilities(currentNetwork.capabilities);
                new DisplayTask().execute(wifiStr);
            }

            mScan = wifiScanList;

            String advice = checkSecType(currentNetwork);
            new DisplayTask().execute("\n\n" + advice);

            /*
            new DisplayTask().execute("\n\n" + mDhcpInfo.toString());
            new DisplayTask().execute("\n\n" + mActiveNetwork.toString());
            new DisplayTask().execute("\n\n" + mConInfo.toString());
            */

            //Display gateway

            advice = getDHCPStr();
            new DisplayTask().execute("\n\n" + "\n\n" + advice);

            Toast.makeText(MainActivity.this, "Scan Finished", Toast.LENGTH_LONG).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //runScans();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}

