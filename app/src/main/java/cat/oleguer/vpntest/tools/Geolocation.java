package cat.oleguer.vpntest.tools;

import com.google.gson.annotations.SerializedName;


/**
 * This class parses a JSON like the following and serializes an object representing it
 * JSON RESPONSE EXAMPLE
 *             {"ip":"188.241.120.119",
 *             "continent_code":"EU",
 *             "continent_name":"Europe",
 *             "country_code2":"CH",
 *             "country_code3":"CHE",
 *             "country_name":"Switzerland",
 *             "country_capital":"Bern",
 *             "state_prov":"Zurich",
 *             "district":"",
 *             "city":"Zurich",
 *             "zipcode":"8001",
 *             "latitude":"47.37700",
 *             "longitude":"8.53977",
 *             "is_eu":false,
 *             "calling_code":"+41",
 *             "country_tld":".ch",
 *             "languages":"de-CH,fr-CH,it-CH,rm",
 *             "country_flag":"https://ipgeolocation.io/static/flags/ch_64.png",
 *             "geoname_id":"6530239",
 *             "isp":"Hydra Communications Ltd",
 *             "connection_type":"",
 *             "organization":"Hydra Communications Ltd",
 *             "currency":{"code":"CHF","name":"Swiss Franc","symbol":"CHF"},
 *             "time_zone":{"name":"Europe/Zurich","offset":1,"current_time":"2023-05-23 16:37:29.907+0200",
 *             "current_time_unix":1684852649.907,
 *             "is_dst":true,"dst_savings":1}}
 */
public class Geolocation {
    private String ip;
    @SerializedName("continent_code")
    private String continentCode;
    @SerializedName("country_code2")
    private String countryCode2;
    @SerializedName("country_code3")
    private String countryCode3;
    @SerializedName("country_name")
    private String countryName;
    @SerializedName("latitude")
    private String latitude;
    @SerializedName("longitude")
    private String longitude;
    @SerializedName("city")
    private String city;
    @SerializedName("isp")
    private String isp;
    @SerializedName("organization")
    private String organization;
    @SerializedName("current_time")
    private String current_time;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getContinentCode() {
        return continentCode;
    }

    public void setContinentCode(String continentCode) {
        this.continentCode = continentCode;
    }

    public String getCountryCode2() {
        return countryCode2;
    }

    public void setCountryCode2(String countryCode2) {
        this.countryCode2 = countryCode2;
    }

    public String getCountryCode3() {
        return countryCode3;
    }

    public void setCountryCode3(String countryCode3) {
        this.countryCode3 = countryCode3;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getCurrent_time_unix() {
        return current_time;
    }

    public void setCurrent_time(String current_time_unix) {
        this.current_time = current_time_unix;
    }

    @Override
    public String toString() {
        try {
            return countryName + "-" + isp + "-" + organization + "-" + latitude + "." + longitude;
        } catch (Exception e) {
            return "";
        }
        /*return "Geolocation{" +
                "ip='" + ip + '\'' +
                ", continentCode='" + continentCode + '\'' +
                ", countryCode2='" + countryCode2 + '\'' +
                ", countryCode3='" + countryCode3 + '\'' +
                ", countryName='" + countryName + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", city='" + city + '\'' +
                ", isp='" + isp + '\'' +
                ", organization='" + organization + '\'' +
                ", current_time_unix='" + current_time + '\'' +
                '}';*/
    }
}
