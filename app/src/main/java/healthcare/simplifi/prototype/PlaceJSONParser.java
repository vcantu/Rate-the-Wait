package healthcare.simplifi.prototype;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Viviano on 5/13/2015.
 */
public class PlaceJSONParser {

    private Context context;
    private String[] unimportantTypes;
    public boolean hasToken = false;
    public String token = "";

    public PlaceJSONParser(Context context) {
        this.context = context;
        unimportantTypes = context.getResources().getStringArray(R.array.unimportant);
    }

    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parse(JSONObject jObject){

        JSONArray jPlaces = null;
        try {
            if (jObject.has("next_page_token")) {
                hasToken = true;
                token = jObject.getString("next_page_token");
            }
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jPlaces){
        int placesCount = jPlaces.length();
        List<HashMap<String, String>> placesList = new ArrayList<HashMap<String,String>>();
        HashMap<String, String> place = null;

        /** Taking each place, parses and adds to list object */
        for(int i=0; i<placesCount;i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject)jPlaces.get(i));
                if (place != null)
                    placesList.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    /** Parsing the Place JSON object */
    private HashMap<String, String> getPlace(JSONObject jPlace){

        HashMap<String, String> place = new HashMap<String, String>();
        String placeName = "-NA-";
        String latitude="";
        String longitude="";

        try {
            // Extracting Place name, if available
            if(!jPlace.isNull("name")){
                placeName = jPlace.getString("name");
            }

            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");

            place.put("place_id", jPlace.getString("place_id"));
            //TODO: radarserach will only have id
            place.put("place_name", placeName);
            place.put("lat", latitude);
            place.put("lng", longitude);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }

    private boolean isUnimportant(JSONArray typesArray) {
        int length = typesArray.length();

        for (int i=0; i<length; i++) {
            try {
                for (String s : unimportantTypes) {
                    if (typesArray.getString(i).equals(s))
                        return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
