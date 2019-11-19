package vowel.apk.locationActivity;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;
import vowel.apk.R;
import vowel.apk.databaseHelpers.DatabaseHelper;
import vowel.apk.databaseHelpers.DatabaseLocation;

import static android.content.ContentValues.TAG;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static java.nio.charset.StandardCharsets.UTF_8;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener , MapboxMap.OnMapClickListener{

    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
    ArrayList<String> rawLoc;
    ArrayList<String> rawLongLat;
    ArrayList<String> ecList;
    private static final String LAYER_ID = "LAYER_ID";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
    private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";

    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private GeoJsonSource source;
    private FeatureCollection featureCollection;

    DatabaseHelper databaseHelper;


    private LocationActivityLocationCallback callback = new LocationActivityLocationCallback(this);
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    OfflineManager offlineManager;
    private static final String JSON_FIELD_REGION_NAME ="uz_map";
    private static final Charset JSON_CHARSET = UTF_8;
    OfflineTilePyramidRegionDefinition definition;
    String userEc;
    DatabaseLocation databaseLocation;
    DatagramPacket dp;
    DatagramSocket datagramSocket;


    List<Feature> symbolLayerIconFeatureList;

    String ecFromNotif;

//inflate the view
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoidGF2amluamlrYSIsImEiOiJjangyeWZhbGowNjlvNDRxanAzaWN1ajA3In0.MMn8ev6y77myx5f6lFleCw");
        setContentView(R.layout.content_main2);
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //initialize
        rawLoc = new ArrayList<>();
        rawLongLat = new ArrayList<>();
        ecList = new ArrayList<>();
        databaseLocation = new DatabaseLocation(this);
        databaseHelper = new DatabaseHelper(this);


        rawLoc = databaseLocation.getAllLocations();
        //set a list for user locations from database
        for(int i=0;i<rawLoc.size(); i++){
            Collections.addAll(rawLongLat, rawLoc.get(i).split("_~")[1]);
            Collections.addAll(ecList, rawLoc.get(i).split("_~")[0]);
        }


        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setReuseAddress(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        //get the ec number form shared preference
        ecNumber = PreferenceManager.getDefaultSharedPreferences(this);
        userEc = getPref();
//get intent data from a notification
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                ecFromNotif = extras.getString("EC");


            }


    }

    // show the red marker on the map
public void showDistrest(double longitude, double latitude){
    IconFactory iconFactory = IconFactory.getInstance(LocationActivity.this);
    Icon icon = iconFactory.fromResource(R.drawable.red_marker);
    mapboxMap.addMarker(new MarkerOptions()
            .position(new LatLng(latitude, longitude))
            .icon(icon)
            .title("UNDER ATTACK"));
}

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        symbolLayerIconFeatureList = new ArrayList<>();
        //loop through the list from the database and put markers on the map
        for(int i = 0; i< rawLongLat.size(); i++){
            symbolLayerIconFeatureList.add(Feature.fromGeometry(
                    Point.fromLngLat(Double.parseDouble(rawLongLat.get(i).split("<->")[0]), Double.parseDouble(rawLongLat.get(i).split("<->")[1]))
            ));
            symbolLayerIconFeatureList.get(i).addStringProperty("NAME", databaseHelper.getUsernameDetail(ecList.get(i)));


        }

        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT);


        mapboxMap.setStyle(new Style.Builder().fromUri(Style.OUTDOORS)
                .withSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList))).withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconOffset(new Float[] {0f, -9f}))
                ), new Style.OnStyleLoaded(){
            @Override
            public void onStyleLoaded(@NonNull Style style){
                enableLocationComponent(style);
                new LoadGeoJsonDataTask(LocationActivity.this).execute();
                mapboxMap.addOnMapClickListener(LocationActivity.this);


                if(ecFromNotif!=null){
                    String loc = databaseLocation.getLocation(ecFromNotif);
                     //show the red marker using intent data to get location of victim
                    showDistrest(Double.parseDouble(loc.split(" ")[0]) , Double.parseDouble(loc.split(" ")[1]));
                }


                offlineManager = OfflineManager.getInstance(LocationActivity.this);

                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                        .include(new LatLng(-17.791003, 31.062562)) // Northeast
                        .include(new LatLng(-17.778848, 31.042606)) // Southwest
                        .build();

                definition = new OfflineTilePyramidRegionDefinition(
                        style.getUri(),
                        latLngBounds,
                        10,
                        20,
                        LocationActivity.this.getResources().getDisplayMetrics().density);

                byte[] metadata;
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(JSON_FIELD_REGION_NAME, "University of Zimbabwe");
                    String json = jsonObject.toString();
                    metadata = json.getBytes(JSON_CHARSET);
                } catch (Exception exception) {
                    Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
                    metadata = null;
                }

                if(metadata!=null) {
                    offlineManager.createOfflineRegion(definition, metadata,
                            new OfflineManager.CreateOfflineRegionCallback() {
                                @Override
                                  public void onCreate(OfflineRegion offlineRegion) {
                                    offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);



                                    // Monitor the download progress using setObserver
                                    offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                                        @Override
                                        public void onStatusChanged(OfflineRegionStatus status) {

                                            // Calculate the download percentage
                                            double percentage = status.getRequiredResourceCount() >= 0
                                                    ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                                                    0.0;

                                            if (status.isComplete()) {
                                                // Download complete


                                                Log.d(TAG, "Region downloaded successfully.");
                                            } else if (status.isRequiredResourceCountPrecise()) {
                                                Log.d(TAG, String.valueOf(percentage));
                                            }
                                        }

                                        @Override
                                        public void onError(OfflineRegionError error) {
                                            Log.d(TAG, String.valueOf(error));
                                        }

                                        @Override
                                        public void mapboxTileCountLimitExceeded(long limit) {
// Notify if offline region exceeds maximum tile count
                                            Log.d(TAG, String.valueOf(limit));
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                   Log.d(TAG, error);
                                }

                            });
                }


            }
        });

    }

//Gettting permission to get Location
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // init location engine
            initLocationEngine();

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }

        mapView.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(this::enableLocationComponent);
        } else {
            finish();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    SharedPreferences ecNumber;
    String getPref() {
        return ecNumber.getString("ecNumber", null);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

//The LocationActivityLocationCallback interface's method which fires when the device's location has changed.
    private class LocationActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<LocationActivity> activityWeakReference;

        LocationActivityLocationCallback(LocationActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            LocationActivity activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();

                //broadcast my location to other users
                try {
                    sendDatagramMessage("coL" + userEc + result.getLastLocation().getLongitude() + "~~__" + result.getLastLocation().getLatitude(),
                            InetAddress.getByName("255.255.255.255"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //update my location in database
                if (!databaseLocation.isInDatabase(userEc))
                    databaseLocation.insertLocation(userEc, result.getLastLocation().getLongitude(), result.getLastLocation().getLatitude());
                else databaseLocation.updateLocation(userEc, result.getLastLocation().getLongitude(), result.getLastLocation().getLatitude());
                if (location == null) {
                    return;
                }

                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Timber.tag("LocationChangeActivity").d(exception.getLocalizedMessage());
            LocationActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //send a datagram message for location updates
    public void     sendDatagramMessage(final String message, final InetAddress group) {
        Thread sendThread = new Thread(() -> {
            byte[] messageByte = message.getBytes(StandardCharsets.UTF_8);
            dp = new DatagramPacket(messageByte, messageByte.length, group, 7544);
            try {
                datagramSocket.send(dp);
                //Log.i("Send lock---->", "Sent message( " + message + " ) to " + group);
                datagramSocket.disconnect();
                //datagramSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sendThread.start();
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
    }


    //set up location data
    public void setUpData(final FeatureCollection collection) {
        featureCollection = collection;
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
                setupSource(style);
                setUpImage(style);
                setUpMarkerLayer(style);
                setUpInfoWindowLayer(style);
            });
        }
    }

    //inflate the view which shows the names of the users on the map
    private void setUpInfoWindowLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        /* show image with id title based on the value of the name feature property */
                        iconImage("{NAME}"),

                        /* set anchor of icon to bottom-left */
                        iconAnchor(ICON_ANCHOR_BOTTOM),

                        /* all info window and marker image to appear at the same time*/
                        iconAllowOverlap(true),

                        /* offset the info window to be above the marker */
                        iconOffset(new Float[] {-2f, -28f})
                ));
/* add a filter to show only when selected feature property is true */
          //      .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));
    }

    //set up the marker layer
    private void setUpMarkerLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        iconImage(MARKER_IMAGE_ID),
                        iconAllowOverlap(true),
                        iconOffset(new Float[] {0f, -8f})
                ));
    }
    //inflate the marker image
    private void setUpImage(@NonNull Style loadedStyle) {
        loadedStyle.addImage(MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                this.getResources(), R.drawable.green_marker));
    }

    // icon click listerner
    private boolean handleClickIcon(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty("NAME");
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty("NAME").equals(name)) {
                        if (featureSelectStatus(i)) {
                            setFeatureSelectState(featureList.get(i), false);
                        } else {
                            setSelected(i);
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }
    private void setSelected(int index) {
        if (featureCollection.features() != null) {
            Feature feature = featureCollection.features().get(index);
            setFeatureSelectState(feature, true);
            refreshSource();
        }
    }

    private void setFeatureSelectState(Feature feature, boolean selectedState) {
        if (feature.properties() != null) {
            feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
            refreshSource();
        }
    }

    private boolean featureSelectStatus(int index) {
        if (featureCollection == null) {
            return false;
        }
        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
    }


//Async task to load the JSON file containing the
    private  class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {

        private final WeakReference<LocationActivity> activityRef;

        LoadGeoJsonDataTask(LocationActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected FeatureCollection doInBackground(Void... params) {
            LocationActivity activity = activityRef.get();

            if (activity == null) {
                return null;
            }

            //String geoJson = loadGeoJsonFromAsset(activity, "us_west_coast.geojson");
            return FeatureCollection.fromFeatures( symbolLayerIconFeatureList);
           // return FeatureCollection.fromJson(geoJson);
        }

        @Override
        protected void onPostExecute(FeatureCollection featureCollection) {
            super.onPostExecute(featureCollection);
            LocationActivity activity = activityRef.get();
            if (featureCollection == null || activity == null) {
                return;
            }
            for (Feature singleFeature : featureCollection.features()) {
                singleFeature.addBooleanProperty(PROPERTY_SELECTED, false);
            }

            activity.setUpData(featureCollection);
            new GenerateViewIconTask(activity).execute(featureCollection);
        }

    }

    //generating the icons in the
    private static class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {

        private final HashMap<String, View> viewMap = new HashMap<>();
        private final WeakReference<LocationActivity> activityRef;
        private final boolean refreshSource;

        GenerateViewIconTask(LocationActivity activity, boolean refreshSource) {
            this.activityRef = new WeakReference<>(activity);
            this.refreshSource = refreshSource;
        }

        GenerateViewIconTask(LocationActivity activity) {
            this(activity, false);
        }

        @SuppressWarnings("WrongThread")
        @Override
        protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
            LocationActivity activity = activityRef.get();
            if (activity != null) {
                HashMap<String, Bitmap> imagesMap = new HashMap<>();
                LayoutInflater inflater = LayoutInflater.from(activity);

                FeatureCollection featureCollection = params[0];

                for (Feature feature : featureCollection.features()) {

                    BubbleLayout bubbleLayout = (BubbleLayout)
                            inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);

                    String name = feature.getStringProperty("NAME");
                    TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
                    titleTextView.setText(name);
                    titleTextView.setTypeface(null, Typeface.BOLD);


                    int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    bubbleLayout.measure(measureSpec, measureSpec);

                    float measuredWidth = bubbleLayout.getMeasuredWidth();

                    bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

                    Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
                    imagesMap.put(name, bitmap);
                    viewMap.put(name, bubbleLayout);
                }

                return imagesMap;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
            super.onPostExecute(bitmapHashMap);
            LocationActivity activity = activityRef.get();
            if (activity != null && bitmapHashMap != null) {
                activity.setImageGenResults(bitmapHashMap);
                if (refreshSource) {
                    activity.refreshSource();
                }
            }
           // Toast.makeText(activity, R.string.tap_on_marker_instruction, Toast.LENGTH_SHORT).show();
        }
    }

    public void setImageGenResults(HashMap<String, Bitmap> imageMap) {
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
// calling addImages is faster as separate addImage calls for each bitmap.
                style.addImages(imageMap);
            });
        }
    }

    private void refreshSource() {
        if (source != null && featureCollection != null) {
            source.setGeoJson(featureCollection);
        }
    }

    private void setupSource(@NonNull Style loadedStyle) {
        source = new GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection);
        loadedStyle.addSource(source);
    }



    /**
     * Utility class to generate Bitmaps for Symbol.
     */
    private static class SymbolGenerator {

        /**
         * Generate a Bitmap from an Android SDK View.
         *
         * @param view the View to be drawn to a Bitmap
         * @return the generated bitmap
         */
        static Bitmap generate(@NonNull View view) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(measureSpec, measureSpec);

            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();

            view.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
    }

}
