package tmobile.hackathon.babyband;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{
    private boolean connectionStatus = false;
    private MediaPlayer mediaPlayer;
    private TextView tempatureTextView;
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
    private Profile profile;
    private Timer timer;
    private Handler handler = new Handler();
    private BluetoothAdapter adapter;
    private BluetoothManager manager;
    private boolean inCar = false;
    private BluetoothScanner scanner = new BluetoothScanner();
    private GoogleMap mMap;
    private boolean loaded = false;
    private MapView mapView;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        boolean val = getIntent().getBooleanExtra("profile", true);
        daveProfile = val;

        database = FirebaseDatabase.getInstance().getReference();


        profile = new Profile("dave", database);

        hrv = (HeartRateView) findViewById(R.id.heartRate);
        hrv.setProfile(profile);
        //        homeIcon = (ImageView) findViewById(homeIcon);
        //        homeTV = (TextView) findViewById(homeTV);
        nameTV = (TextView) findViewById(R.id.nameTV);
        tempatureTextView = (TextView) findViewById(R.id.tempatureTV);
        //        statusTextView = (TextView) findViewById(R.id.statusTV);
        //        locationTextView = (TextView) findViewById(R.id.locationTV);
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
                        hrv.invalidate();
                        checkStatus();
                        inCar = false;

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
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onPause()
    {
        super.onPause();
        adapter.stopLeScan(scanner);
        mapView.onPause();
    }

    public void test()
    {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(5000);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkStatus()
    {
        if (profile.getLocation() == null)
            return;
        if (inCar)
            database.child("name").child("dave").child("loc").setValue("car");
        else
        {
            database.child("name").child("dave").child("loc").setValue("house");
        }
        updateTempature(profile.getTemp());
        updateHeartrate(profile.getHeartRate());
        updateHome(profile.getLocation());
        updateLat(profile.getLat());
        updateLon(profile.getLon());
    }

    public void registerListeners()
    {
        String child = (daveProfile) ? "dave" : "jill";

        database = FirebaseDatabase.getInstance().getReference();

        //        database.child("name").child("dave").child("temp").addChildEventListener(new ChildEventListener()
        //        {
        //            @RequiresApi(api = Build.VERSION_CODES.M)
        //            @Override
        //            public void onChildAdded(DataSnapshot dataSnapshot, String s)
        //            {
        //                Log.i("MainActivity", "Data" + dataSnapshot);
        //                String tempString = dataSnapshot.toString(); //temperature=74.75
        //                int i = tempString.indexOf("temperature");
        //                tempString = tempString.substring(i + 12, i + 17);
        //                Log.i("MainActivity", "Parsed Valued : " + tempString);
        //                double value = Double.parseDouble(tempString);
        //                updateTempature(value);
        //            }
        //
        //            @Override
        //            public void onChildChanged(DataSnapshot dataSnapshot, String s)
        //            {
        //
        //            }
        //
        //            @Override
        //            public void onChildRemoved(DataSnapshot dataSnapshot)
        //            {
        //
        //            }
        //
        //            @Override
        //            public void onChildMoved(DataSnapshot dataSnapshot, String s)
        //            {
        //
        //            }
        //
        //            @Override
        //            public void onCancelled(DatabaseError databaseError)
        //            {
        //
        //            }
        //        });

        database.child("name").child(child).child("temper").addValueEventListener(new ValueEventListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Double loc = (Double) dataSnapshot.getValue();
                updateTempature(loc);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        database.child("name").child(child).child("loc").addValueEventListener(new ValueEventListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String loc = (String) dataSnapshot.getValue();
                updateHome(loc);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        database.child("name").child(child).child("hr").addValueEventListener(new ValueEventListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Long ll = (Long) dataSnapshot.getValue();
                updateHeartrate((int) ll.longValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        database.child("name").child(child).child("lat").addValueEventListener(new ValueEventListener()
        {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Double ll = (Double) dataSnapshot.getValue();
                updateLat(ll.doubleValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        database.child("name").child(child).child("long").addValueEventListener(new ValueEventListener()
        {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Double ll = (Double) dataSnapshot.getValue();
                updateLon(ll.doubleValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public void vibrate(int miliseconds)
    {
        Toast.makeText(getApplicationContext(), "Something bad happened", Toast.LENGTH_SHORT).show();
/*                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound);
                mediaPlayer.start();
                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                v.vibrate(miliseconds);*/
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
        if (temp > 80)//LOCATION TAGS
        {
            tempatureTextView.setTextColor(getColor(R.color.bad));
        } else
        {
            tempatureTextView.setTextColor(getColor(R.color.good));
        }
        tempatureTextView.setText("Temperature : " + temp);
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
        if (heartrate > 120 || heartrate < 30)
        {
            heartRateTextView.setTextColor(getColor(R.color.bad));
        } else
            //
            heartRateTextView.setTextColor(getColor(R.color.good));
    }

    //        heartRateTextView.setText("Heartrate " + heartrate + " BPM");

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
        mapView.onResume();
    }

    @Override
    public final void onDestroy()
    {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public final void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    public void updateMap()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationManager services = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = services.getBestProvider(criteria, false);
        Location location = services.getLastKnownLocation(provider);
        LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng babyLatLng = new LatLng(profile.getLat(), profile.getLon());
        Location babyLocation = new Location("");
        babyLocation.setLatitude(babyLatLng.latitude);
        babyLocation.setLongitude(babyLatLng.longitude);

        Log.i("MainActivity", "Location : " + location);
        Log.i("MainActivity", "BabyLocation : " + babyLatLng);
        float distanceInMeters = location.distanceTo(babyLocation);
        final float MILES_TO_METERS = 0.000621371F;
        Log.i("MainActivity", "The baby is " + distanceInMeters * MILES_TO_METERS + " away");

        MarkerOptions babyMarker = new MarkerOptions();
        babyMarker.position(babyLatLng);
        babyMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
        babyMarker.title("Baby");

        mMap.addMarker(new MarkerOptions().position(sydney).title("You")).showInfoWindow();
        mMap.addMarker(babyMarker).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothScanner implements BluetoothAdapter.LeScanCallback
{

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
    {
        if (device.getType() == 2)
        {
            Log.i("Bluetooth", "rssi : " + rssi);
            inCar = true;
            if (inCar && rssi < -90)
            {
                Log.i("Bluetooth", "Addr : " + device.getAddress());
                Log.i("Bluetooth", "Name : " + device.getName());
                Log.i("Bluetooth", "UUID" + device.getUuids());
                Log.i("Bluetooth", "Type : " + device.getType());
                Log.i("Bluetooth", "SOMETHING HAPPEND : " + rssi);
                updateStatus(false);
                test();
            }
        }
    }
}
}
