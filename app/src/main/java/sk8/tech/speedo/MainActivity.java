package sk8.tech.speedo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {
    private TextView mTextMessage;
    private TextView speedLimitTextView;
    private TextView currentSpeedTextView;
    private TextView accelerationTextView;

    private LinearLayout contentFrameLayout;

    private int selectedTab = 0;

    private float CONST_SPEED_LIMIT = 15;
    private float CONST_PREDICTION_SECOND = 6;

    private float CURRENT_SPEED = 0;
    private float CURRENT_ACCELERATION = 0;
    private Time CURRENT_TIME = new Time();
    private float accX;
    private float accY;
    private float accZ;
    private long lastEvent;
    private long deltaT;
    private float Vx;
    private float Vy;
    private float Vz;

    private long CONST_MINIMAL_UPDATE_TIME_INTERVAL = 1000;
    private long CONST_MINIMAL_UPDATE_DISTANCE_INTERVAL = 0;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATOIN = 67;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_one:

                    selectedTab = 0;
                    updateViews();

                    return true;
                case R.id.navigation_two:

                    selectedTab = 1;
                    updateViews();

                    return true;
                case R.id.navigation_three:

                    selectedTab = 2;
                    updateViews();

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

        // Reset current values;
        CURRENT_SPEED = 0;
        CURRENT_ACCELERATION = 0;
        Time currentTime = new Time();
        currentTime.setToNow();

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

        // Listens to LocationManager for Speed
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CONST_MINIMAL_UPDATE_TIME_INTERVAL, CONST_MINIMAL_UPDATE_DISTANCE_INTERVAL, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, CONST_MINIMAL_UPDATE_TIME_INTERVAL, CONST_MINIMAL_UPDATE_DISTANCE_INTERVAL, this);

        // Listens to SensorManager for acceleration
        SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @SuppressWarnings("MissingPermission")
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CONST_MINIMAL_UPDATE_TIME_INTERVAL, CONST_MINIMAL_UPDATE_DISTANCE_INTERVAL, this);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, CONST_MINIMAL_UPDATE_TIME_INTERVAL, CONST_MINIMAL_UPDATE_DISTANCE_INTERVAL, this);

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
        return CURRENT_SPEED;
    }

    private float getCurrentAcceleration() {
        return CURRENT_ACCELERATION;
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
//        if (acceleration >= 2000) {
//            // This case, acceleration is more than 2000 km/h/s, a rocket? haha
//            // This is a result of initial acceleration, no need to worry.
//            return false;
//        } else {
            // This is real meaningful acceleration
            float predictedFutureSpeed = currentSpeed + seconds * acceleration;
            return predictedFutureSpeed > getCurrentSpeedLimit();
//        }

    }

    private void updateViews() {

        if (selectedTab == 0) {
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
            String currentSpeed = String.format("%.1f", getCurrentSpeed()) + " km/h";
            currentSpeedTextView.setText("Current: " + currentSpeed);
            String currentAcc = String.format("%.1f", getCurrentAcceleration()) + " km/h/s";
            accelerationTextView.setText("Acc: " + currentAcc);
        } else if (selectedTab == 1) {
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

                //Assume we're prediction future speed in 6 seconds

                float currentSpeed = getCurrentSpeed();
                float acceleration = getCurrentAcceleration();

                float speedToSpeeding = getCurrentSpeedLimit() - currentSpeed;
                if (speedToSpeeding < 3) {
                    mTextMessage.setText("You are about to speeding in " + CONST_PREDICTION_SECOND + " seconds!");
                    contentFrameLayout.setBackgroundColor(Color.RED);

                    mTextMessage.setTextColor(Color.WHITE);
                    speedLimitTextView.setTextColor(Color.WHITE);
                    currentSpeedTextView.setTextColor(Color.WHITE);
                    accelerationTextView.setTextColor(Color.WHITE);
                } else if (speedToSpeeding < 5) {
                    mTextMessage.setText("You are likely to be speeding in " + CONST_PREDICTION_SECOND + " seconds!");
                    contentFrameLayout.setBackgroundColor(Color.YELLOW);

                    mTextMessage.setTextColor(Color.WHITE);
                    speedLimitTextView.setTextColor(Color.WHITE);
                    currentSpeedTextView.setTextColor(Color.WHITE);
                    accelerationTextView.setTextColor(Color.WHITE);
                } else {
                    mTextMessage.setText("You are good, not likely to be speed in " + CONST_PREDICTION_SECOND + " seconds!");
                    contentFrameLayout.setBackgroundColor(Color.WHITE);

                    mTextMessage.setTextColor(Color.BLACK);
                    speedLimitTextView.setTextColor(Color.BLACK);
                    currentSpeedTextView.setTextColor(Color.BLACK);
                    accelerationTextView.setTextColor(Color.BLACK);
                }
            }

            speedLimitTextView.setText("Limit: " + getCurrentSpeedLimit() + " km/h");
            String currentSpeed = String.format("%.1f", getCurrentSpeed()) + " km/h";
            currentSpeedTextView.setText("Current: " + currentSpeed);
            String currentAcc = String.format("%.1f", getCurrentAcceleration()) + " km/h/s";
            accelerationTextView.setText("Acc: " + currentAcc);
        } else if (selectedTab == 2) {
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
        }

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

        float PREVIOUS_SPEED = CURRENT_SPEED;
        Time previousTime = CURRENT_TIME;

        // Converting unit from meter/second to km/h
        float meterPerSecondSpeed = location.getSpeed();
        float meterPerHourSpeed = meterPerSecondSpeed * 60 * 60;
        float kmPerHourSpeed = meterPerHourSpeed / 1000;
        CURRENT_SPEED = kmPerHourSpeed;

        //Computation for acceleration based on locaiton
        Time currentTime = new Time();
        currentTime.setToNow();
        float differenceOfSpeedInKmPerHour = CURRENT_SPEED - PREVIOUS_SPEED;
        float differenceOfTimeInSecond = TimeUnit.MILLISECONDS.toSeconds(currentTime.toMillis(true)-previousTime.toMillis(true));
        float differenceOfTimeInMinute = differenceOfTimeInSecond / 60;
        float differenceOfTimeInHour = differenceOfTimeInMinute / 60;
        CURRENT_TIME = currentTime;
        CURRENT_ACCELERATION = differenceOfSpeedInKmPerHour / differenceOfTimeInSecond;

//        Toast.makeText(this, "Difference in seconds: " + differenceOfTimeInSecond, Toast.LENGTH_LONG).show();


//        Toast.makeText(this, "Provider: " + location.getProvider()
//                + ", \nSpeed: " + CURRENT_SPEED + "km/h"
//                + ", \nAcc: " + CURRENT_ACCELERATION + "km/h/s", Toast.LENGTH_LONG).show();

        updateViews();
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

    /**
     * Called when there is a new sensor event.  Note that "on changed"
     * is somewhat of a misnomer, as this will also be called if we have a
     * new reading from a sensor with the exact same sensor values (but a
     * newer timestamp).
     * <p>
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        accX = event.values[SensorManager.DATA_X];
        accY = event.values[SensorManager.DATA_Y];
        accZ = event.values[SensorManager.DATA_Z];
        long now = System.currentTimeMillis();
        deltaT = now - lastEvent;
        lastEvent = now;

        // calculate velocity in 3D by using 3D acceleration
        Vx = Vx + deltaT * accX;
        Vy = Vy + deltaT * accX;
        Vz = Vz + deltaT * accX;

    }

    /**
     * Called when the accuracy of the registered sensor has changed.  Unlike
     * onSensorChanged(), this is only called when this accuracy value changes.
     * <p>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (BuildConfig.DEBUG) Log.d("MainActivity", "Acc sensor accuracy changed: ");
    }

    // TODO: Implement 'fetch data from sensor'
    // @doc: https://stackoverflow.com/questions/20398898/how-to-get-speed-in-android-app-using-location-or-accelerometer-or-some-other-wa
}
