package tmobile.hackathon.babyband;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener// implements OnMapReadyCallback
{
    private ArrayList<BluDevice> devices;
    private GoogleApiClient googleApiClient;
    private boolean connectionStatus = false;
    private MediaPlayer mediaPlayer;
    private TextView tempatureTextView;
    private ImageView watchImageView;
    private TextView devicesTextView;
    //    private TextView statusTextView;
    //    private TextView locationTextView;
    private TextView nameTV;
    private TextView heartRateTextView;
    //    private TextView homeTV;
    //    private ImageView homeIcon;
    private DatabaseReference database;
    private double lastLLVal = 0;
    private double lastLTVal = 0;
    private boolean daveProfile = true;
    private HeartRateView hrv;
    private TemperatureView temperatureView;
    private Profile profile;
    private Timer timer;
    private Handler handler = new Handler();
    private BluetoothAdapter adapter;
    private BluetoothManager manager;
    private boolean inCar = false;
    private BluetoothScanner scanner = new BluetoothScanner();
    private GoogleMap mMap;
    private boolean loaded = false;
    //    private MapView mapView;
    private boolean inAlarm = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todd);

        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).build();

        if (getActionBar() != null)
        {
            getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_red)));
            getActionBar().setTitle("Emma");
        }
        //        mapView = (MapView) findViewById(R.id.mapView);
        //        mapView.onCreate(savedInstanceState);
        //        mapView.getMapAsync(this);

        boolean val = getIntent().getBooleanExtra("profile", true);
        daveProfile = val;

        database = FirebaseDatabase.getInstance().getReference();


        profile = new Profile("dave", database);

        hrv = (HeartRateView) findViewById(R.id.heartRate);
        hrv.setProfile(profile);
        //        temperatureView = (TemperatureView) findViewById(R.id.temperatureView);
        //        watchImageView = (ImageView) findViewById(R.id.watchIV);
        //        nameTV = (TextView) findViewById(R.id.nameTV);
        tempatureTextView = (TextView) findViewById(R.id.tempatureTV);
        heartRateTextView = (TextView) findViewById(R.id.heartrateTV);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                handler.post(new Runnable()
                {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run()
                    {
                        //                        if (!profile.isConnected())
                        //                        {
                        //                            watchImageView.setImageResource(R.drawable.watch_off);
                        //                            return;
                        //                        }
                        //                        watchImageView.setImageResource(R.drawable.watch_on);
                        //                        temperatureView.invalidate();
                        hrv.invalidate();
                        checkStatus();
                        //                        inCar = false;
                        //
                        if (profile.isInitLat() && profile.isInitLong() && !loaded)
                        {
                            updateMap();
                            loaded = true;
                        }
                    }
                });
            }
        }, 1000, 1000);

        manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        adapter.startDiscovery();
        adapter.startLeScan(scanner);

        devices = new ArrayList<>();
    }

    private void setUpPlaces()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(googleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>()
        {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods)
            {
                Log.i("MainActivity", "pl : " + placeLikelihoods.toString());
                for (PlaceLikelihood pl : placeLikelihoods)
                {
                    Log.i("MainActivity", "Places name : " + pl.getPlace().getName());
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onPause()
    {
        super.onPause();
        adapter.stopLeScan(scanner);
        //        mapView.onPause();
    }

    public void test()
    {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(5000);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound);
        mediaPlayer.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkStatus()
    {
        //        if (profile.getLocation() == null)
        //            return;
        //        if (inCar)
        //            database.child("name").child("dave").child("loc").setValue("car");
        //        else
        //        {
        //            database.child("name").child("dave").child("loc").setValue("house");
        //        }
        updateTempature(profile.getTemp());
        updateHeartrate(profile.getHeartRate());
        //        updateHome(profile.getLocation());
        //        updateLat(profile.getLat());
        //        updateLon(profile.getLon());
    }

    public void vibrate(int miliseconds)
    {
        Toast.makeText(getApplicationContext(), "Something bad happened", Toast.LENGTH_SHORT).show();

        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(miliseconds);
    }

    public void updateHome(String loc)
    {
        if (loc.equals("house"))
        {
            //            homeIcon.setImageResource(R.drawable.ic_home_black_24dp);
        } else if (loc.equals("car"))
        {
            //            homeIcon.setImageResource(R.drawable.ic_directions_car_black_24dp);
        }
        //        homeTV.setText("Location : " + loc);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateTempature(double temp)
    {
        if (temp > 85)//LOCATION TAGS
        {
            test();
            //            tempatureTextView.setTextColor(getColor(R.color.bad));
        } else
        {
            //            tempatureTextView.setTextColor(getColor(R.color.good));
        }
        tempatureTextView.setText("" + temp);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateStatus(boolean ok)
    {
        if (!ok)
        {
            //            statusTextView.setTextColor(getColor(R.color.bad));
            //            sendNotification();
            vibrate(5000);
        } else
        {
            if (mediaPlayer != null)
                mediaPlayer.stop();
            //            statusTextView.setTextColor(getColor(R.color.good));
        }
        //        statusTextView.setText("Status : " + ((ok) ? "Ok" : "Bad"));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateHeartrate(int heartrate)
    {
        //        if (heartrate > 120 || heartrate < 30)
        //        {
        //            heartRateTextView.setTextColor(getColor(R.color.bad));
        //            test();
        //        } else
        //            heartRateTextView.setTextColor(getColor(R.color.good));
        heartRateTextView.setText(heartrate + "");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateLat(double lat)
    {
        lastLTVal = lat;
        //        locationTextView.setText("LT : " + lat + " LL : " + lastLLVal);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateLon(double lon)
    {
        lastLLVal = lon;
        //        locationTextView.setText("LT : " + lastLTVal + " LL : " + lon);
    }

    public void launchDaveActivity()
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("profile", true);
        startActivity(intent);
        finish();
    }

    public void launchJillActivity()
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("profile", false);
        startActivity(intent);
        finish();
    }

    @Override
    public final void onResume()
    {
        super.onResume();
        //        mapView.onResume();
    }

    @Override
    public final void onDestroy()
    {
        super.onDestroy();
        //        mapView.onDestroy();
    }

    @Override
    public final void onLowMemory()
    {
        super.onLowMemory();
        //        mapView.onLowMemory();
    }

    //    @Override
    //    public void onMapReady(GoogleMap googleMap)
    //    {
    //        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
    //        {
    //            return;
    //        }
    //        mMap = googleMap;
    //        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    //    }

    @Override
    public void onStart()
    {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        setUpPlaces();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i("MainActivity", "Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Log.i("MainActivity", "failed : " + connectionResult);
    }

    public void updateMap()
    {
        //        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        //        {
        //            return;
        //        }
        //        LocationManager services = (LocationManager) getSystemService(LOCATION_SERVICE);
        //        Criteria criteria = new Criteria();
        //        String provider = services.getBestProvider(criteria, false);
        //        Location location = services.getLastKnownLocation(provider);
        //        LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
        //        LatLng babyLatLng = new LatLng(profile.getLat(), profile.getLon());
        //        Location babyLocation = new Location("");
        //        babyLocation.setLatitude(babyLatLng.latitude);
        //        babyLocation.setLongitude(babyLatLng.longitude);
        //
        //        Log.i("MainActivity", "Location : " + location);
        //        Log.i("MainActivity", "BabyLocation : " + babyLatLng);
        //        float distanceInMeters = location.distanceTo(babyLocation);
        //        final float MILES_TO_METERS = 0.000621371F;
        //        Log.i("MainActivity", "The baby is " + distanceInMeters * MILES_TO_METERS + " away");
        //
        //        MarkerOptions babyMarker = new MarkerOptions();
        //        babyMarker.position(babyLatLng);
        //        babyMarker.title("Baby");
        //
        ////        mMap.addMarker(new MarkerOptions().position(sydney).title("You")).showInfoWindow();
        //        mMap.addMarker(babyMarker).showInfoWindow();
        //        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    public void displayDevices()
    {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    class BluetoothScanner implements BluetoothAdapter.LeScanCallback
    {
        private List<ParcelUuid> uuids;
        private AdvertiseData data;
        private String id;

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            data = new AdvertiseData.Builder().addServiceUuid(ParcelUuid.
                    fromString(UUID.nameUUIDFromBytes(scanRecord).toString())).build();
            uuids = data.getServiceUuids();
            id = uuids.get(0).getUuid().toString();
            //            StringBuilder s = new StringBuilder();
            //            for (byte b : scanRecord)
            //            {//577ae0fc-ca17-37f4-8ec5-5884a5941d0f
            //                s.append(String.format("%02x", b));
            //            }
            Log.i("Bluetooth", "Device : " + id);
            //        Log.i("Bluetooth", "Name : " + device.getName());

            Log.i("Bluetooth", "name : " + device.getName() + " " + id);

            boolean found = false;
            for (int i = devices.size(); i > 0; ++i)
            {
                BluDevice bd = devices.get(i);
                bd.increaseAmount();
                if (bd.getAmount() > 100)
                {
                    devices.remove(device);
                }
                if (bd.getAddr().contains(id))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                devices.add(new BluDevice(id));
            }


            //            if (id.contains("a85568e7-e011-3134-bbba-0a564f8130ea") && !inAlarm)
            //            {
            //                Log.i("Bluetooth", "rssi : " + rssi);
            //                inCar = true;
            //                if (inCar && rssi < -80)
            //                {
            ////                    inAlarm = true;
            //                    Log.i("Bluetooth", "Addr : " + device.getAddress());
            //                    Log.i("Bluetooth", "Name : " + device.getName());
            //                    Log.i("Bluetooth", "UUID" + device.getUuids());
            //                    Log.i("Bluetooth", "Type : " + device.getType());
            //                    Log.i("Bluetooth", "SOMETHING HAPPEND : " + rssi);
            //                    updateStatus(false);
            ////                    test();
            //                }
            //            }
        }
    }
}
