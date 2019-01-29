package de.tu_darmstadt.epool.pfoertneradmin;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class pictureUpload extends AppCompatActivity {
    String TAG = "pictureUpload";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_upload);
    }

    public void getPicture(View view){
        ActivityCompat.requestPermissions(pictureUpload.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        // nur als test anzeige
//        ImageView imagetest = (ImageView) findViewById(R.id.imageViewtest);
        CircleImageView imagetest2 = (CircleImageView) findViewById(R.id.profile_image);

        if (reqCode == 1) {
            try {
                if(data != null){

                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                imagetest.setImageBitmap(selectedImage);
                imagetest2.setImageBitmap(selectedImage);

                //TODO: send picture to server
                sendFile(data.getData());

                Log.d(TAG, "pic loaded");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(pictureUpload.this, "Something went wrong, with choosing a picture!", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(pictureUpload.this, "You haven't picked Image!",Toast.LENGTH_LONG).show();
        }
    }

    private void sendFile(Uri fileUri){
        final PfoertnerApplication app = PfoertnerApplication.get(this);
//        File file = null;

//        try {
//            final InputStream imageStream = getContentResolver().openInputStream(fileUri);
//            OutputStream output = new FileOutputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


        Log.d(TAG, fileUri.toString());
        Log.d(TAG, fileUri.getPath());
        Log.d(TAG, getPath(fileUri));
//        File file = new File("/storage/emulated/0/Download/4EtP0n3.jpg");
        File file = new File(getPath(fileUri));

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", file.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);

        // finally, execute the request
        Call<ResponseBody> call = app.getService().upload(description, body, app.getDevice().id);
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
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

}