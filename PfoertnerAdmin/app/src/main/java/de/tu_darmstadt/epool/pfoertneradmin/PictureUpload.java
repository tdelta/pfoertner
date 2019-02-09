package de.tu_darmstadt.epool.pfoertneradmin;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.function.Consumer;

import de.hdodenhof.circleimageview.CircleImageView;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;

public class PictureUpload extends AppCompatActivity {
    private static final String TAG = "PictureUpload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_upload);

        final AdminApplication app = AdminApplication.get(this);

        app.getOffice().getMemberById(
                app.getMemberId()
        ).ifPresent(
                member -> {
                    final TextView firstNameTextView = this.findViewById(R.id.firstNameView);
                    final TextView lastNameTextView = this.findViewById(R.id.lastNameView);
                    final CircleImageView imagetest = (CircleImageView) findViewById(R.id.profile_image);

                    firstNameTextView.setText(member.getFirstName());
                    lastNameTextView.setText(member.getLastName());

                    member
                            .getPicture(app.getFilesDir())
                            .ifPresent(imagetest::setImageBitmap);

                    member.addObserver(
                            new MemberObserver() {
                                @Override
                                public void onFirstNameChanged(String newFirstName) {
                                    firstNameTextView.setText(newFirstName);
                                }

                                @Override
                                public void onLastNameChanged(String newLastName) {
                                    lastNameTextView.setText(newLastName);
                                }

                                @Override
                                public void onPictureChanged() {
                                    member.getPicture(app.getFilesDir())
                                            .ifPresent(imagetest::setImageBitmap);
                                }
                            }
                    );
                }
        );
    }

    public void setFirstName(final View btn) {
        final TextView firstNameTextView = this.findViewById(R.id.firstNameView);

        promptForString(
                "Please enter a new first name.",
                firstNameTextView.getText().toString(),
                newFirstName -> {
                    final AdminApplication app = AdminApplication.get(this);

                    app.getOffice().getMemberById(
                            app.getMemberId()
                    ).ifPresent(
                            member -> member.setFirstName(
                                    app.getService(),
                                    app.getAuthentication(),
                                    newFirstName
                            )
                    );
                }
        );
    }

    public void setLastName(final View btn) {
        final TextView lastNameTextView = this.findViewById(R.id.lastNameView);

        promptForString(
                "Please enter a new last name.",
               lastNameTextView.getText().toString(),
               newLastName -> {
                   final AdminApplication app = AdminApplication.get(this);

                   app.getOffice().getMemberById(
                           app.getMemberId()
                   ).ifPresent(
                           member -> member.setLastName(
                                   app.getService(),
                                   app.getAuthentication(),
                                   newLastName
                           )
                   );
               }
        );
    }

    private void promptForString(final String message, final String initialText, final Consumer<String> onPositiveButton) {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setMessage(message);

        final EditText input = new EditText(this);
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
        ActivityCompat.requestPermissions(PictureUpload.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == 1) {
            if(data != null) {

                final Uri pictureUri = data.getData();

                if (pictureUri != null) {
                    final AdminApplication app = AdminApplication.get(this);

                    app
                            .getOffice()
                            .getMemberById(
                                    app.getMemberId()
                            )
                            .ifPresent(
                                    member -> member.setPicture(
                                            app.getService(),
                                            app.getAuthentication(),
                                            getContentResolver().getType(pictureUri),
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
            Toast.makeText(PictureUpload.this, "You haven't picked an image!",Toast.LENGTH_LONG).show();
        }
    }

    public String getPath(Uri uri)
    {
        final String[] projection = { MediaStore.Images.Media.DATA };
        final Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return null;
        }

        final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        final String s = cursor.getString(column_index);
        cursor.close();

        return s;
    }

}