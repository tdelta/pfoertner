package de.tu_darmstadt.epool.pfoertneradmin;

import android.media.Image;
import android.nfc.Tag;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.signature.ObjectKey;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class SpionActivity extends AppCompatActivity {
    String TAG = "spion";
    ImageView spion;
    PfoertnerApplication app;
    PfoertnerService service;

    private Disposable spionDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spion);
        app = PfoertnerApplication.get(this);
        service = app.getService();
        spion = (ImageView) findViewById(R.id.imageViewSpion);


        app
                .getRepo()
                .getOfficeRepo()
                .getOffice(app.getOffice().getId())
                .observe(this, office -> {

                    Log.d(TAG, "Vor dem Erstellen der URL");

                    Log.d(TAG, "(office.getSpionPicture, office.getSpionPictureMD5) = (" + office.getSpionPicture() + ", " + office.getSpionPictureMD5() + ")");

//                    GlideUrl glideUrl = new GlideUrl(office.getSpionPicture(),
//                            new LazyHeaders.Builder()
//                            .addHeader("Authorization", app.getAuthentication().id)
//                            .build()
//                            );

                    Log.d(TAG, "Nach dem Erstellen der URL");


                    if (office != null) {
                        if (office.getSpionPicture() != null) {
                            GlideUrl glideUrl = new GlideUrl(office.getSpionPicture(),
                                    new LazyHeaders.Builder()
                                            .addHeader("Authorization", app.getAuthentication().id)
                                            .build()
                            );

                            Glide
                                    .with(SpionActivity.this)
                                    .load(glideUrl)
                                    .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_grey_500dp))
                                    .signature(new ObjectKey(office.getSpionPictureMD5() == null ? "null" : office.getSpionPictureMD5()))
                                    .into(spion);
                        }

                        else {
                            Log.d(TAG, "There is no spion picture to upload.");
                        }
                    }
                });
    }

    public void getNewSpionPicture(View view)  {
        Log.d(TAG, "ICHWERDEAUSGEFÜHRT");
        spionDisposable = Completable.fromAction(
                this::initSpion
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
                .initSpionPhoto(app.getAuthentication().id,app.getOffice().getId())
                .execute()
                .body();
        Log.d(TAG, "DIE RESPONSE VOM SERVER: " + response.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (spionDisposable != null) {
            spionDisposable.dispose();
        }
    }
}
