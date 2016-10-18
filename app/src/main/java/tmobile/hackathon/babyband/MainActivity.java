package tmobile.hackathon.babyband;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
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
    private TextView devicesTextView;
    private TextView locationTextView;
    private ImageView locationImageView;
    private TextView nameTV;
    private TextView heartRateTextView;
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
    private boolean sendNot = false;
    private Place currentPlace;

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
            getActionBar().setHomeAsUpIndicator(R.drawable.drawer);
            getActionBar().setDisplayHomeAsUpEnabled(true);
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
        devicesTextView = (TextView) findViewById(R.id.devicesTextView);
        locationTextView = (TextView) findViewById(R.id.locationTextView);
        locationImageView = (ImageView) findViewById(R.id.locationImage);
        locationImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                launchGoogleMaps();
            }
        });

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
                        if (!profile.isConnected())
                        {
                            return;
                        }
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



        devices = new ArrayList<>();
    }

    private void setUpPlaces()
    {
//        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
                Log.i("MainActivity", "status : " + placeLikelihoods.getStatus());
                float highest = -1f;
                for (PlaceLikelihood pl : placeLikelihoods)
                {
                    Log.i("MainActivity", "Places name : " + pl.getPlace().getName());
                    Log.i("MainActivity", "ll : " + pl.getLikelihood());
                    if (pl.getLikelihood() > highest)
                    {
                        Log.i("MainActivity", "new high");
                        highest = pl.getLikelihood();
                        locationTextView.setText(pl.getPlace().getName());
                    }
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void sendNotification(String text)
    {
        if (sendNot)
            return;
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification n = new Notification.Builder(this).setContentTitle("Babyband").setContentText(text).setSmallIcon(R.drawable.heart).setContentIntent(pIntent).setAutoCancel(true).setPriority(Notification.PRIORITY_HIGH).build();
        n.defaults = Notification.DEFAULT_ALL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
        sendNot = true;

        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                sendNot = false;
            }
        }, 10000);
    }

    public void test()
    {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(2000);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound);
        mediaPlayer.start();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mediaPlayer.stop();
                inAlarm = false;
            }
        }, 5000);

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateTempature(double temp)
    {
        if (temp > 85)//LOCATION TAGS
        {
            sendNotification("Overheating " + temp + "F");
            test();
        } else
        {
            //            tempatureTextView.setTextColor(getColor(R.color.good));
        }
        tempatureTextView.setText("" + (int) temp);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void updateHeartrate(int heartrate)
    {
        if (heartrate > 200)
        {
            sendNotification("Elevated Heartrate " + heartrate + "BPM");
        } else if (heartrate < 30)
        {
            sendNotification("Decreased Heartrate " + heartrate + "BPM");
        }
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

    @Override
    public final void onResume()
    {
        super.onResume();
    }

    @Override
    public final void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public final void onLowMemory()
    {
        super.onLowMemory();
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

    private void launchGoogleMaps()
    {
        String format = "geo:0,0?q=" + Double.toString(profile.getLat()) + "," + Double.toString(profile.getLon()) + "(" + "Baby" + ")";
        Uri uri = Uri.parse(format);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
        private StringBuilder text;

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            data = new AdvertiseData.Builder().addServiceUuid(ParcelUuid.
                    fromString(UUID.nameUUIDFromBytes(scanRecord).toString())).build();
            uuids = data.getServiceUuids();
            id = uuids.get(0).getUuid().toString();

            if (id == null)
                return;

            Log.i("Bluetooth", "name : " + device.getName() + " " + id);

            text = null;
            text = new StringBuilder();

            if (id.equals("577ae0fc-ca17-37f4-8ec5-5884a5941d0f"))
            {
                inCar = true;
                inAlarm = false;
            }

            if (inAlarm)
                return;

            boolean found = false;
            for (int i = devices.size() - 1; i > 0; --i)
            {
                BluDevice bd = devices.get(i);
                bd.increaseAmount();

                if (bd.getAmount() > 15)
                {
                    devices.remove(bd);
                    if (bd.getName().equals("Stacey's Model S"))
                    {
                        sendNotification("Don't leave your baby!");
                        inAlarm = true;
                        test();
                        inCar = false;
                        devicesTextView.setText("Stacey's Model S");
                        return;
                    }
                }

                if (bd.getAddr().equals(id))
                {
                    bd.reset();
                    found = true;
                }

                if (!bd.getName().equals("null"))
                {
                    if (text.length() > 0)
                    {
                        text.append(", " + bd.getName());
                    } else
                    {
                        text.append(bd.getName());
                    }
                }

            }
            if (!found)
            {
                Log.i("Bluetooth", "Adding : " + id);
                devices.add(new BluDevice(id));
            }

            devicesTextView.setText(text.toString());


            //            if (id.contains("a85568e7-e011-3134-bbba-0a564f8130ea") && !inAlarm)
            //            {
            //                Log.i("Bluetooth", "rssi : " + rssi);
            //                if (rssi < -95 && inCar)
            //                {
            //                    sendNotification("Don't leave your baby!");
            //                    inAlarm = true;
            //                    test();
            //                }
            //            }
        }
    }
}
