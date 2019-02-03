package de.tu_darmstadt.epool.pfoertneradmin;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Consumer;

import de.hdodenhof.circleimageview.CircleImageView;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PictureUpload extends AppCompatActivity {
    private static final String TAG = "PictureUpload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_upload);

        final AdminApplication app = AdminApplication.get(this);

        final CircleImageView imagetest = (CircleImageView) findViewById(R.id.profile_image);

        //TODO: warning hardcode personid
        Log.d(TAG, "MemberID: " + app.getMemberId());
        Call<ResponseBody> call = app.getService().downloadPicture(app.getMemberId());

        call.enqueue(new Callback<ResponseBody>() {
                         @Override
                         public void onResponse(Call<ResponseBody> call,
                                                Response<ResponseBody> response) {

                             if(response.code() == 200) {
                                 InputStream input = response.body().byteStream();
                                 Bitmap selectedImage2 = BitmapFactory.decodeStream(input);
                                 imagetest.setImageBitmap(selectedImage2);
                             }
                         }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                             Log.d(TAG, "picturedownload failed");
            }
        });


        app.getOffice().getMemberById(
                app.getMemberId()
        ).ifPresent(
                member -> {
                    final TextView firstNameTextView = this.findViewById(R.id.firstNameView);
                    final TextView lastNameTextView = this.findViewById(R.id.lastNameView);

                    firstNameTextView.setText(member.getFirstName());
                    lastNameTextView.setText(member.getLastName());

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
                            }
                    );
                }
        );
    }

    public void setFirstName(final View btn) {
        promptForString(
                "Please enter a new first name.",
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
        promptForString(
                "Please enter a new last name.",
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

    private void promptForString(final String message, final Consumer<String> onPositiveButton) {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setMessage(message);

        final EditText input = new EditText(this);

        alertBuilder.setView(input);
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

        final CircleImageView imagetest = (CircleImageView) findViewById(R.id.profile_image);

        if (reqCode == 1) {
            try {
                if(data != null){

                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imagetest.setImageBitmap(selectedImage);

                //TODO: send picture to server
                sendFile(data.getData());

                Log.d(TAG, "pic loaded");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(PictureUpload.this, "Something went wrong, with choosing a picture!", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(PictureUpload.this, "You haven't picked Image!",Toast.LENGTH_LONG).show();
        }
    }

    private void sendFile(Uri fileUri){
        final AdminApplication app = AdminApplication.get(this);

        final File file = new File(getPath(fileUri));
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        final MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", file.getName(), requestFile);

        // add another part within the multipart request
        final String descriptionString = "hello, this is description speaking";
        final RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);

        // finally, execute the request
        //TODO: warning hardcode personid
        final Call<ResponseBody> call = app.getService().uploadPicture(description, body, app.getMemberId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
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