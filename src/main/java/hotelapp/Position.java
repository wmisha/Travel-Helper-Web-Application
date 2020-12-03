package hotelapp;

import com.google.gson.annotations.SerializedName;

public class Position {
    @SerializedName(value = "lat")
    private String latitude;
    @SerializedName(value = "lng")
    private String longitude;



    public Position(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "latitude: " + latitude + "\n" + "longitude: " + longitude;
    }
}
