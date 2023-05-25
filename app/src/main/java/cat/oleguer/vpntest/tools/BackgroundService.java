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
import android.provider.Settings;
import android.util.Log;
import android.R;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
    private static int INTERVAL = 5000; // Interval for repeating the process in milliseconds
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "testvpn_channel_id";
    private String hostname = "";
    private int startPort = 80;
    private int endPort = 81;
    private boolean isRunning;
    private Handler handler;
    private OkHttpClient httpClient;
    private int openPortsCount = 0;
    private int closedPortsCount = 0;
    private int totalConnectionsCount = 0;
    private HashMap<String,Geolocation> localStoredIPs = new HashMap<>();
    private HashMap<String,Geolocation> remoteStoredIPs = new HashMap<>();
    private Intent myIntent;

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
        myIntent = intent;
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
        //Calling the service https://www.ipify.org/ whating for call back.
        makeIpifyRequest(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to retrieve public IP address: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myIp = response.body().string();
                    Log.d(TAG,"Resolved my IP:" + myIp);
                    if (!localStoredIPs.containsKey(myIp)) {
                        // IP has changed, store the new IP and timestamp
                        Geolocation geoLocalization = IPGeolocation.getLocalization(myIp,myIntent);
                        Log.d(TAG,"Geoloalized IP:" + geoLocalization.toString());
                        localStoredIPs.put(myIp,geoLocalization);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String timestamp = sdf.format(new Date());
                        logIP(timestamp, myIp, "local", geoLocalization.toString());
                        Log.d(TAG,"Found New IP: " + myIp);
                    }
                    resolveAndScanPorts(myIp);
                } else {
                    Log.e(TAG, "Failed to retrieve public IP address: " + response.message());
                }
            }
        });
    }

    /**
     * Let's find our IP using the free online service  https://www.ipify.org/
     *
     * @param callback
     */
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

    /**
     * Scanm all ports of a remote server with the ip: remoteIP
     * It will scan all ports defined between START_PORT and END_PORT in the configuration file: build.gradle
     *
     * @param myIp IP from the https://www.ipify.org/ service
     * @param remoteIp Repote IP defined by the HOSTNAME in the configuration file: build.gradle
     */
    private void scanPorts(String myIp, String remoteIp) {
        boolean match = false;
        String responseIp = "";
        for (int port = startPort; port <= endPort; port++) {
            try {
                // Open a TCP connection to the remote server
                Socket socket = new Socket(remoteIp, port);

                OutputStream outputStream = socket.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                BufferedWriter writer = new BufferedWriter(outputStreamWriter);

                // Generate a random UUID
                String hexString = uuidToHexString(UUID.randomUUID());

                // Generate a mobile unique identifyer
                String muid = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID)
                                + "-" + Build.BRAND + "-" + Build.MODEL;

                /**
                 *  Sending a message through the socket
                 *
                 *  [Timestamp, muid, myIP, uuid, geoloc]
                 *
                 */
                String message = getTimeStamp() + "," + muid + "," + myIp + "," + hexString + "," + localStoredIPs.get((myIp).toString());

                writer.write(message);
                writer.newLine();
                writer.flush();

                // Read the response from the stream
                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                String response = reader.readLine();

                /**
                 * Parse the response and compare IP and UUID values
                 * The response must have the following format:
                 *
                 * [remote IP Address, UUID]
                 *
                 */
                if (response != null) {
                    String[] values = response.split(",");
                    if (values.length >= 2) {
                        // Getting remote IP from response
                        responseIp = values[0].trim();

                        // Storing remote IP if it is new
                        if (!remoteStoredIPs.containsKey(responseIp)) {
                            // Remote Storede IP has changed, store the new IP and timestamp
                            Geolocation geoLocalization = IPGeolocation.getLocalization(responseIp,myIntent);
                            Log.d(TAG,"Geoloalized Remote Stored IP:" + geoLocalization.toString());
                            remoteStoredIPs.put(responseIp,geoLocalization);
                            logIP(getTimeStamp(), responseIp, "remote", geoLocalization.toString());
                            Log.d(TAG,"Found New Remote Stored IP: " + responseIp);
                        }

                        String responseUuid = values[1].trim();

                        // We raise an alert for the log if remote ip is diferent from my ip and if the received uuid
                        // is different from the uuid we sent before.

                        if (responseIp.equals(myIp) && responseUuid.equals(hexString)) {
                            // IP and UUID match
                            Log.d(TAG,"IP and UUID match the response.");
                            match=true;
                        } else {
                            // IP or UUID do not match
                            Log.d(TAG,"IP or UUID do not match the response."+ responseIp + "- " + myIp + "-" + responseUuid + "-" + hexString);
                        }
                    } else {
                        // Invalid response format
                        Log.d(TAG,"Invalid response format.");
                    }
                } else {
                    // No response received
                    Log.d(TAG,"No response received.");
                }

                // Close the streams and socket
                reader.close();
                inputStreamReader.close();
                inputStream.close();

                writer.close();
                outputStreamWriter.close();
                outputStream.close();
                socket.close();


                // Store the response in a CSV file
                storeResponse(getTimeStamp(), myIp, remoteIp, port, "Open", responseIp, match, hexString);
                openPortsCount++;
                // Close the socket
                socket.close();
            } catch (IOException e) {
                // Port is closed or an error occurred
                closedPortsCount++;
                storeResponse(getTimeStamp(), myIp, remoteIp, port, "Closed", responseIp, match, "");
            }
            totalConnectionsCount++;
        }
    }

    /**
     * Writes a local file response.csv with all params needed to debug
     * @param timestamp
     * @param myIp  IP from the https://www.ipify.org/ service
     * @param remoteIp  IP of the server we are connecting
     * @param port  Server's port we are accessing
     * @param status    Port open / closed
     * @param responseIp    The IP the server says it has
     * @param match true if Local IP, Response IP, Sent and recived UUID match
     * @param uuid
     */
    private void storeResponse(String timestamp, String myIp, String remoteIp, int port, String status, String responseIp, boolean match, String uuid) {
        String filepath = "";
        try {
            // Append the response to a CSV file

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "response.csv");

            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            writer.println(timestamp + "," + myIp + "," + remoteIp + "," + port + "," + status + "," + responseIp + "," + match + "," + uuid + "," + localStoredIPs.get(myIp).toString());
            Log.d(TAG,file.getAbsolutePath()+ "," +timestamp + "," + myIp + "," + remoteIp + "," + port + "," + status + "," + responseIp + "," + match + "," + uuid + "," + localStoredIPs.get(myIp).toString());
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
                                                //+ "\nDNS List: " + dnsListString
                                                //+ "\nRoutes: " + routesListString
                                                //+ "\nServer Running: " + checkServer);
                                                + "\nLocation: " + localStoredIPs.get(myIp).toString());
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


    /**
     * Create the notification channel (required for Android Oreo and above)
     * @return
     */
    private Notification createNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "TESTVPN Foreground Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Build and return the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TESTVPN Foreground Service")
                .setContentText("Running in the background")
                .setSmallIcon(R.drawable.ic_notification_overlay);


        return builder.build();
    }


    /**
     * Returns an hexadecimal representation of a random UUID
     * https://www.cockroachlabs.com/blog/what-is-a-uuid/
     *
     * @param uuid
     * @return
     */
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

    /**
     * Writes a CSV File logging all found IP
     *
     * [timestamp, ip, local / remote, geolocation]
     *
     * @param timestamp
     * @param ip
     * @param localOrRemote
     * @param geoLoc
     */
    public void logIP(String timestamp, String ip, String localOrRemote, String geoLoc) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "ips.csv");
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

            // Append a new line to the CSV file with timestamp and IP
            writer.println(timestamp + "," + ip + "," + localOrRemote + "," + geoLoc);
            writer.flush();
            writer.close();

            Log.d(TAG,"IP logged successfully. Timestamp: " + timestamp + ", IP: " + ip);
        } catch (IOException e) {
            System.err.println("Error logging IP: " + e.getMessage());
        }
    }

    /**
     *
     * @return Timestamp in "yyyy-MM-dd HH:mm:ss" format
     */
    private static String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

}
