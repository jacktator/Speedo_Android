package sk8.tech.speedo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView speedLimitTextView;
    private TextView currentSpeedTextView;
    private TextView accelerationTextView;

    private LinearLayout contentFrameLayout;

    private float CONST_SPEED_LIMIT = 20;
    private float CONST_PREDICTION_SECOND = 6;

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
                    mTextMessage.setText(R.string.title_home);

                    contentFrameLayout.setBackgroundColor(Color.GREEN);

                    return true;
                case R.id.navigation_three:
                    mTextMessage.setText(R.string.title_dashboard);

                    contentFrameLayout.setBackgroundColor(Color.WHITE);

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

    // TODO: Implement 'fetch data from sensor'
    // @doc: https://stackoverflow.com/questions/20398898/how-to-get-speed-in-android-app-using-location-or-accelerometer-or-some-other-wa
}
