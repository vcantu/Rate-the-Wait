package healthcare.simplifi.prototype;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Viviano on 5/26/2015.
 */
public class MapPlace {

    //main
    public String place_id;
    public Marker marker; // marker holds name and position

    public String[] types;

    //Details
    public String address, phone;

    public MapPlace(Marker marker, String place_id) {
        this.marker = marker;
        this.place_id = place_id;
    }

}
