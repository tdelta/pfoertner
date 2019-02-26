package de.tu_darmstadt.epool.pfoertneradmin;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class SpionActivity extends AppCompatActivity {
    ImageView spion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spion);
        spion = (ImageView) findViewById(R.id.imageViewSpion);

        //get current spion picture
    }

    public void getNewSpionPicture(View view){
        //get current spion picture
    }
}
