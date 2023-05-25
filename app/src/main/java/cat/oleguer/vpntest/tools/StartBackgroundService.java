package cat.oleguer.vpntest.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class StartBackgroundService {
    private static final String TAG = "StartBackgroundService";
    private static int INTERVAL = 5000; // Interval for repeating the process in milliseconds
    private static String HOSTNAME = "";
    private static int START_PORT = 80;
    private static int END_PORT = 81;
    private static String IPGEOLOCATION_API_KEY = "";

    /**
     * Starts the Background service inserting some configuration parameters into the Intent
     * @param context
     */
    public static void startBackgroundService (Context context){

        Intent startIntent = new Intent(context, BackgroundService.class);
        Log.d(TAG, "Comencem a mirar el servidor" + startIntent.toString());

        retrieveConfigurationVariables(context);

        startIntent.setAction("START");
        startIntent.putExtra("hostname", HOSTNAME);
        startIntent.putExtra("startPort", START_PORT);
        startIntent.putExtra("endPort", END_PORT);
        startIntent.putExtra("interval", INTERVAL);
        startIntent.putExtra("ipgeolocation_api_key",IPGEOLOCATION_API_KEY);
        context.startService(startIntent);

    }


    /**
     * Retrieves the configuration variables from the build.gradle file and stores it to insert it to the Intent
     *
     * @param context
     */
    private static void retrieveConfigurationVariables(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Bundle metaData = applicationInfo.metaData;

            if (metaData != null && metaData.containsKey("INTERVAL")) {
                INTERVAL = metaData.getInt("INTERVAL");
                Log.d(TAG,"Configuration value found: INTERVAL: " + INTERVAL);
            } else {
                // Default value if INTERVAL is not found
                Log.d(TAG,"Configuration value not found in file build.gradle: manifestPlaceholders = [INTERVAL: \"1000\"]: ");
                INTERVAL = 5000;
            }
            if (metaData != null && metaData.containsKey("HOSTNAME")) {
                HOSTNAME = metaData.getString("HOSTNAME");
                Log.d(TAG,"Configuration value found: HOSTNAME: " + HOSTNAME);
            } else {
                // Default value if INTERVAL is not found
                Log.d(TAG,"Configuration value not found in file build.gradle: manifestPlaceholders = [HOSTNAME: \"example.com\"]: ");
                HOSTNAME = "";
            }
            if (metaData != null && metaData.containsKey("START_PORT")) {
                START_PORT = metaData.getInt("START_PORT");
                Log.d(TAG,"Configuration value found: START_PORT: " + START_PORT);
            } else {
                // Default value if START_PORT is not found
                Log.d(TAG,"Configuration value not found in file build.gradle: manifestPlaceholders = [START_PORT: \"80\"]: ");
                START_PORT = 80;
            }
            if (metaData != null && metaData.containsKey("END_PORT")) {
                END_PORT = metaData.getInt("END_PORT");
                Log.d(TAG,"Configuration value found: END_PORT: " + END_PORT);
            } else {
                // Default value if END_PORT is not found
                Log.d(TAG,"Configuration value not found in file build.gradle: manifestPlaceholders = [END_PORT: \"84\"]: ");
                END_PORT = 84;
            }
            if (metaData != null && metaData.containsKey("IPGEOLOCATION_API_KEY")) {
                IPGEOLOCATION_API_KEY = metaData.getString("IPGEOLOCATION_API_KEY");
                Log.d(TAG,"Configuration value found: IPGEOLOCATION_APY_KEY: " + IPGEOLOCATION_API_KEY);
            } else {
                // Default value if IPGEOLOCATION_APY_KEY is not found
                Log.d(TAG,"Configuration value not found in file build.gradle: manifestPlaceholders = [IPGEOLOCATION_API_KEY: \"XXXX\"]: ");
                IPGEOLOCATION_API_KEY = "";
            }
        } catch (PackageManager.NameNotFoundException e) {
            // Handle exception
            e.printStackTrace();
            // Default value if an exception occurs
            INTERVAL = 5000;
        }
    }

}
