package com.kawasdk.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kawasdk.R;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.smartlook.sdk.smartlook.Smartlook;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

public class Common extends AppCompatActivity {

    private static Animation animVerticala;
    public static ProgressDialog proDialog;
    public static Context context;
    public static ImageView imageline;
    public static double MXCAMERALAT;
    public static double MXCAMERALNG;
    public static double CAMERALAT;
    public static double CAMERALNG;
    public static double MAXZOOM = 22.00;
    public static double MINZOOM = 5.00;
    public static double MAPZOOM = 17.00;
    public static ProgressBar PROGRESSBAR;
    public static final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1Ijoia2F3YS1hZG1pbiIsImEiOiJja3RqcmN3N2kwNWEyMzJueWQzd2J0Znk1In0.WK1trBUr51BifsBNRX5ekw"; // MAPBOX TOKEN
    //public static final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoicnVwZXNoamFpbiIsImEiOiJja3JwdmdneGU1NHlxMnpwODN6bzFpbnkwIn0.UgSIBr9ChJFyrAKxtdNf9w"; // OLd MAPBOX TOKEN
    public static final String BASE_URL = "https://data.kawa.space/"; // live url
    public static final String ADRESS_URL = "https://nominatim.openstreetmap.org/"; // live url
    //public static final String BASE_URL = "https://data-staging.kawa.space/"; // test url
    public static final String SDK_VERSION = android.os.Build.VERSION.SDK;
    private static final int PERMISSION_REQUEST_CODE = 100;
    public static String FARMS_FETCHED_AT = "";
    public static InterfaceKawaEvents interfaceKawaEvents;
    public static String SEGMENT_KEY = "IKuQjAPnvs0jDZtAj2z52b7yuDrjM1Zm";
    public static String USER_NAME; // for avoid submit api call
    public static String USER_ADDRESS; // for avoid submit api call
    public static String USER_COMPANY; // for avoid submit api call

    //1 - for all functnality with edit
    // 2 - for avoid merge and submit api call
    // 3 - for avoid submit api call
    // 4 - for merge api and address api
    public static String PHASERSTR = "3";
    // 'in' for bahasha lanuage.
    // 'en' for english lanuage.
    public static String LANGUAGE = "en";

    public Common(Context context) {
        this.context = context;
    }

    public static void drawMapLayers(Style style, List<Point> llPts, String id, String type) {
        List<List<Point>> llPtsA = new ArrayList<>();
        llPtsA.add(llPts);
        float opacityP = 0.6f;
        float opacityL = 0.0f;
        Integer color = null;

        if (type.equals("edit")) {
            opacityL = 1.0f;
            color = Color.parseColor("#c9577e");
        } else {
            Random random = new Random();
            int colorR = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            color = Integer.parseInt(String.valueOf(colorR));
        }

        style.addSource(new GeoJsonSource("polySourceID" + id, Polygon.fromLngLats(llPtsA)));
        style.addLayerBelow(new FillLayer("polyLayerID" + id, "polySourceID" + id).withProperties(fillColor(color), fillOpacity(opacityP)), "settlement-label");

        style.addSource(new GeoJsonSource("lineSourceID" + id, FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(llPts))})));
        style.addLayer(new LineLayer("lineLayerID" + id, "lineSourceID" + id).withProperties(
                PropertyFactory.lineWidth(4f),
                PropertyFactory.lineColor(Color.parseColor("#000000")),
                PropertyFactory.lineOpacity(opacityL)
        ));
    }

    public static void initMarker(Style style, MapboxMap MAPBOXMAP, MapView MAPVIEW) {
        interfaceKawaEvents = (InterfaceKawaEvents) context;
        Bitmap marker_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker);
        addMarker(style, marker_image);
        MAPBOXMAP.addOnMoveListener(new MapboxMap.OnMoveListener() {
            @Override
            public void onMoveBegin(MoveGestureDetector detector) {
                setMarkerPosition(style, MAPBOXMAP);
            }

            @Override
            public void onMove(@NonNull MoveGestureDetector detector) {
                setMarkerPosition(style, MAPBOXMAP);
            }

            @Override
            public void onMoveEnd(MoveGestureDetector detector) {
                setMarkerPosition(style, MAPBOXMAP);
                onkawaUpdatechange(interfaceKawaEvents, MAPBOXMAP);
            }
        });

        MAPBOXMAP.addOnCameraMoveCancelListener(() -> setMarkerPosition(style, MAPBOXMAP));
        MAPBOXMAP.addOnCameraIdleListener(() -> setMarkerPosition(style, MAPBOXMAP));
        MAPBOXMAP.addOnFlingListener(() -> setMarkerPosition(style, MAPBOXMAP));
        MAPVIEW.addOnDidFinishLoadingStyleListener(() -> setMarkerPosition(style, MAPBOXMAP));
    }

    public static void setMarkerPosition(Style style, MapboxMap MAPBOXMAP) {
        CameraPosition cameraPosition = MAPBOXMAP.getCameraPosition();
        LatLng location = cameraPosition.target;
        //MAPZOOM = cameraPosition.zoom;
        CAMERALAT = location.getLatitude();
        CAMERALNG = location.getLongitude();

        GeoJsonSource markerSorceID = style.getSourceAs("markerSorceID");
        if (markerSorceID != null) {
            markerSorceID.setGeoJson(Point.fromLngLat(CAMERALNG, CAMERALAT));
        }
    }

    public static void addMarker(Style style, Bitmap marker_image) {
        style.addSource(new GeoJsonSource("markerSorceID"));
        style.addImage("marker_image", marker_image);
        style.addLayer(new SymbolLayer("markerID", "markerSorceID").withProperties(
                PropertyFactory.iconImage("marker_image"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconSize(0.6F)
        ));
    }

    public static void setZoomLevel(float val, MapboxMap MAPBOXMAP) {
        CameraPosition cameraPosition = MAPBOXMAP.getCameraPosition();
        double zoomleval = cameraPosition.zoom;
        double previouszoomleval = zoomleval;
        double previousLat = CAMERALAT;
        double previousLng = CAMERALNG;
        String previousvisibleRegion = getVisibleRegion(MAPBOXMAP);
        String visibleRegion = "";
        Properties properties = new Properties();
        String fildsInformation = getFiledsDetails();
        Log.e("zoomleval", String.valueOf(zoomleval));
        if (val == 1.0 && zoomleval >= 2 && zoomleval < 3) {
            zoomleval = 3.1;
        }
        if (zoomleval > 3) {
            MAPBOXMAP.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition.target, zoomleval + val), 1000);
            visibleRegion = getVisibleRegion(MAPBOXMAP);
            zoomleval = zoomleval + val;
        }
        if (KawaMap.SEGMENT_API_KEY != "" && KawaMap.SEGMENT_API_KEY != null) {
            String jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + "Zoom leval saved" + "\"" + ",\"previousCoordinates\":{\"lat\":" + "\"" +
                    previousLat + "\"" + ",\"long\":" + "\"" + previousLng + "\"" + "},\"currentCoordinates\":{\"lat\":" + "\"" +
                    CAMERALAT + "\"" + ",\"long\":" + "\"" + CAMERALNG + "\"" + "}, \"previousViewport\": " +
                    previousvisibleRegion + ", \"currentViewport\": " + visibleRegion + ",\"previousZoom\": " + previouszoomleval + ",\"currentZoom\": " + zoomleval + "}}";
            Log.e("TAG", "segmentInit: " + jString);
            JsonObject jsonObject = JsonParser.parseString(jString).getAsJsonObject();
            properties.putValue("data", jsonObject);

            String zoomType = "";
            if (val == 1.0) {
                zoomType = "Zoom in";
            } else {
                zoomType = "Zoom out";
            }
            Analytics.with(context).track(zoomType, properties);
        }
    }

    public static void lockZoom(MapboxMap MAPBOXMAP) {
        new android.os.Handler(Looper.getMainLooper()).postDelayed(
                new Runnable() {
                    public void run() {
                        VisibleRegion vRegion = MAPBOXMAP.getProjection().getVisibleRegion();
                        LatLng BOUND_CORNER_NW = new LatLng(vRegion.nearLeft.getLatitude(), vRegion.nearLeft.getLongitude());
                        LatLng BOUND_CORNER_SE = new LatLng(vRegion.farRight.getLatitude(), vRegion.farRight.getLongitude());
                        LatLngBounds RESTRICTED_BOUNDS_AREA = new LatLngBounds.Builder()
                                .include(BOUND_CORNER_NW)
                                .include(BOUND_CORNER_SE)
                                .build();
                        MAPBOXMAP.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA);
                        MAPBOXMAP.setMinZoomPreference(MAPZOOM);
                    }
                },
                3000);
    }

    public static boolean checkLatLongInPolygon(LatLng coordsOfPoint, List<LatLng> latlngsOfPolygon) {
        int i;
        int j;
        boolean contains = false;
        for (i = 0, j = latlngsOfPolygon.size() - 1; i < latlngsOfPolygon.size(); j = i++) {
            if ((latlngsOfPolygon.get(i).getLongitude() > coordsOfPoint.getLongitude()) != (latlngsOfPolygon.get(j).getLongitude() > coordsOfPoint.getLongitude()) &&
                    (coordsOfPoint.getLatitude() < (latlngsOfPolygon.get(j).getLatitude() - latlngsOfPolygon.get(i).getLatitude()) * (coordsOfPoint.getLongitude() - latlngsOfPolygon.get(i).getLongitude()) / (latlngsOfPolygon.get(j).getLongitude() - latlngsOfPolygon.get(i).getLongitude()) + latlngsOfPolygon.get(i).getLatitude())) {
                contains = !contains;
            }
        }
        return contains;
    }

    public static void showLoader(String loaderType) {
        if (proDialog == null || !proDialog.isShowing()) {
            proDialog = new ProgressDialog(context);
            proDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            proDialog.show();
            View view = LayoutInflater.from(context).inflate(R.layout.loader, null);
            proDialog.setContentView(view);
            imageline = view.findViewById(R.id.imageline);
            PROGRESSBAR = view.findViewById(R.id.progress_circular);
            if (loaderType.equals("isScanner")) {
                PROGRESSBAR.setVisibility(View.GONE);
                imageline.setVisibility(View.VISIBLE);
                animVerticala = AnimationUtils.loadAnimation(context,
                        R.anim.slide_down);
                imageline.startAnimation(animVerticala);
            } else {
                imageline.setVisibility(View.GONE);
                PROGRESSBAR.setVisibility(View.VISIBLE);
            }
        }
    }


    public static void hideLoader() {
        if (proDialog != null && proDialog.isShowing()) {
            imageline.clearAnimation();
            proDialog.dismiss();
        }
    }

    public static void onkawaUpdatechange(InterfaceKawaEvents listner, MapboxMap MAPBOXMAP) {
        VisibleRegion vRegion = MAPBOXMAP.getProjection().getVisibleRegion();
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject centerPointobject = new JSONObject();

            interfaceKawaEvents = listner;
            if (interfaceKawaEvents != null) {
                centerPointobject.put("latitude", CAMERALAT);
                centerPointobject.put("longitude", CAMERALAT);
                centerPointobject.put("zoomLeval", MAPZOOM);
                jsonObject.put("center_position", centerPointobject);
                jsonObject.put("four_corners", vRegion);
                interfaceKawaEvents.onkawaUpdate(jsonObject);
            }
        } catch (Exception e) {

        }
    }

    public void setLocale(Activity context) {
        Log.e("TAG", "setLocale: ");
        String languageToLoad = "en";
        if (KawaMap.isBahasaEnable) {
            languageToLoad = "in";
        }
        // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getBaseContext().getResources().updateConfiguration(config,
                context.getBaseContext().getResources().getDisplayMetrics());
    }

    public static boolean checkFileManagerPermission() {
        int result = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            Log.e("PERMISSION", "PERMISSION_GRANTED");
            return true;
        } else {
            return false;
        }
    }
    public static void requestFileManagerPermission() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                    Toast.makeText(context, "Cannot save farms.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public static void smartlookEvent(String eventName) {
        JSONObject props = new JSONObject();

        try {
            props.put("Lat", CAMERALAT);
            props.put("Long", CAMERALNG);
            props.put("zoom", MAPZOOM);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Smartlook.trackCustomEvent(eventName, props);
    }

    public static String getVisibleRegion(MapboxMap mapboxMap) {
        VisibleRegion vRegion = mapboxMap.getProjection().getVisibleRegion();
        String vRegionStr = "{  \"type\": \"Feature\", \"geometry\": { \"type\": \"Polygon\", \"coordinates\": " +
                "[[[" + vRegion.farLeft.getLongitude() + ", " + vRegion.farLeft.getLatitude() + "], " +
                "[" + vRegion.nearLeft.getLongitude() + ", " + vRegion.nearLeft.getLatitude() + "], " +
                "[" + vRegion.nearRight.getLongitude() + ", " + vRegion.nearRight.getLatitude() + "], " +
                "[" + vRegion.farRight.getLongitude() + ", " + vRegion.farRight.getLatitude() + "], " +
                "[" + vRegion.farLeft.getLongitude() + "," + vRegion.farLeft.getLatitude() + "]]]} }";
        return vRegionStr;
    }

    public static String getFiledsDetails() {
        String fildsStr;
        if (USER_NAME == null && USER_ADDRESS == null) {
            fildsStr = "{\"user\":{}";
        } else if (USER_COMPANY != null) {
            fildsStr = "{\"user\":{\"name\":" + "\"" + USER_NAME + "\"" + ",\"address\":" + "\"" + USER_ADDRESS + "\"" + "}";
        } else {
            fildsStr = "{\"user\":{\"name\":" + "\"" + USER_NAME + "\"" + ",\"address\":" + "\"" + USER_ADDRESS + "\"" + ",\"company\":" + "\"" + USER_COMPANY + "\"" + "}";
        }
        return fildsStr;
    }

    public static void segmentEvents(Context context, String eventname, String message, MapboxMap mapboxMap, String responsemsg, String eventType) {
        String jString = "";
        Properties properties = new Properties();
        String visibleRegion = getVisibleRegion(mapboxMap);
        String fildsInformation = getFiledsDetails();
        if (KawaMap.SEGMENT_API_KEY != "" && KawaMap.SEGMENT_API_KEY != null) {

            switch (eventType) {
                case "CURRENT_LOC":
                    jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + message + "\"" + ", \"viewport\": "
                            + visibleRegion + ",\"coordinates\":{\"lat\":" + "\"" + CAMERALAT + "\"" + ",\"long\":" + "\"" + CAMERALNG + "\"" + "}}  }";
                    break;
                case "SEARCH":
                    jString = fildsInformation + ",\"metadata\":{\"query\":" + "\"" + responsemsg + "\"" + ", \"tappedResult\":{ \"name\":" +
                            "\"" + message + "\"" + ",\"coordinates\":{\"lat\":" + "\"" + CAMERALAT + "\"" + ",\"long\":" + "\"" + CAMERALNG + "\"" + "}}  }}";
                    break;
                case "GET_FARMS":
                    jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + message + "\"" + ",\"coordinates\":{\"lat\":" + "\"" + CAMERALAT + "\"" + ",\"long\":" + "\"" + CAMERALNG + "\"" + "}, \"viewport\": " +
                            visibleRegion + ",\"response\":" + "\"" + responsemsg + "\"}}";
                    break;
                case "FARMS_SELECTION":
                    jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + "boundary selection saved " + "\"" + ",\"coordinates\":{\"lat\":" + "\"" + CAMERALAT + "\"" + ",\"long\":" + "\"" + CAMERALNG + "\"" + "}, \"viewport\": " +
                            visibleRegion + ",\"coordinates\":" + responsemsg + ",\"eventType\":" + message + "}}";
                    break;
                case "SAVE_ON_SUCCESS":
                    jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + "Selection Data Saved" + "\"" + ",\"coordinates\":{\"lat\":" + "\"" + CAMERALAT + "\"" + ",\"long\":" + "\"" + CAMERALNG + "\"" + "}, \"viewport\": " +
                            visibleRegion + ",\"resultantFarms\":" + message + ",\"mergeAPI\":{\"message\":\"Data save on success\",\"response\":" + responsemsg + "}}}";
                    break;
                case "SAVE_ON_FAILURE":
                    jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + "Selection Data Saved" + "\"" + ",\"coordinates\":{\"lat\":" + "\"" + CAMERALAT + "\"" + ",\"long\":" + "\"" + CAMERALNG + "\"" + "}, \"viewport\": " +
                            visibleRegion + ",\"resultantFarms\":" + message + ",\"mergeAPI\":{\"message\":\"Data saved on failure\",\"error\":" + responsemsg + "}}}";
                    break;
                case "ADD_MORE_PLOTS":
                    jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + message + "\"" + ",\"coordinates\":{\"lat\":" + "\"" + CAMERALAT + "\"" + ",\"long\":" + "\"" + CAMERALNG + "\"" + "}, \"viewport\": " +
                            visibleRegion + "}}";
                    break;
                case "SAVE_DETAILS":
                    jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + message + "\"" + ",\"coordinates\":{\"lat\":" + "\"" + CAMERALAT + "\"" + ",\"long\":" + "\"" + CAMERALNG + "\"" + "}, \"viewport\": " +
                            visibleRegion + ",\"resultantFarms\":" + responsemsg + "}}";
                    break;
                case "START_OVER":
                case "MARK_ANOTHER_PLOTS":
                    jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + message + "\"" + "}}";
                    break;
                case "GET_ALL_POLYGON_DATA":
                    jString = fildsInformation + ",\"metadata\":{\"message\":" + "\"" + message + "\"" + ",\"api_response\":" + responsemsg + "}}";
                    break;
            }
            Log.e("TAG", "segmentInit: " + jString);
            try {
                JsonObject jsonObject = JsonParser.parseString(jString).getAsJsonObject();
                properties.putValue("data", jsonObject);
                Log.e("TAG", "segment>>: " + properties);
                Analytics.with(context).track(eventname, properties);
            } catch (Exception e) {
                Toast.makeText(context, String.valueOf(e.getMessage()), Toast.LENGTH_LONG).show();
            }
        }
    }
}

