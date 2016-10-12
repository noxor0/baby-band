package tmobile.hackathon.babyband;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by William Zulueta on 10/10/16.
 */

public class Profile
{
    private String name;
    private double temp;
    private int lastHeartRate;
    private DatabaseReference database;
    private String location;
    private double lat;
    private double lon;

    private boolean initLat = false;
    private boolean initLong = false;

    public boolean isInitLat()
    {
        return initLat;
    }

    public boolean isInitLong()
    {
        return initLong;
    }

    public Profile(String name, DatabaseReference database)
    {
        this.name = name;
        this.database = database;
        registerListeners();
    }

    public boolean isOk()
    {
        return true;
    }

    public void registerListeners()
    {
        database.child("name").child(name).child("temper").addValueEventListener(new ValueEventListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Double loc = (Double) dataSnapshot.getValue();
                temp = loc;
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        database.child("name").child(name).child("loc").addValueEventListener(new ValueEventListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String loc = (String) dataSnapshot.getValue();
                location = loc;
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        database.child("name").child(name).child("hr").addValueEventListener(new ValueEventListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Long ll = (Long) dataSnapshot.getValue();
                lastHeartRate = ll.intValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        database.child("name").child(name).child("lat").addValueEventListener(new ValueEventListener()
        {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Double ll = (Double) dataSnapshot.getValue();
                lat = ll;
                initLat = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        database.child("name").child(name).child("long").addValueEventListener(new ValueEventListener()
        {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Double ll = (Double) dataSnapshot.getValue();
                lon = ll;
                initLong = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    public int getHeartRate()
    {
        return lastHeartRate;
    }

    public double getLon()
    {
        return lon;
    }

    public double getLat()
    {
        return lat;
    }

    public String getLocation()
    {
        return location;
    }

    public double getTemp()
    {
        return temp;
    }
}
