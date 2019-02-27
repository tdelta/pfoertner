package de.tu_darmstadt.epool.pfoertneradmin;

import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class SpionActivity extends AppCompatActivity {
    String TAG = "spion";
    ImageView spion;
    PfoertnerApplication app;
    PfoertnerService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spion);
        app = PfoertnerApplication.get(this);
        service = app.getService();
        spion = (ImageView) findViewById(R.id.imageViewSpion);

        Glide
                .with(SpionActivity.this)
                .load(office.getPicture())
                .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_grey_500dp))
                .signature(new ObjectKey(office.getPictureMD5() == null ? "null" : office.getPictureMD5()))
                .into(spion);
    }

    public void getNewSpionPicture(View view)  {
        Completable.fromAction(
                () -> initSpion()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d(TAG, "Successfully completed asking for spion picture."),
                        throwable -> Log.e(TAG, "Failed asking for spion picture.", throwable)
                );
    }

    public void initSpion() throws IOException{
        final ResponseBody response;

        response = service
                .initSpionPhoto(app.getOffice().getId())
                .execute()
                .body();
    }
}
