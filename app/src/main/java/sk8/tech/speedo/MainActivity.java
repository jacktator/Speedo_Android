package sk8.tech.speedo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private TextView mTextMessage;
    private TextView speedLimitTextView;
    private TextView currentSpeedTextView;
    private TextView accelerationTextView;

    private LinearLayout contentFrameLayout;

    private float CONST_SPEED_LIMIT = 20;
    private float CONST_PREDICTION_SECOND = 6;

    private long CONST_MINIMAL_UPDATE_TIME_INTERVAL = 1000;
    private long CONST_MINIMAL_UPDATE_DISTANCE_INTERVAL = 0;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATOIN = 67;

    //The following is temp data, will be deleted one hooked up to sensor
    private float TEMP_CURRENT_SPEED = 10;
    private float TEMP_CURRENT_ACCELERATION = 2;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_one:

                    updateViews();

                    return true;
                case R.id.navigation_two:

                    mTextMessage.setText(R.string.title_second);

                    contentFrameLayout.setBackgroundColor(Color.GREEN);

                    return true;
                case R.id.navigation_three:

                    // Update Background Color
                    contentFrameLayout.setBackgroundColor(Color.WHITE);

                    // Update Text Color
                    mTextMessage.setTextColor(Color.BLACK);
                    speedLimitTextView.setTextColor(Color.BLACK);
                    currentSpeedTextView.setTextColor(Color.BLACK);
                    accelerationTextView.setTextColor(Color.BLACK);


                    // Update texts
                    mTextMessage.setText(R.string.welcome_text);

                    speedLimitTextView.setText(R.string.subtitle_one);
                    currentSpeedTextView.setText(R.string.subtitle_two);
                    accelerationTextView.setText(R.string.subtitle_three);

                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        currentSpeedTextView = (TextView) findViewById(R.id.current_speed_text_view);
        accelerationTextView = (TextView) findViewById(R.id.acceleration_text_view);
        speedLimitTextView = (TextView) findViewById(R.id.speed_limit_text_view);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        contentFrameLayout = (LinearLayout) this.findViewById(R.id.container);

        //Update texts
        mTextMessage.setText(R.string.welcome_text);

        // Settings for Location Manager
        // Acquire a reference to the system Location Manager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // Should we show an explanation?

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATOIN);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

            Toast.makeText(this, "Please grant this app permission to use GPS in Settings", Toast.LENGTH_LONG).show();
            return;
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CONST_MINIMAL_UPDATE_TIME_INTERVAL, CONST_MINIMAL_UPDATE_DISTANCE_INTERVAL, this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATOIN: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Toast.makeText(this, "Thank you! Let's get started!", Toast.LENGTH_SHORT).show();
                    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                    //noinspection MissingPermission
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CONST_MINIMAL_UPDATE_TIME_INTERVAL, CONST_MINIMAL_UPDATE_DISTANCE_INTERVAL, this);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(this, "Please grant this app permission to use GPS in Settings", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private float getCurrentSpeed() {
        return TEMP_CURRENT_SPEED;
    }

    private float getCurrentAcceleration() {
        return TEMP_CURRENT_ACCELERATION;
    }

    private float getCurrentSpeedLimit() {
        return CONST_SPEED_LIMIT;
    }

    private boolean isSpeeding() {
        if (getCurrentSpeed() > getCurrentSpeedLimit()) {
            return true;
        } else {
            return false;
        }
    }

//    private boolean willBeSpeeding() {
//        //Assume we're prediction future speed in 6 seconds
//
//        float currentSpeed = getCurrentSpeed();
//        float acceleration = getCurrentAcceleration();
//        float predictedFutureSpeed = currentSpeed + 6 * acceleration;
//
//        if (predictedFutureSpeed > getCurrentSpeedLimit()) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    private boolean willBeSpeedingInSeconds(float seconds) {
        //Assume we're prediction future speed in 6 seconds

        float currentSpeed = getCurrentSpeed();
        float acceleration = getCurrentAcceleration();
        float predictedFutureSpeed = currentSpeed + seconds * acceleration;

        return predictedFutureSpeed > getCurrentSpeedLimit();
    }

    private void updateViews() {

        if (isSpeeding()) {

            contentFrameLayout.setBackgroundColor(Color.RED);

            mTextMessage.setTextColor(Color.WHITE);
            speedLimitTextView.setTextColor(Color.WHITE);
            currentSpeedTextView.setTextColor(Color.WHITE);
            accelerationTextView.setTextColor(Color.WHITE);
            mTextMessage.setText(R.string.already_speeding_notification);

        } else if (willBeSpeedingInSeconds(CONST_PREDICTION_SECOND)) {

            contentFrameLayout.setBackgroundColor(Color.RED);

            mTextMessage.setTextColor(Color.WHITE);
            speedLimitTextView.setTextColor(Color.WHITE);
            currentSpeedTextView.setTextColor(Color.WHITE);
            accelerationTextView.setTextColor(Color.WHITE);
            mTextMessage.setText("You will be speeding in " + CONST_PREDICTION_SECOND + " seconds!");

        } else {

            contentFrameLayout.setBackgroundColor(Color.WHITE);

            mTextMessage.setTextColor(Color.BLACK);
            speedLimitTextView.setTextColor(Color.BLACK);
            currentSpeedTextView.setTextColor(Color.BLACK);
            accelerationTextView.setTextColor(Color.BLACK);
            mTextMessage.setText("You will NOT be speeding in " + CONST_PREDICTION_SECOND + " seconds!");
        }

        speedLimitTextView.setText("Limit: " + getCurrentSpeedLimit() + " km/h");
        currentSpeedTextView.setText("Current: " + getCurrentSpeed() + " km/h");
        accelerationTextView.setText("Acc: " + getCurrentAcceleration() + " km/h/s");

    }

    /**
     * Called when the location has changed.
     * <p>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "onLocationChanged: " + location.getSpeed(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the provider status changes. This method is called when
     * a provider is unable to fetch a location or if the provider has recently
     * become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     * @param status   {@link LocationProvider#OUT_OF_SERVICE} if the
     *                 provider is out of service, and this is not expected to change in the
     *                 near future; {@link LocationProvider#TEMPORARILY_UNAVAILABLE} if
     *                 the provider is temporarily unavailable but is expected to be available
     *                 shortly; and {@link LocationProvider#AVAILABLE} if the
     *                 provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific
     *                 status variables.
     *                 <p>
     *                 <p> A number of common key/value pairs for the extras Bundle are listed
     *                 below. Providers that use any of the keys on this list must
     *                 provide the corresponding value as described below.
     *                 <p>
     *                 <ul>
     *                 <li> satellites - the number of satellites used to derive the fix
     * @see https://stackoverflow.com/questions/21644990/which-is-best-way-to-calculate-speed-in-android-whether-manual-calculation-usin
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (BuildConfig.DEBUG) Log.d("MainActivity", "LocationProvider status changed: " + status);
    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {
        if (BuildConfig.DEBUG) Log.d("MainActivity", "LocationProvider Enabled");
    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {
        if (BuildConfig.DEBUG) Log.d("MainActivity", "LocationProvider Disabled");
    }

    // TODO: Implement 'fetch data from sensor'
    // @doc: https://stackoverflow.com/questions/20398898/how-to-get-speed-in-android-app-using-location-or-accelerometer-or-some-other-wa
}
