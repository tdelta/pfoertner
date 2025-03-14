package de.tu_darmstadt.epool.pfoertneradmin.fragments;

import android.Manifest;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.function.Consumer;

import de.hdodenhof.circleimageview.CircleImageView;
import de.tu_darmstadt.epool.pfoertneradmin.AdminApplication;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.MemberProfileViewModel;
import io.reactivex.disposables.CompositeDisposable;

public class PictureUploadFragment extends Fragment {
    private static final String TAG = "PictureUploadFragment";
    private MemberProfileViewModel viewModel;

    private CompositeDisposable disposables;

    public PictureUploadFragment() {

    }

    /**
     * Is called when activity gets created
     *
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (disposables != null) {
            disposables.dispose();
        }
        disposables = new CompositeDisposable();

        final AdminApplication app = AdminApplication.get(getContext());

        viewModel = ViewModelProviders.of(this).get(MemberProfileViewModel.class);
        viewModel.init(app.getMemberId());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.fragment_picture_upload, container, false);

        viewModel.getMember().observe(this, member -> {
            // TODO
            Log.d(TAG, "Observed change!");

            if (member != null) {
                final TextView firstNameTextView = mainView.findViewById(R.id.firstNameView);
                final TextView lastNameTextView = mainView.findViewById(R.id.lastNameView);

                Glide
                        .with(this)
                        .load(member.getPicture())
                        .placeholder(ContextCompat.getDrawable(getContext(), R.drawable.ic_account_circle_grey_500dp))
                        .signature(new ObjectKey(member.getPictureMD5() == null ? "null" : member.getPictureMD5()))
                        .into((CircleImageView) mainView.findViewById(R.id.profile_image));

                firstNameTextView.setText(member.getFirstName());
                lastNameTextView.setText(member.getLastName());
            }

            else {
                Log.d(TAG, "There is no member set, or it was destroyed on an update.");
            }
        });

        mainView.findViewById(R.id.profile_image).setOnClickListener(
                this::getPicture
        );

        mainView.findViewById(R.id.firstNameLayout).setOnClickListener(
                this::setFirstName
        );

        mainView.findViewById(R.id.lastNameLayout).setOnClickListener(
                this::setLastName
        );

        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destroyed");

        disposables.dispose();
    }

    /**
     * Resulthandler for selecting a picture to be uploaded
     * @param reqCode Request Code
     * @param resultCode Result Code
     * @param data Data in the Intent
     */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == 1) {
            if(data != null) {

                final Uri pictureUri = data.getData();

                if (pictureUri != null) {
                    final AdminApplication app = AdminApplication.get(getContext());

                    app
                            .getOffice()
                            .getMemberById(
                                    app.getMemberId()
                            )
                            .ifPresent(
                                    member -> member.setPicture(
                                            app.getService(),
                                            app.getAuthentication(),
                                            getContext().getContentResolver().getType(pictureUri),
                                            getPath(pictureUri)
                                    )
                            );
                }

                else {
                    Log.d(TAG, "Could not send a new image. No image data was returnded by the intent.");
                }
            }

            else {
                Log.d(TAG, "Could not send a new image. No image data was returnded by the intent.");
            }
        }else {
            Toast.makeText(getContext(), "You haven't picked an image!",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * get the path to the picture you want to upload
     * @param uri uri containing path to picture
     * @return path to picture as String
     */
    private String getPath(final Uri uri) {
        final String[] projection = { MediaStore.Images.Media.DATA };
        final Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return null;
        }

        final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        final String s = cursor.getString(column_index);
        cursor.close();

        return s;
    }

    /**
     * Set the first name for the owner of the admin app
     * @param btn view context for layout
     */
    public void setFirstName(final View btn) {
        final TextView firstNameTextView = btn.findViewById(R.id.firstNameView);

        promptForString(
                "Please enter a new first name.",
                firstNameTextView.getText().toString(),
                newFirstName -> {
                    final AdminApplication app = AdminApplication.get(getActivity());

                    disposables.add(
                            app
                                    .getRepo()
                                    .getMemberRepo()
                                    .setFirstName(app.getMemberId(), newFirstName)
                                    .subscribe(
                                            () -> Log.d(TAG, "Successfully set new first name to " + newFirstName),
                                            throwable -> Log.e(TAG, "Failed to set new first name.", throwable)
                                    )
                    );
                }
        );
    }

    /**
     * Set the last name for the owner of the admin app
     * @param btn view context for layout
     */
    public void setLastName(final View btn) {
        final TextView lastNameTextView = btn.findViewById(R.id.lastNameView);

        promptForString(
                "Please enter a new last name.",
                lastNameTextView.getText().toString(),
                newLastName -> {
                    final AdminApplication app = AdminApplication.get(getContext());

                    disposables.add(
                            app
                                    .getRepo()
                                    .getMemberRepo()
                                    .setLastName(app.getMemberId(), newLastName)
                                    .subscribe(
                                            () -> Log.d(TAG, "Successfully set new last name to " + newLastName),
                                            throwable -> Log.e(TAG, "Failed to set new first name.", throwable)
                                    )
                    );
                }
        );
    }

    /**
     *
     * This method displays an input dialog for strings (used in
     * set firstName() and setLastName()) and insert the input into
     * a consumer.
     *
     *
     * @param message which will be displayed above the input field
     * @param initialText initially displayed message
     * @param onPositiveButton consumer which receives the input
     */
    private void promptForString(final String message, final String initialText, final Consumer<String> onPositiveButton) {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());

        alertBuilder.setMessage(message);

        final EditText input = new EditText(getContext());
        alertBuilder.setView(input);
        input.setText(initialText);

        alertBuilder.setPositiveButton("Submit", (dialog, i) -> {
            final String newText = input.getText().toString();

            if (!newText.isEmpty()) {
                onPositiveButton.accept(input.getText().toString());
            }
        });

        final AlertDialog alert = alertBuilder.create();
        alert.show();
    }


    /**
     * calls activity to choose which photo you like to upload
     * @param view context for layout
     */
    public void getPicture(View view){
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }
}
