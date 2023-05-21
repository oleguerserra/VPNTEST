package cat.oleguer.vpntest.tools;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.R;

import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";

    private static final int INTERVAL = 5000; // Interval for repeating the process in milliseconds
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "testvpn_channel_id";

    private String hostname = "vpn.serra.cat";
    private int startPort = 80;
    private int endPort = 81;
    private boolean isRunning;
    private Handler handler;
    private OkHttpClient httpClient;
    private int openPortsCount = 0;
    private int closedPortsCount = 0;
    private int totalConnectionsCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        // Start the service as a foreground service
        startForeground(NOTIFICATION_ID, createNotification());
        handler = new Handler();
        httpClient = new OkHttpClient();
        startScanning();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("START")) {
                    if (!isRunning) {
                        hostname = intent.getStringExtra("hostname");
                        startPort = intent.getIntExtra("startPort", 0);
                        endPort = intent.getIntExtra("endPort", 0);
                        isRunning = true;
                        startScanning();
                        Log.d(TAG,"Start scanning hostname: " + hostname + " from port: " + startPort + " to: " + endPort);
                    }
                } else if (action.equals("STOP")) {
                    stopScanning();
                    stopSelf();
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void startScanning() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    resolveHostnameAndScanPorts();
                    handler.postDelayed(this, INTERVAL);
                }
            }
        }, INTERVAL);
    }

    private void resolveHostnameAndScanPorts() {
        makeIpifyRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to retrieve public IP address: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myIp = response.body().string();
                    resolveAndScanPorts(myIp);
                } else {
                    Log.e(TAG, "Failed to retrieve public IP address: " + response.message());
                }
            }
        });
    }

    private void makeIpifyRequest(Callback callback) {
        Request request = new Request.Builder()
                .url("https://api.ipify.org")
                .build();

        httpClient.newCall(request).enqueue(callback);
    }

    private void resolveAndScanPorts(String myIp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Resolve the IP address from the hostname
                    InetAddress address = InetAddress.getByName(hostname);
                    String remoteIp = address.getHostAddress();
                    scanPorts(myIp, remoteIp);
                } catch (UnknownHostException e) {
                    Log.e(TAG, "Failed to resolve hostname: " + e.getMessage());
                }
            }
        }).start();
    }

    private void scanPorts(String myIp, String remoteIp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        for (int port = startPort; port <= endPort; port++) {
            try {
                // Open a TCP connection to the remote server
                Socket socket = new Socket(remoteIp, port);

                OutputStream outputStream = socket.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                BufferedWriter writer = new BufferedWriter(outputStreamWriter);

                // Generate a random UUID
                UUID uuid = UUID.randomUUID();

                // Convert the UUID to a hexadecimal string
                String hexString = uuidToHexString(uuid);

                String message = timestamp + "," + myIp + "," + hexString;

                writer.write(message);
                writer.newLine();
                writer.flush();

                writer.close();
                outputStreamWriter.close();
                outputStream.close();



                // Store the response in a CSV file
                storeResponse(timestamp, myIp, remoteIp, port, "Open", hexString);
                openPortsCount++;
                // Close the socket
                socket.close();
            } catch (IOException e) {
                // Port is closed or an error occurred
                closedPortsCount++;
                storeResponse(timestamp, myIp, remoteIp, port, "Closed", "");
            }
            totalConnectionsCount++;
        }
    }

    private void storeResponse(String timestamp, String myIp, String remoteIp, int port, String status, String uuid) {
        String filepath = "";
        try {
            // Append the response to a CSV file
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "response.csv");

            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            writer.println(timestamp + "," + myIp + "," + remoteIp + "," + port + "," + status + "," + uuid);
            Log.d(TAG,file.getAbsolutePath()+ "," +timestamp + "," + myIp + "," + remoteIp + "," + port + "," + status + "," + uuid);
            writer.close();
            filepath = file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Failed to store response: " + e.getMessage());
        }

        boolean checkNetwork = VPNUtils.checkNetwork(getApplicationContext());
        String vpnInfo = VPNUtils.getVpnConnectionInfo(getApplicationContext());
        boolean killSwitch = VPNUtils.isKillSwitchEnabled(getApplicationContext());

        List<String> ipList = VPNUtils.getIpv4Addresses(getApplicationContext());
        String ipListString = "";
        Iterator<String> ipit = ipList.listIterator();
        while (ipit.hasNext()) ipListString = ipListString +ipit.next() + ", ";

        List<String> dnsList = VPNUtils.getDnsServers();
        String dnsListString = "";
        Iterator<String> dnsit = dnsList.iterator();
        while (dnsit.hasNext()) dnsListString = dnsListString + dnsit.next() + ", ";

        List<String> routesList = VPNUtils.getStaticRoutes();

        String routesListString = "";
        Iterator<String> routesit = routesList.iterator();
        while (routesit.hasNext()) routesListString = routesListString + routesit.next() + ", ";

        boolean checkServer = VPNUtils.checkServerConnection("www.omnium.cat",80);

        NetworkTools nt = new NetworkTools(getApplicationContext());

        String connectionType = VPNUtils.getConnectionType(getApplicationContext());


        // Send a broadcast to the widget with the updated counter values
        Intent updateIntent = new Intent("UPDATE_COUNTERS");
        updateIntent.putExtra("openPortsCount", openPortsCount);
        updateIntent.putExtra("closedPortsCount", closedPortsCount);
        updateIntent.putExtra("totalConnectionsCount", totalConnectionsCount);
        updateIntent.putExtra("vpnStatus","VPN Status: " + nt.checkVPN() + "\nNetwork Status: " + nt.checkNetwork());
        updateIntent.putExtra("myIp",myIp
                                                + "\nCheck Network: " + checkNetwork
                                                + "\nConnection Type: " + connectionType
                                                + "\nVPN Info: " + checkNetwork
                                                + "\nKill Switch: " + killSwitch
                                                + "\nIP List: " + ipListString
                                                + "\nDNS List: " + dnsListString
                                                + "\nRoutes: " + routesListString
                                                + "\nServer Running: " + checkServer);
        sendBroadcast(updateIntent);
    }

    private void stopScanning() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private Notification createNotification() {
        // Create the notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Foreground Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Build and return the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Running in the background")
                .setSmallIcon(R.drawable.ic_notification_overlay);


        return builder.build();
    }




        private static String uuidToHexString(UUID uuid) {
            long mostSignificantBits = uuid.getMostSignificantBits();
            long leastSignificantBits = uuid.getLeastSignificantBits();

            // Convert the most significant bits and least significant bits to hexadecimal strings
            String mostSignificantHex = Long.toHexString(mostSignificantBits);
            String leastSignificantHex = Long.toHexString(leastSignificantBits);

            // Concatenate the hexadecimal strings and pad with leading zeros if necessary
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(mostSignificantHex);
            stringBuilder.append(leastSignificantHex);

            // Pad with leading zeros to ensure the final string has a length of 32
            while (stringBuilder.length() < 32) {
                stringBuilder.insert(0, "0");
            }

            return stringBuilder.toString();
        }



}
