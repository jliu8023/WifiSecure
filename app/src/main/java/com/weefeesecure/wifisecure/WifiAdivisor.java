package com.weefeesecure.wifisecure;

import android.net.wifi.ScanResult;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class WifiAdivisor {

    private ScanResult mGivenScan;
    private String mCapabilities;

    //Secure settings
    final private String[] mApprSecTypes = {"WPA2"};
    final private String[] mApprEncTypes = {"CCMP", "AES"};
    final private String[] mSecSets ={"n/a"};

    //Unsecure settings
    final private String[] mDisaSecTypes = {"WEP", "WPA-"};
    final private String[] mDisaEncTypes = {"TKIP"};
    final private String[] mUnsecSets = {"WPS"};

    //Constructor needs a ScanResult
    WifiAdivisor(ScanResult givenScan){
        mGivenScan = givenScan;
        mCapabilities = givenScan.capabilities;
    }

    //Compares list of enabled secure settings to unsecure settings
    public boolean isSecure(){
        return (isTypeSec() && !isTypeUnsec());
    }

    public boolean isKnown(){
        return (isTypeSec() || isTypeUnsec());
    }

    //Update ScanResult
    public void updateScan(ScanResult givenScan){
        mGivenScan = givenScan;
    }

    //Get SSID
    public String getSSID(){
        return mGivenScan.SSID;
    }

    //Methods to find enabled settings off of list
    public ArrayList<String> enSecTypes(){
        return enList(mApprSecTypes, mDisaSecTypes);
    }

    public ArrayList<String> enEncTypes(){
        return enList(mApprEncTypes, mDisaEncTypes);
    }

    public ArrayList<String> enSet(){
        return enList(mSecSets, mUnsecSets);
    }

    //Return distance from router in the form of "##.##"
    public String getDist(){
        return new DecimalFormat("##.##").format(calcDist());
    }

    //Check if feature is enabled
    private boolean isEnabled(String given){
        return mCapabilities.contains(given);
    }

    //Check if an approved security and encryption type is used
    private boolean isTypeSec(){
        return checkList(mApprSecTypes, mApprEncTypes, mSecSets);
    }

    /*
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
    */
    //Check if a unsecured security and encryption type is used
    private boolean isTypeUnsec(){
        return checkList(mDisaSecTypes, mDisaEncTypes, mUnsecSets);
    }

    //Find enabled features out of given list
    private ArrayList<String> enList(String[] appList, String[] disaList){
        ArrayList<String> found = new ArrayList<String>(0);

        for (int x = 0; x < appList.length; x++){
            if (isEnabled(appList[x])){
                found.add(appList[x]);
                break;
            }
        }

        for (int x = 0; x < disaList.length; x++){
            if (isEnabled(disaList[x])){
                found.add(disaList[x]);
                break;
            }
        }

        return found;
    }


    //Private method for checking security and encryption types list
    private boolean checkList(String[] secList, String[] encList, String[] setList){
        boolean secCheck = false;
        boolean encCheck = false;
        boolean setCheck = false;

        for (int x = 0; x < secList.length; x++){
            if (isEnabled(secList[x])){
                secCheck = true;
                break;
            }
        }

        for (int x = 0; x < encList.length; x++){
            if (isEnabled(encList[x])){
                encCheck = true;
                break;
            }
        }

        for (int x = 0; x < setList.length; x++){
            if (isEnabled(setList[x])){
                setCheck = true;
                break;
            }
        }

        return (secCheck && encCheck && setCheck);
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
