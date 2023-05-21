package cat.oleguer.vpntest.tools;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkTools {
    private static final String TAG = "NetworkTools";

    private Context context;

    public NetworkTools(Context cx){
        setContext(cx);
        ConnectivityManager cm = (ConnectivityManager) cx.getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.d("---olelog---",cm.toString());

    }

    public boolean checkNetwork (){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        //return true;
    }

    public static boolean hasConnection() {
        return false;
    }

    public boolean checkVPN(){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network[] networks = cm.getAllNetworks();

        Log.i(TAG, "Network count: " + networks.length);
        for(int i = 0; i < networks.length; i++) {

            NetworkCapabilities caps = cm.getNetworkCapabilities(networks[i]);

            Log.i(TAG, "Network " + i + ": " + networks[i].toString());
            Log.i(TAG, "VPN transport is: " + caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return true;
            Log.i(TAG, "NOT_VPN capability is: " + caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN));

        }
        return false;

    }

    public void setContext(Context context) {
        this.context = context;
    }
}
