package healthcare.simplifi.prototype;

import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import healthcare.simplifi.prototype.uielements.RateButton;
import healthcare.simplifi.prototype.uielements.SlidingUpPanelLayout;
import healthcare.simplifi.prototype.uielements.TypePicker.TypePicker;
import healthcare.simplifi.prototype.uielements.rating.CrowdMeasure;
import healthcare.simplifi.prototype.uielements.rating.LineMeasure;
import healthcare.simplifi.prototype.uielements.rating.ParkingMeasure;


public class MapActivity extends ActionBarActivity implements SlidingUpPanelLayout.PanelSlideListener{

    private final Context context = this;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private PlacesAPI placesAPI;

    private LocationManager locationManager;

    //markers
    private List<MapPlace> places = new ArrayList<>();

    private LatLng currLoc = new LatLng(0, 0);

    //UI
    private RateButton rateButton;
    private SearchView searchView;
    private SlidingUpPanelLayout slideUpLayout;
    private ScrollView mapScrollView;
    private TypePicker typePicker;

    //bottom banner
    private RelativeLayout banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpMapIfNeeded();

        typePicker = (TypePicker)findViewById(R.id.typePicker);

        placesAPI = new PlacesAPI(this);

        mapScrollView = (ScrollView)findViewById(R.id.scrollView);
        slideUpLayout = (SlidingUpPanelLayout)findViewById(R.id.slidingLayout);

        slideUpLayout.setScrollableView(mapScrollView, 125);
        slideUpLayout.setPanelSlideListener(this);

        rateButton = (RateButton)findViewById(R.id.rate_button);
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRate();
                rateButton.invalidate();
            }
        });

        banner = (RelativeLayout)findViewById(R.id.bottom_banner);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlaceInfoActivity.class);
                startActivity(intent);
            }
        });
        final TextView text = (TextView)banner.findViewById(R.id.banner_name);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (typePicker.active)
                    typePicker.deactivate();
                rateButton.setVisibility(View.VISIBLE);
                banner.setVisibility(View.VISIBLE);
                for (MapPlace p : places) {
                    if (p.marker.equals(marker))
                        text.setText(p.marker.getTitle());
                }
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng pos) {
                rateButton.setVisibility(View.INVISIBLE);
                banner.setVisibility(View.INVISIBLE);
                if (typePicker.active)
                    typePicker.deactivate();
//                HashMap<String, String> params = new HashMap<>();
//                params.put(PlacesAPI.kSEARCH_TYPE, PlacesAPI.tNEARBY);
//                params.put(PlacesAPI.kLOCATION, extract(pos));
//                params.put(PlacesAPI.kRADIUS, "500");
//                placesAPI.call(params);
            }
        });

        //Load first places
        HashMap<String, String> params = new HashMap<>();
        params.put(PlacesAPI.kSEARCH_TYPE, PlacesAPI.tNEARBY);
        params.put(PlacesAPI.kLOCATION, extract(currLoc));
        params.put(PlacesAPI.kRADIUS, "1000");

        placesAPI.call(params);

        //PlacePost post = new PlacePost();
        //post.execute("test");
    }

    public void showToast(String type) {
        Toast.makeText(this, "Searching for " + type, Toast.LENGTH_SHORT).show();
    }

    private void startRate() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.rate_layout);
        dialog.setTitle("Rate the Wait");

        final CrowdMeasure crowdMeasure = (CrowdMeasure) dialog.findViewById(R.id.crowdMeasure);
        final SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.seekBar);
        seekBar.setMax(CrowdMeasure.MAX);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                crowdMeasure.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final LineMeasure lineMeasure = (LineMeasure) dialog.findViewById(R.id.lineMeasure);
        final SeekBar lineSeekBar = (SeekBar) dialog.findViewById(R.id.lineSeekBar);
        lineSeekBar.setMax(100 - 1);//set amount desired -1
        lineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lineMeasure.setProgress(progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final ParkingMeasure parkingMeasure = (ParkingMeasure) dialog.findViewById(R.id.parkMeasure);
        final SeekBar parkSeekBar = (SeekBar) dialog.findViewById(R.id.parkSeekBar);
        parkSeekBar.setMax(ParkingMeasure.MAX);
        parkSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                parkingMeasure.setProgress(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        final Button submitBtn = (Button)dialog.findViewById(R.id.button);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void startFilter() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.filter_layout);
        dialog.setTitle("Customize Search");

        final SeekBar distanceBar = (SeekBar)dialog.findViewById(R.id.seekBarDistance);
        final TextView distanceText = (TextView)dialog.findViewById(R.id.textDistance);
        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceText.setText("Distance: " + (progress * 100) + "m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final CheckBox openNowCheck = (CheckBox)dialog.findViewById(R.id.openCheckBox);
        final RatingBar dollarBar = (RatingBar)dialog.findViewById(R.id.dollarBar);

        final SeekBar timeBar = (SeekBar)dialog.findViewById(R.id.seekBarWaitTime);
        final TextView timeText = (TextView)dialog.findViewById(R.id.textWaitTime);
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timeText.setText("Wait Time: " + formatTime(progress * 15 + 15) + " or less");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        dialog.show();
    }
    private String formatTime(int minutes) {
        String str = "";
        int hrs = minutes / 60;
        int mins = minutes % 60;

        if (hrs > 0)
            str += hrs + "hr" + (hrs!=1 ? "s":"") + (mins>0 ? " ":"");
        if (mins > 0)
            str += mins + "min" + (mins!=1 ? "s":"");

        return str;
    }

    public void loadPlacesFromType(String type) {
        HashMap<String, String> params = new HashMap<>();
        params.put(PlacesAPI.kSEARCH_TYPE, PlacesAPI.tNEARBY);
        params.put(PlacesAPI.kLOCATION, extract(currLoc));
        params.put(PlacesAPI.kRANKBY, "distance");
        params.put(PlacesAPI.kTYPE, type);
        placesAPI.call(params);
        clearMarkers();
    }
    public void loadPlacesFromAddon(String addon) {
        String[] addons = addon.split("&");
        HashMap<String, String> params = new HashMap<>();
        params.put(PlacesAPI.kSEARCH_TYPE, PlacesAPI.tNEARBY);
        params.put(PlacesAPI.kLOCATION, extract(currLoc));
        params.put(PlacesAPI.kRANKBY, "distance");
        for (int i=0; i<addons.length; i++) {
            String[] temp = addons[i].split("=");
            params.put(temp[0], temp[1]);
        }
        placesAPI.call(params);
        clearMarkers();
    }

    //Events
    public void onLoadPlaces(List<HashMap<String,String>> list) {
        if (list == null)
            return;
        for(int i=0;i<list.size();i++){
            HashMap<String, String> hmPlace = list.get(i);
            double lat = Double.parseDouble(hmPlace.get("lat"));
            double lng = Double.parseDouble(hmPlace.get("lng"));

            //avoids duplicates
            if (checkMarker(lat, lng)) {
                MarkerOptions markerOptions = new MarkerOptions();

                String name = hmPlace.get("place_name");
                LatLng latLng = new LatLng(lat, lng);

                markerOptions.position(latLng);
                markerOptions.title(name);

                addMarker(markerOptions, hmPlace.get("place_id"));
            }
        }

        //TODO: make it for search only
        try {
            //Sets all items in view
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (MapPlace p : places) {
                builder.include(p.marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cam = CameraUpdateFactory.newLatLngBounds(bounds, 0);
            mMap.animateCamera(cam);
        }
        catch (Exception e) {}
    }

    private boolean checkMarker(double lat, double lng) {
        for (MapPlace p : places) {
            if (p.marker.getPosition().latitude == lat
                    && p.marker.getPosition().longitude == lng)
                return false;
        }
        return true;
    }
    private void addMarker(MarkerOptions mo, String id) {
        places.add(new MapPlace(mMap.addMarker(mo), id));
    }
    private void clearMarkers() {
        places = new ArrayList<>();
        mMap.clear();
    }

    //menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);

        searchView = (SearchView)menu.findItem(R.id.search_button).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(getApplicationContext(), MapActivity.class)));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_filter:
                startFilter();
                PlacePost pp = new PlacePost();
                pp.execute(new String[]{""});
                return true;
            case R.id.action_send_notification:
                int notificationId = 001;
                // Build intent for notification content
                Intent viewIntent = new Intent(this, MapActivity.class);
                //viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
                PendingIntent viewPendingIntent =
                        PendingIntent.getActivity(this, 0, viewIntent, 0);

                //TODO: USE THIS TO CUSTOMIZE MAIN NOTIFICATION LOOKS
                NotificationCompat.WearableExtender wearableExtender =
                        new NotificationCompat.WearableExtender()
                                .setHintHideIcon(false);


                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_rtw_notification)
                                .setContentTitle("Rate The Wait")
                                .setContentText("THIS IS CONTENT TEXT!")
                                .setContentIntent(viewPendingIntent)
                                .extend(wearableExtender)
                                .addAction(R.drawable.ic_action_rate, "hi",
                                        viewPendingIntent);

                // Get an instance of the NotificationManager service
                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(this);

                // Build the notification and issues it with notification manager.
                notificationManager.notify(notificationId, notificationBuilder.build());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //used for search
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            clearMarkers();
            searchView.setIconified(true);
            searchView.setIconified(true);
            onBackPressed();

            String query = intent.getStringExtra(SearchManager.QUERY);
            //search here
            HashMap<String, String> params = new HashMap<>();
            params.put(PlacesAPI.kSEARCH_TYPE, PlacesAPI.tTEXT);
            params.put(PlacesAPI.kLOCATION, extract(currLoc));
            params.put(PlacesAPI.kRADIUS, "5000");//TODO: range searches
            params.put(PlacesAPI.kQUERY, query);

            placesAPI.call(params);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                try {
                    setUpMap();
                } catch (Exception e) {}
            }
        }
    }
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // Enable MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);
        double latitude = myLocation.getLatitude();
        double longitude = myLocation.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        currLoc = latLng;

        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setContentDescription("Find Places by wait-time");
        mMap.setTrafficEnabled(true);
        mMap.setPadding(0, 0, 0, 400);

        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.5f));
    }

    /* extracts the coordinates from the LatLng.toString() method */
    private String extract(LatLng ll) {
        String s = ll.toString();
        return s.substring(10, s.length()-2);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelCollapsed(View panel) {
//        transparentView.setVisibility(View.GONE);
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14f), 1000, null);
        }
        //ScrollView.setScrollingEnabled(false);
    }

    @Override
    public void onPanelExpanded(View panel) {
//        transparentView.setVisibility(View.INVISIBLE);
//        if (mMap != null && mLocation != null) {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 11f), 1000, null);
//        }
        //mListView.setScrollingEnabled(true);
    }

    @Override
    public void onPanelAnchored(View panel) {

    }

    //TODO: this will be a class
    //SEND POST TO SERVER
    private class PlacePost extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            // do above Server call here
            // Creating HTTP client
            HttpClient httpClient = new DefaultHttpClient();
            // Creating HTTP Post
            HttpPost httpPost = new HttpPost(
                    //"http://places-review.herokuapp.com/reviews.JSON");
                    "http://107.21.119.157/MedApp/MedService/Service.svc/XmlService/InsertUserReg");

            // Building post parameters
            // key and value pair
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("UserID", "testing@sb3inc.com"));
            nameValuePair.add(new BasicNameValuePair("CompanyID", "2"));
            nameValuePair.add(new BasicNameValuePair("Password", "1234"));
            nameValuePair.add(new BasicNameValuePair("CellPhone", "123-456-7890"));
            nameValuePair.add(new BasicNameValuePair("Zipcode", "06745"));
            nameValuePair.add(new BasicNameValuePair("UserPhoto", ""));
            nameValuePair.add(new BasicNameValuePair("UserType", "2"));
            nameValuePair.add(new BasicNameValuePair("UserName", "First Middle Last"));

            // Url Encoding the POST parameters
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                // writing error to Log
                e.printStackTrace();
            }

            // Making HTTP Request
            try {
                HttpResponse response = httpClient.execute(httpPost);

                // writing response to log
                Log.d("Http Response:", response.toString());

                Log.d("entity", EntityUtils.toString(response.getEntity()));
            } catch (ClientProtocolException e) {
                // writing exception to log
                e.printStackTrace();
            } catch (IOException e) {
                // writing exception to log
                e.printStackTrace();

            }
            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

}
