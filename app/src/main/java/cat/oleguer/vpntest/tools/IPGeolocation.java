package cat.oleguer.vpntest.tools;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IPGeolocation {

    private static final String TAG = "IPGeolocation";

    public static Geolocation getLocalization(String ipAddress) {

        try {
            // Create the API URL
            String apiUrl = "https://api.ipgeolocation.io/ipgeo?apiKey=110df17c1d27413cbd494088bb39a539&ip=" + ipAddress;
            // Replace YOUR_API_KEY with your actual API key from IPGeolocation.io




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

            Gson gson = new Gson();

            Geolocation geolocation = gson.fromJson(response.toString(), Geolocation.class);


            // Parse the JSON response
            // Note: You'll need to use a JSON library such as Gson or Jackson to parse the JSON response


            String jsonResponse = response.toString();
            Log.d(TAG, jsonResponse);
            return geolocation;
            // Handle the geolocation data as per your requirements
            // For example, you can extract and display the country, city, latitude, longitude, etc. from the response

        } catch (Exception e) {
            Log.d(TAG,e.getMessage());
            return null;
        }
    }
}
