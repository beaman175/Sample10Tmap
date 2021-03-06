package com.example.dongja94.sampletmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TMapView mapView;
    LocationManager mLM;
    String mProvider = LocationManager.GPS_PROVIDER;
    EditText keywordView;
    ListView listView;
    ArrayAdapter<POIItem> mAdapter;
    RadioGroup groupType;

    private static final String API_KEY = "458a10f5-c07e-34b5-b2bd-4a891e024c2a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        keywordView = (EditText)findViewById(R.id.edit_keyword);
        listView = (ListView)findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<POIItem>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                POIItem item = (POIItem)listView.getItemAtPosition(position);
                TMapPoint point = item.poiitem.getPOIPoint();
                moveMap(point.getLatitude(), point.getLongitude());
            }
        });
        groupType = (RadioGroup)findViewById(R.id.group_type);
        mapView = (TMapView) findViewById(R.id.mapView);
        mapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKPMapApikeySucceed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupMap();
                    }
                });
            }

            @Override
            public void SKPMapApikeyFailed(String s) {

            }
        });
        mapView.setSKPMapApiKey(API_KEY);
        Button btn = (Button)findViewById(R.id.btn_marker);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TMapPoint point = mapView.getCenterPoint();

                addMarker("marker" + markerid++, point);
            }
        });

        btn = (Button)findViewById(R.id.btn_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = keywordView.getText().toString();
                if (!TextUtils.isEmpty(keyword)) {
                    searchPOI(keyword);
                }
            }
        });

        btn = (Button)findViewById(R.id.btn_route);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                route();
            }
        });
        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    TMapPoint startPoint, endPoint;

    private void route() {
        if (startPoint != null && endPoint != null) {
            TMapData data = new TMapData();
            data.findPathDataWithType(TMapData.TMapPathType.CAR_PATH, startPoint, endPoint, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(final TMapPolyLine tMapPolyLine) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setPath(tMapPolyLine);
                            startPoint = null;
                            endPoint = null;
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "start or end is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void setPath(TMapPolyLine path) {
        mapView.addTMapPath(path);
        Bitmap start = ((BitmapDrawable)ContextCompat.getDrawable(this, android.R.drawable.ic_input_get)).getBitmap();
        Bitmap end = ((BitmapDrawable)ContextCompat.getDrawable(this, android.R.drawable.ic_input_delete)).getBitmap();
        mapView.setTMapPathIcon(start, end);
    }

    private void searchAroundPOI(String keyword) {
        TMapPoint point = mapView.getCenterPoint();
        searchAroundPOI(keyword, point);
    }

    private void searchAroundPOI(String keyword, TMapPoint point) {
        TMapData data = new TMapData();
        data.findAroundKeywordPOI(point, keyword, 33, 100, new TMapData.FindAroundKeywordPOIListenerCallback() {
            @Override
            public void onFindAroundKeywordPOI(final ArrayList<TMapPOIItem> arrayList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        replacePOI(arrayList);
                    }
                });
            }
        });
    }

    private void searchPOI(String keyword) {
        TMapData data = new TMapData();
        data.findAllPOI(keyword, new TMapData.FindAllPOIListenerCallback() {
            @Override
            public void onFindAllPOI(final ArrayList<TMapPOIItem> arrayList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        replacePOI(arrayList);
                    }
                });
            }
        });
    }

    private void replacePOI(List<TMapPOIItem> poiList) {
        mAdapter.clear();
        for (TMapPOIItem poi : poiList) {
            mAdapter.add(new POIItem(poi));
            addMarker(poi.getPOIID(), poi.getPOIPoint(), poi.getPOIName(), poi.getPOIAddress());
        }
    }

    private int markerid = 0;

    private void addMarker(String markerid, TMapPoint point) {
        addMarker(markerid, point, "Marker", "marker test");
    }

    private void addMarker(String markerid, TMapPoint point, String title, String subtitle) {
        TMapMarkerItem item = new TMapMarkerItem();
        item.setTMapPoint(point);
        Bitmap icon = ((BitmapDrawable)ContextCompat.getDrawable(this, android.R.drawable.ic_input_add)).getBitmap();
        item.setIcon(icon);
        item.setPosition(0.5f, 1);
        Bitmap lefticon = ((BitmapDrawable)ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_info)).getBitmap();
        Bitmap righticon = ((BitmapDrawable)ContextCompat.getDrawable(this, android.R.drawable.ic_input_get)).getBitmap();
        item.setCalloutLeftImage(lefticon);
        item.setCalloutTitle(title);
        item.setCalloutSubTitle(subtitle);
        item.setCalloutRightButtonImage(righticon);
        item.setCanShowCallout(true);
        mapView.addMarkerItem(markerid, item);
    }

    boolean isInitialized = false;
    private void setupMap() {
        Toast.makeText(this, "setup map", Toast.LENGTH_SHORT).show();
        isInitialized = true;
//        mapView.setTrafficInfo(true);
        mapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {
                Toast.makeText(MainActivity.this, "marker : " + tMapMarkerItem.getID(), Toast.LENGTH_SHORT).show();
                switch (groupType.getCheckedRadioButtonId()) {
                    case R.id.radio_start:
                        startPoint = tMapMarkerItem.getTMapPoint();
                        break;
                    case R.id.radio_end:
                        endPoint = tMapMarkerItem.getTMapPoint();
                        break;
                }
            }
        });
        if (cacheLocation != null) {
            moveMap(cacheLocation);
            setMyLocation(cacheLocation);
            cacheLocation = null;
        }
//        mapView.setSightVisible(true);
//        mapView.setCompassMode(true);

    }

    private static final int RC_PERMISSION = 1;

    @Override
    protected void onStart() {
        super.onStart();
        getLocation();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSION);
                return;
            }
            Snackbar.make(mapView, "location permission", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSION);
                        }
                    }).show();
            return;
        }

        mLM.requestSingleUpdate(mProvider, mListener, null);
//        Location location = mLM.getLastKnownLocation(mProvider);
//        if (location != null) {
//            moveMap(location);
//        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.removeUpdates(mListener);
    }



    Location cacheLocation;
    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (!isInitialized) {
                cacheLocation = location;
                return;
            }
            cacheLocation = null;
            moveMap(location);
            setMyLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void moveMap(double lat, double lng) {
        mapView.setCenterPoint(lng, lat);
        mapView.setZoomLevel(17);
    }

    private void moveMap(Location location) {
        moveMap(location.getLatitude(), location.getLongitude());
    }

    private void setMyLocation(Location location) {
        mapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        Bitmap icon = ((BitmapDrawable)(ContextCompat.getDrawable(this, R.mipmap.ic_launcher))).getBitmap();
        mapView.setIcon(icon);
        mapView.setIconVisibility(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
