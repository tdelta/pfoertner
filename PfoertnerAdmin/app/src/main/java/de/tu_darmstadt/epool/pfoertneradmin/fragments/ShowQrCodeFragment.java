package de.tu_darmstadt.epool.pfoertneradmin.fragments;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.function.Consumer;

import de.hdodenhof.circleimageview.CircleImageView;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertneradmin.AdminApplication;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.MemberProfileViewModel;
import io.reactivex.disposables.CompositeDisposable;

public class ShowQrCodeFragment extends Fragment {
    private static final String TAG = "ShowQrCodeFragment";

    public ShowQrCodeFragment() {

    }

    /**
     * called on activity creation
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Called on creation
     * initialize the fragment
     * @param inflater needed to create views in the fragment
     * @param container parent view of the fragment
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     * @return view for layout context
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.fragment_show_qrcode, container, false);

        showQRCode(mainView);

        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Get and display the QR-Code in the Admin App
     * @param mainView view context for layout
     */
    protected void showQRCode(final View mainView){
        final PfoertnerApplication app = PfoertnerApplication.get(getContext());

        app
                .getRepo()
                .getOfficeRepo()
                .getOffice(app.getOffice().getId())
                .observe(this, office -> {
                    if (office != null) {
                        final ImageView qrCodeView = mainView.findViewById(R.id.qrCodeView);
                        final QRCode qrCode = new QRCode(
                                new QRCodeData(office).serialize()
                        );

                        qrCodeView.setImageDrawable(qrCode);
                    }

                    else {
                        Log.d(TAG, "Cant show QR code yet, since there is no office set right now.");
                    }
                });
    }
}
