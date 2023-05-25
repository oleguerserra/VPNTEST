package cat.oleguer.vpntest.tools;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class VPNUtils {

    private static final String TAG = "VPNUtils";

    //Check if we have network connexion
    public static boolean checkNetwork (Context context){
        boolean connected = true;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connected = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        Log.d(TAG,"VPNUtils-checkNetwork: " + connected);
        return connected;
    }

    public static boolean checkVPN(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();
        Log.i(TAG, "Network count: " + networks.length);
        for(int i = 0; i < networks.length; i++) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(networks[i]);
            Log.d(TAG, "checkVPN-Network " + i + ": " + networks[i].toString());
            Log.d(TAG, "checkVPN-VPN transport is: " + caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return true;
            Log.d(TAG, "checkVPN-NOT_VPN capability is: " + caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN));
        }
        return false;
    }

    // Check the status of the VPN kill switch
    public static boolean isKillSwitchEnabled(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                Network[] networks = connectivityManager.getAllNetworks();
                for (Network network : networks) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                    if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        Log.d(TAG,"isKillSwitchEnabled: " + true);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking kill switch status: " + e.getMessage());
        }
        Log.d(TAG,"isKillSwitchEnabled: " + false);
        return false;
    }

    // Get information about the active VPN connection
    public static String getVpnConnectionInfo(Context context) {
        VpnService vpnService = getVpnService(context);

        if (vpnService!= null) return vpnService.toString();
        else return "Null VPN Service";
    }

    // Helper method to retrieve the VpnService instance
    private static VpnService getVpnService(Context context) {
        VpnService vpnService = null;
        try {
            Class<?> vpnServiceClass = Class.forName("android.net.VpnService");
            Method method = vpnServiceClass.getMethod("prepare", Context.class);
            Object result = method.invoke(null, context);
            if (result instanceof VpnService) {
                vpnService = (VpnService) result;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting VpnService instance: " + e.getMessage());
        }
        return vpnService;
    }

    public static String getConnectionType(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
            return connectivityManager.getActiveNetworkInfo().getTypeName();
        } catch (Exception e){
            Log.e(TAG, "Error getting connection Type: " + e.getMessage());
            return "";
        }
    }

    // Get the list of IPv4 addresses of the terminal
    public static List<String> getIpv4Addresses(Context context) {

        List<String> ips = new ArrayList<String>();
        try {

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        //String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        String ip =  inetAddress.getHostAddress();
                        Log.d(TAG, "getIpv4Addresses IP="+ ip);
                        ips.add(ip);
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        } catch (NullPointerException npe){
            Log.d(TAG, "CAN'T FIND NETWORK INTERFACES");
        }
        return ips;

        /*
        try {
            InetAddress[] inetAddresses = InetAddress.getAllByName("localhost");

            for (InetAddress inetAddress : inetAddresses) {
                if (inetAddress.getHostAddress().contains(".")) {
                    ipv4Addresses.add(inetAddress.getHostAddress());
                }
            }
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error getting IPv4 addresses: " + e.getMessage());
        }
        return ipv4Addresses;

         */
    }

    /* Get the list of IPv6 addresses of the terminal
    public static List<String> getIpv6Addresses() {
        List<String> ipv6Addresses = new ArrayList<>();
        try {
            InetAddress[] inetAddresses = InetAddress.getAllByName("localhost");
            for (InetAddress inetAddress : inetAddresses) {
                if (inetAddress.getHostAddress().contains(":")) {
                    ipv6Addresses.add(inetAddress.getHostAddress());
                }
            }
        } catch (UnknownHostException e) {
            Log.e(TAG, "Error getting IPv6 addresses: " + e.getMessage());
        }
        return ipv6Addresses;
    }*/

    // Get the list of DNS servers where the terminal is resolving
    public static List<String> getDnsServers() {
        List<String> dnsServers = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("getprop | grep dns");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG,"getDnsServers-DNS Server: " + line);
                dnsServers.add(line.trim());
            }
            reader.close();
            process.destroy();
        } catch (IOException e) {
            Log.e(TAG, "Error getting DNS servers: " + e.getMessage());
        }
        return dnsServers;
    }

    // Get the list of static routes
    public static List<String> getStaticRoutes() {
        List<String> staticRoutes = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("ip route");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String lineRoute = "";
            while ((lineRoute = reader.readLine()) != null) {
                staticRoutes.add(lineRoute.trim());
                Log.d(TAG,"getStaticRoutes-route: " + lineRoute.trim());
            }
            reader.close();
            process.destroy();
        } catch (IOException e) {
            Log.e(TAG, "Error getting static routes: " + e.getMessage());
        }
        return staticRoutes;
    }

    // Check the connection to a provided external server

    public static boolean checkServerConnection(String serverAddress, int port) {
        try {
            //Socket socket = new Socket(remoteIp, port);
            InetAddress address = InetAddress.getByName(serverAddress);
            String remoteIp = address.getHostAddress();
            InetSocketAddress sa = new InetSocketAddress(remoteIp,port);
            Socket ss = new Socket();
            ss.connect(sa, 1);
            ss.close();
            //Socket socket = new Socket(serverAddress, port);
            //socket.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking server connection: " + e.getMessage());
        }
        return false;
    }
}
