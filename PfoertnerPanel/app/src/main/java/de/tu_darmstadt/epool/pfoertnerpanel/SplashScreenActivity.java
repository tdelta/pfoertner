package de.tu_darmstadt.epool.pfoertnerpanel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SplashScreenActivity extends AppCompatActivity {
    private static BiConsumer<Context, Consumer<Void>> s_work;

    public static void run(final Activity parent, final BiConsumer<Context, Consumer<Void>> work) {
        if (s_work != null) {
            throw new RuntimeException("There can only be a single active splash screen.");
        }

        final Intent intent = new Intent(parent, SplashScreenActivity.class);
        parent.startActivity(intent);

        s_work = work;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        s_work.accept(
            this,
            aVoid -> {
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
}
