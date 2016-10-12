package tmobile.hackathon.babyband;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by William Zulueta on 10/9/16.
 */

public class LaunchActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("profile", true);
        startActivity(intent);
        finish();
    }

}
