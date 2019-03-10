package de.tu_darmstadt.epool.pfoertner.common.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";

    private static BiConsumer<SplashScreenActivity, Consumer<Void>> s_work;
    private static View s_view;
    private static int s_orientation;

    public static void run(final Activity parent, final View view, final int screenOrientation, final BiConsumer<SplashScreenActivity, Consumer<Void>> work) {
        Log.d(TAG, "Initializing splash screen");

        if (s_work != null) {
            throw new RuntimeException("There can only be a single active splash screen.");
        }

        s_view = view;
        s_work = work;
        s_orientation = screenOrientation;

        final Intent intent = new Intent(parent, SplashScreenActivity.class);
        parent.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Splash screen activity is being created.");

        super.onCreate(savedInstanceState);
        if (s_view.getParent() != null) {
            ((ViewGroup) s_view.getParent()).removeView(s_view);
        }

        setRequestedOrientation(s_orientation);
        setContentView(s_view);

        s_work.accept(
            this,
            aVoid -> {
                Log.d(TAG, "Splash screen is being closed.");

                this.finish();
                s_work = null;
            }
        );
    }

    @Override
    public void onBackPressed() {
        // prevent the user from removing the splash screen from the activity stack. (pressing back button)
        // instead, move the entire app into the background
        moveTaskToBack(true);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Splash screen activity is closing.");

        super.onStop();
    }
}
