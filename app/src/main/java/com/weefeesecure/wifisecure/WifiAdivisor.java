package com.weefeesecure.wifisecure;

import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class WifiAdivisor {

    private ScanResult mGivenScan;
    private String mCapabilities;
    private String[] mInfo = new String[6];

    //Secure settings
    final private String[] mApprSecTypes = {"WPA2"};
    final private String[] mApprEncTypes = {"CCMP", "AES", "PERSONAL"};
    final private String[] mSecSets ={"ESS"};

    //Unsecure settings
    final private String[] mDisaSecTypes = {"WEP", "WPA-"};
    final private String[] mDisaEncTypes = {"TKIP", "ENTERPRISE"};
    final private String[] mUnsecSets = {"WPS"};

    //Constructor needs a ScanResult
    WifiAdivisor(ScanResult givenScan){
        mGivenScan = givenScan;
        mCapabilities = givenScan.capabilities;
    }

    public String[] getCap(){
        String found = mCapabilities.replaceAll("\\[","");
        return found.split("\\]");
    }

    public boolean isNotNull() {return mGivenScan != null;};

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

    //Get Ip
    public String getIP() { return mInfo[0]; }

    //Get Subnet
    public String getSubnet() { return mInfo[1]; }

    //Get Gateway
    public String getGateway() { return mInfo[2]; }

    //Get DHCP Server
    public String getDHCPServer() { return mInfo[3]; }

    //Get DNS 1
    public String getDNS1() { return mInfo[4]; }

    //Get DNS 2
    public String getDNS2() { return mInfo[5]; }

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

    //Parsing DHCP info for relevant info
    private void updateDHCP(DhcpInfo dhcp){
        String ans = null;
        if (dhcp != null) {
            String given[] = dhcp.toString().split(" ");
            mInfo[0] = given[1]; // Ip Address
            mInfo[1] = given[5]; // Subnet Mask
            mInfo[2] = given[3]; // Gateway
            mInfo[3] = given[12]; // DHCP Server
            mInfo[4] = given[7]; // DNS Server 1
            mInfo[5] = given[9]; // DNS Server 2
        }
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
