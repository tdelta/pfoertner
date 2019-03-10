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


    public void getPicture(View view){
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }
}
