package cat.oleguer.vpntest.tools;

import android.util.Log;
import android.content.Intent;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IPGeolocation {

    private static final String TAG = "IPGeolocation";

    /**
     * Returns a Geolocation of an IP using the service https://ipgeolocation.io
     *
     * You need to obtain an API Key and store it in the build.gradle file
     *
     * @param ipAddress  IP we want to locate
     * @param intent Intent of the APP to retrieve the API key stored
     * @return a Geolocation of the IP
     */
    public static Geolocation getLocalization(String ipAddress, Intent intent) {

        String ipgeolocation_api_key = intent.getStringExtra("ipgeolocation_api_key");
        Log.d(TAG, "ipgeolocation_api_key= " + ipgeolocation_api_key);
        try {
            // Create the API URL
            String apiUrl = "https://api.ipgeolocation.io/ipgeo?apiKey=" + ipgeolocation_api_key + "&ip=" + ipAddress;

            // Send HTTP GET request
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse the JSON response
            Gson gson = new Gson();
            Geolocation geolocation = gson.fromJson(response.toString(), Geolocation.class);

            String jsonResponse = response.toString();
            Log.d(TAG, jsonResponse);
            return geolocation;

        } catch (Exception e) {
            Log.d(TAG,e.getMessage());
            return null;
        }
    }
}
