package de.tu_darmstadt.epool.pfoertneradmin.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.signature.ObjectKey;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class SpionFragment extends Fragment {
    private static final String TAG = "SpionFragment";

    ImageView spion;
    PfoertnerApplication app;
    PfoertnerService service;

    private Disposable spionDisposable;

    public SpionFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = PfoertnerApplication.get(getContext());
        service = app.getService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.fragment_spion, container, false);

        spion = (ImageView) mainView.findViewById(R.id.imageViewSpion);

        if(spion == null){
            Log.d(TAG,"The spion ImageView could not be found");
        } else{
            Log.d(TAG, "The spion ImageView was found");
        }

        app
                .getRepo()
                .getOfficeRepo()
                .getOffice(app.getOffice().getId())
                .observe(this, office -> {
                    Log.d(TAG, "(office.getSpionPicture, office.getSpionPictureMD5) = (" + office.getSpionPicture() + ", " + office.getSpionPictureMD5() + ")");

                    if (office != null) {
                        if (office.getSpionPicture() != null) {

                            Log.d(TAG, "Starting to build the URL for the getSpion request");

                            GlideUrl glideUrl = new GlideUrl(office.getSpionPicture(),
                                    new LazyHeaders.Builder()
                                            .addHeader("Authorization", app.getAuthentication().id)
                                            .build()
                            );

                            Log.d(TAG, "Finished to build  the URL for the getSpion request");

                            Glide
                                    .with(this)
                                    .load(glideUrl)
                                    .placeholder(ContextCompat.getDrawable(getContext(), R.drawable.ic_user_secret_solid))
                                    .signature(new ObjectKey(office.getSpionPictureMD5() == null ? "null" : office.getSpionPictureMD5()))
                                    .into(spion);

                            Log.d(TAG, "The gilde request is done");
                        }

                        else {
                            Log.d(TAG, "There is no spion picture to upload.");
                        }
                    }
                });

        mainView.findViewById(R.id.button).setOnClickListener(
                this::getNewSpionPicture
        );

        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (spionDisposable != null) {
            spionDisposable.dispose();
        }
    }

    public void initSpion() throws IOException {
        final ResponseBody response;

        response = service
                .initSpionPhoto(app.getAuthentication().id,app.getOffice().getId())
                .execute()
                .body();
        Log.d(TAG, "DIE RESPONSE VOM SERVER: " + response.toString());
    }

    public void getNewSpionPicture(View view)  {
        Log.d(TAG, "ICHWERDEAUSGEFÜHRT");
        spionDisposable = Completable.fromAction(
                this::initSpion
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {Log.d(TAG, "Successfully completed asking for spion picture.");

                            Toast toast = Toast.makeText(getActivity(), "Spion picture is being taken.",
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 200);
                            toast.show();
                        },
                        throwable -> Log.e(TAG, "Failed asking for spion picture.", throwable)
                );
    }
}
