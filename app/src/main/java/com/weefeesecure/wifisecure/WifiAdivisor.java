package com.weefeesecure.wifisecure;

import android.net.wifi.ScanResult;
import java.lang.Math;
import java.text.DecimalFormat;

public class WifiAdivisor {

    private ScanResult mGivenScan;
    private String[] SecTypes, EncTypes, Sets;

    final private String[] mApprSecTypes = {"WPA2"};
    final private String[] mApprEncTypes = {"CCMP", "AES"};
    final private String[] mSecSets ={""};

    final private String[] mDisaSecTypes = {"WEP", "WPA-"};
    final private String[] mDisaEncTypes = {"TKIP"};
    final private String[] mUnsecSets = {"WPS"};

    //Constructor needs a ScanResult
    WifiAdivisor(ScanResult givenScan){
        mGivenScan = givenScan;
    }

    //Compares list of enabled secure settings to unsecure settings
    public boolean isSecure(){
        return (isTypeSec() && !isTypeUnsec());
    }

    //Update ScanResult
    public void updateScan(ScanResult givenScan){
        mGivenScan = givenScan;
    }

    //Get SSID
    public String getSSID(){
        return mGivenScan.SSID;
    }

    //Return distance from router in the form of "##.##"
    public String getDist(){
        String dec = new DecimalFormat("##.##").format(calcDist());
        return dec;
    }

    //Check if feature is enabled
    private boolean isEnabled(String given){
        String capabilities = mGivenScan.capabilities;
        return capabilities.contains(given);
    }

    //Check if an approved security and encryption type is used
    private boolean isTypeSec(){
        return checkList(mApprSecTypes, trunStr(mApprEncTypes,mSecSets));
    }

    //Truncate string arrays
    private String[] trunStr(String[] given1, String[] given2){
        String[] newStr = new String[given1.length + mUnsecSets.length];
        for (int x = 0; x < newStr.length; x++){
            if (x < given1.length)
                newStr[x] = given1[x];
            else if ((x - given1.length) < given2.length )
                newStr[x - given1.length] = given2[x - given1.length];
        }
        return newStr;
    }

    //Check if a unsecured security and encryption type is used
    private boolean isTypeUnsec(){
        return checkList(mDisaSecTypes, trunStr(mDisaEncTypes,mUnsecSets));
    }

    //Private method for checking security and encryption types list
    private boolean checkList(String[] secList, String[] encList){
        boolean secCheck = false;
        boolean encCheck = false;

        for (int x = 0; x < mApprSecTypes.length; x++){
            if (isEnabled(mApprSecTypes[x])){
                secCheck = true;
                break;
            }
        }

        for (int x = 0; x < mApprEncTypes.length; x++){
            if (isEnabled(mApprSecTypes[x])){
                encCheck = true;
                break;
            }
        }

        return (secCheck & encCheck);
    }

    /*
        Calculate approx. distance from router based on free space path loss formula.
        Formula finds approx. distance from router if there is no interference between the
        device and the router.
     */
    private double calcDist(){
        double ans = (27.55 - (20 * Math.log10(mGivenScan.frequency))
                + Math.abs(mGivenScan.level)) / 20;
        ans = Math.pow(10, ans);
        return ans;
    }
}
