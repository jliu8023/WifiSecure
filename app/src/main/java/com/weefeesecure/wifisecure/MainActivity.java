package com.weefeesecure.wifisecure;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import android.net.ConnectivityManager;
import android.content.BroadcastReceiver;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DefaultItemAnimator;

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

    private List<ScanResult> mScan;
    private WifiManager mWFMan;
    private ConnectivityManager mConMan;

    private DhcpInfo mDhcpInfo;
    private WifiInfo mConInfo;
    private NetworkInfo mActiveNetwork;

    private WifiScanReceiver mWifiReceiver;

    private TextView mOut;

    private String mWifis[];
    private String mInfo[];

    private List<Info> infoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private InfoAdapter mAdapter;

    private WifiAdivisor mAdv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler);

        mAdapter = new InfoAdapter(infoList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdv != null) {
                    Snackbar.make(v, "Your router is located within "+mAdv.getDist()+" meters.",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });

        mWFMan = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WifiScanReceiver();
        mConMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        mActiveNetwork = mConMan.getActiveNetworkInfo();
        mDhcpInfo = mWFMan.getDhcpInfo();
        mConInfo = mWFMan.getConnectionInfo();

        //Check if wifi connection is detected
        mIsConnected = mActiveNetwork != null && mActiveNetwork.isConnectedOrConnecting();
        mIsWifi = mActiveNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if (mIsConnected && mIsWifi) {
            Toast.makeText(MainActivity.this, "Wifi Connection Detected", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(MainActivity.this,"Wifi Not Detected", Toast.LENGTH_LONG).show();


    }

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
        if (id == R.id.action_start) {
            infoList.clear();
            mAdapter.notifyDataSetChanged();
            runScans();
        };
        return super.onOptionsItemSelected(item);
    }

    private void addInfo(String title, String description, String detailedDescription) {
        Info info = new Info(title,description,detailedDescription);
        infoList.add(info);
        mAdapter.notifyDataSetChanged();
    }

    //AsyncTask to display String given on TextView
    private class DisplayTask extends AsyncTask< String, Void, String> {
        protected String doInBackground(String... givenString){
            return givenString[0];
        }
        //Appending TextView with new string
        protected void onPostExecute( String outString ){
            mOut = (TextView) findViewById(R.id.results_description);
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
            mOut = (TextView) findViewById(R.id.results_description);
            mOut.setText("");
        }
    }

    private class runJsoup extends AsyncTask<String, Void, String> {
        private String result;

        @Override
        protected String doInBackground(String... params) {

            try {
                // Connect to the web site
                Document document = Jsoup.connect("http://" + mInfo[2] + "/hnap1").get();
                // Get the html document title
                result = "Your router has HNAP1 enabled. It is HIGHLY recommended that you purchase a new router.";
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute( String outString ){
            if (outString != null)
                addInfo("HNAP1 enabled:","true",outString);
            else
                addInfo("HNAP1 enabled:","false","Your router is HNAP1 safe");
        }
    }

    private void runScans() {

        mActiveNetwork = mConMan.getActiveNetworkInfo();
        mDhcpInfo = mWFMan.getDhcpInfo();
        mConInfo = mWFMan.getConnectionInfo();

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
        ans +=  "\n\n" + "Gateway" + Html.fromHtml("<html><a href=\"http://"
                + given[3] + "\">" + given[3] + "</a></html>").toString() + "\t\t";
        ans +=  "\n\n" + "DHCP Server" + "\t\t" + given[12];
        ans +=  "\n\n" + "DNS Server 1" + "\t\t" + given[7];
        ans +=  "\n\n" + "DNS Server 2" + "\t\t" + given[9];
        return ans;
    }

    private String stringArrayToString(String[] arr) {
        String res = "";
        for (int i=0;i<arr.length;++i) {
            res = res + arr[i] + "\n";
        }
        return res;
    }

    //Broadcast Receiver for finding available WiFi connections
    private class WifiScanReceiver extends BroadcastReceiver{
        //Method to run when receiving scan
        public void onReceive(Context c, Intent intent) {

            //Getting ScanResults
            List<ScanResult> wifiScanList = mWFMan.getScanResults();
            // checking scan result size
            //new DisplayTask().execute("\n\n"+Integer.toString(wifiScanList.size()));
            mWifis = new String[wifiScanList.size()];

            //Obtaining relevant info from ScanResults
            for(int i = 0; i < wifiScanList.size(); i++){
                mWifis[i] = ((wifiScanList.get(i)).SSID + '\n' );
            }

            //Display ScanResult info
            //new DisplayTask().execute( mWifis[0]);

            ScanResult currentNetwork = getCurrentWifi(wifiScanList);

            if (currentNetwork != null){

                addInfo("Current Network:",currentNetwork.SSID,currentNetwork.capabilities);

                mAdv = new WifiAdivisor(currentNetwork);

                String[] cap = mAdv.getCap();

                addInfo("Connection Security:",""+mAdv.isSecure(),"Here are all the connection capabilities " +
                        "open on your router right now:\n" + stringArrayToString(cap));

//                new DisplayTask().execute("\n\n" + "Security check: " + mAdv.isSecAppr());
//                new DisplayTask().execute("\n\n" + "Encryption check: " + mAdv.isEncAppr());
//                new DisplayTask().execute("\n\n" + "Settings: " + mAdv.isSetAppr());

/*                if (mAdv.isKnown()) {
                    new DisplayTask().execute("\n\n" + "Enabled:");
                    ArrayList<String> enabled = mAdv.enSecTypes();
                    if (!enabled.isEmpty()) {
                        for (int x = 0; x < enabled.size(); x++) {
                            new DisplayTask().execute("\n" + enabled.get(x));
                        }
                    }

                    enabled = mAdv.enEncTypes();
                    if (!enabled.isEmpty()) {
                        for (int x = 0; x < enabled.size(); x++) {
                            new DisplayTask().execute("\n" + enabled.get(x));
                        }
                    }

                    enabled = mAdv.enSet();
                    if (!enabled.isEmpty()) {
                        for (int x = 0; x < enabled.size(); x++) {
                            new DisplayTask().execute("\n" + enabled.get(x));
                        }
                    }
                }*/

            }

/*            mScan = wifiScanList;


            String advice = checkSecType(currentNetwork);

            new DisplayTask().execute("\n\n" + advice);
            new DisplayTask().execute("\n\n" + currentNetwork.toString());
            new DisplayTask().execute("\n\n" + mDhcpInfo.toString());
            new DisplayTask().execute("\n\n" + mActiveNetwork.toString());
            new DisplayTask().execute("\n\n" + mConInfo.toString());


            //Display gateway


            new DisplayTask().execute("\n\n" + "\n\n" + advice);*/
            String advice = getDHCPStr();
            new runJsoup().execute();

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


