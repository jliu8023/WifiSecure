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
        return (isSecAppr() && isEncAppr() && isSetAppr());
    }

    //Checks if known settings are found
    public boolean isKnown(){
        return (isTypeSec() || isTypeUnsec());
    }

    //Checks for each approved type
    public boolean isSecAppr(){
        return (checkList(mApprSecTypes) && !checkList(mDisaSecTypes));
    }

    public boolean isEncAppr(){
        return (checkList(mApprEncTypes) && !checkList(mDisaEncTypes));
    }

    public boolean isSetAppr(){
        return (checkList(mSecSets) && !checkList(mUnsecSets));
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

    //Get methods for accepted settings
    public String[] getAccpSec(){
        return mApprSecTypes;
    }

    public String[] getAccEnc(){
        return mApprEncTypes;
    }

    public String[] getAccSet(){
        return mSecSets;
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
        boolean secCheck = checkList(mApprSecTypes);
        boolean encCheck = checkList(mApprEncTypes);
        boolean setCheck = checkList(mSecSets);
        return (secCheck && encCheck && setCheck);
    }

    //Check if a unsecured security and encryption type is used
    private boolean isTypeUnsec(){
        boolean secCheck = checkList(mDisaSecTypes);
        boolean encCheck = checkList(mDisaEncTypes);
        boolean setCheck = checkList(mUnsecSets);
        return (secCheck && encCheck && setCheck);
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


    //Private method for checking security, encryption, and setting types list
    private boolean checkList(String[] list){
        boolean check = false;

        for (int x = 0; x < list.length; x++){
            if (isEnabled(list[x])){
                check = true;
                break;
            }
        }

        return check;
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
