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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
    private String mGateway;

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
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Info info = infoList.get(position);
                Toast.makeText(getApplicationContext(), info.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);

                intent.putExtra("title", info.getTitle());
                intent.putExtra("description", info.getDescription());
                intent.putExtra("detailedDescription", info.getDetailedDescription());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
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

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    private class runJsoup extends AsyncTask<String, Void, String> {
        private String result;

        @Override
        protected String doInBackground(String... params) {

            try {
                // Connect to the web site
                Document document = Jsoup.connect("http://" + mGateway + "/hnap1").get();
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

            //Checking scan result size
            mWifis = new String[wifiScanList.size()];

            //Obtaining relevant info from ScanResults
            for(int i = 0; i < wifiScanList.size(); i++){
                mWifis[i] = ((wifiScanList.get(i)).SSID + '\n' );
            }

            ScanResult currentNetwork = getCurrentWifi(wifiScanList);

            if (currentNetwork != null){

                mAdv = new WifiAdivisor(currentNetwork);
                mAdv.setDHCP(mDhcpInfo);
                mGateway = mAdv.getGateway();

                String title, desc, det;
                String[] temp, temp2, temp3, temp4;
                ArrayList<String> found;

                title = "Current Network:";
                desc = mAdv.getSSID();
                temp = mAdv.getCap();
                found = mAdv.enSecTypes();
                temp2 = found.toArray(new String[found.size()]);
                found = mAdv.enEncTypes();
                temp3 = found.toArray(new String[found.size()]);
                found = mAdv.enSet();
                temp4 = found.toArray(new String[found.size()]);
                det = "Here are all the connection capabilities " +
                        "open on your router right now:\n" + stringArrayToString(temp) +
                        "\nHere are your enabled security types: \n"+
                        stringArrayToString(temp2) +
                        "\nHere are your enabled encryption types: \n"+
                        stringArrayToString(temp3) +
                        "\nHere are your enabled settings: \n"+
                        stringArrayToString(temp4);

                addInfo(title, desc, det);

                title = "Wifi Connection:";
                if (mAdv.isSecure()) desc = "Secure";
                else desc = "Insecure";
                det = "Here are the approved security types: \n" +
                        stringArrayToString(mAdv.getAccpSec()) +
                        "\nHere are the approved encryption types: \n" +
                        stringArrayToString(mAdv.getAccEnc()) +
                        "\nHere are the approved settings: \n" +
                        stringArrayToString(mAdv.getAccSet());

                addInfo(title, desc, det);

                new runJsoup().execute();
                Toast.makeText(MainActivity.this, "Scan Finished", Toast.LENGTH_LONG).show();

//                new DisplayTask().execute("\n\n" + "Security check: " + mAdv.isSecAppr());
//                new DisplayTask().execute("\n\n" + "Encryption check: " + mAdv.isEncAppr());
//                new DisplayTask().execute("\n\n" + "Settings: " + mAdv.isSetAppr());

                /*
                if (mAdv.isKnown()) {
                    ArrayList<String> enabled = mAdv.enSecTypes();
                    if (!enabled.isEmpty()) {
                        for (int x = 0; x < enabled.size(); x++) {

                        }
                    }

                    enabled = mAdv.enEncTypes();
                    if (!enabled.isEmpty()) {
                        for (int x = 0; x < enabled.size(); x++) {

                        }
                    }

                    enabled = mAdv.enSet();
                    if (!enabled.isEmpty()) {
                        for (int x = 0; x < enabled.size(); x++) {

                        }
                    }
                }
                */

            }

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


