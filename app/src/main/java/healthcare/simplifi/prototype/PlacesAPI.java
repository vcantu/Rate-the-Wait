package healthcare.simplifi.prototype;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Viviano on 5/18/2015.
 */
public class PlacesAPI {

    //keys
    public static final String kSEARCH_TYPE = "search_type", kPAGE_TOKEN = "pagetoken",
                kQUERY = "query", kLOCATION = "location", kRADIUS = "radius",
                kRANKBY = "rankby", kLANG = "language", kKEYWORD = "keyword",
                kTYPE = "type", kMINPRICE = "minprice", kMAXPRICE = "maxprice", kOPENNOW = "opennow";
    //search types
    public static final String tNEARBY = "nearbysearch", tTEXT = "textsearch", tRADAR = "radarsearch",
                tDETAILS = "details";

    private static final String APIkey = "AIzaSyA2m6xi36esswADW8ijFj0-GNIjkJtGdy4";

    private Context context;
    public PlacesAPI(Context context) {
        this.context = context;
    }

    /**
        Loads Places from Google API with the given params

        pagetoken : if this parameter is given the rest are irrelevant

        Required Params:
        search_type : nearbysearch , textsearch , radarsearch, details
        query : only if textSearch

         : These are required if nearbySearch and not rankby=distance
        location : must be in "#,#" format
        radius

        Optional params:
        rankby : distance

        language : TODO: this

        keyword
        type
        minprice
        maxprice
        opennow
     */
    public void call(HashMap<String, String> params) {
        //TODO: page token
        //Creates string builder
        StringBuilder URL = new StringBuilder("https://maps.googleapis.com/maps/api/place/");
        //adds search type
        URL.append(params.get(kSEARCH_TYPE)); URL.append("/json?");

        //Iterates through hashmap and adds parameters accordingly
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry e = (HashMap.Entry)it.next();
            if (!e.getKey().equals(kSEARCH_TYPE)) {
                String text = e.getValue().toString();
                //TODO: fix query text here
                text = text.replace(' ', '+');

                URL.append(e.getKey() + "=" + text + "&");
            }
        }
        URL.append("key=");URL.append(APIkey);

        //TODO: if details do different things
        PlacesTask placesTask = new PlacesTask();
        placesTask.execute(URL.toString());
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        }catch(Exception e){
            Log.d("Exception: ", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        System.out.println(strUrl);
        System.out.println(data);
        return data;
    }

    private class PlacesTask extends AsyncTask<String, Integer, String> {
        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... params) {
            try{
                data = downloadUrl(params[0]);
            }catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return data;
        }
        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }

    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>> {
        JSONObject jObject;
        PlaceJSONParser placeJsonParser;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... params) {

            List<HashMap<String, String>> places = null;
            placeJsonParser = new PlaceJSONParser(context);

            try{
                jObject = new JSONObject(params[0]);
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){
            ((MapActivity)context).onLoadPlaces(list);
        }
    }

}
