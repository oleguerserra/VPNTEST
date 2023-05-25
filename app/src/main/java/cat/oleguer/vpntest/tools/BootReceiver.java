package cat.oleguer.vpntest.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            StartBackgroundService.startBackgroundService(context);
            /*
            Intent startIntent = new Intent(context, BackgroundService.class);
            Log.d(TAG, "Start service" + startIntent.toString());
            startIntent.setAction("START");
            startIntent.putExtra("hostname", "vpn.serra.cat");
            startIntent.putExtra("startPort", 80);
            startIntent.putExtra("endPort", 84);
            context.startService(startIntent);*/
        }
    }
}