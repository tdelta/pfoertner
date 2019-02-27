package de.tu_darmstadt.epool.pfoertner.common.spion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class Spion {
    private Camera camera;
    private static final String TAG = "Spion";
    private PfoertnerApplication app;
    private PfoertnerService service;

    public Spion(Context context) {
        app = PfoertnerApplication.get(context);
        service = app.getService();
        camera = checkDeviceCamera();

        Log.d(TAG, "Cameranumber" + camera.getNumberOfCameras());
    }

    public void takePhoto(){

        try {
            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.startPreview();
            camera.takePicture(null, null, pictureCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Camera checkDeviceCamera(){
        Camera mCamera = null;
        try {
            mCamera = Camera.open(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    @SuppressWarnings("CheckResult")
    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Log.d(TAG, "Transferring bitmap...");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            if(bitmap==null){
//                Toast.makeText(MainActivity.this, "Captured image is empty", Toast.LENGTH_LONG).show();
//                return;
//            }
//            capturedImageHolder.setImageBitmap(bitmap);
            Log.d(TAG, bitmap + "");
            camera.stopPreview();
            camera.release();
            Log.d(TAG, "Transferred bitmap!");

            Completable.fromAction(
                    () -> saveSpion(data)
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> Log.d(TAG, "Successfully completed uploading spion picture."),
                            throwable -> Log.e(TAG, "Failed to upload spion picture.", throwable)
                    );
        }
    };

    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

    private void saveSpion(byte[] data) throws IOException {
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "spion.jpg");
        Log.d(TAG, Environment.getExternalStorageDirectory() + File.separator + "spion");
        Log.d(TAG, f.getPath());
        FileOutputStream fos = null;
        f.createNewFile();
        fos = new FileOutputStream(f);
        fos.write(data);
        sendSpion(f);
    }

    private void sendSpion(File file) throws IOException {
        final RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(FilenameUtils.getExtension(file.getPath())),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        final MultipartBody.Part body =
                MultipartBody.Part.createFormData("spion", file.getName(), requestFile);

        // add another part within the multipart request
        final String descriptionString = "spion picture";
        final RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);

        // finally, execute the request
        final ResponseBody response;

        response = service
                .uploadSpion(description, body, app.getOffice().getId())
                .execute()
                .body();

        if (response == null) {
            throw new RuntimeException("Uploading spion picture failed!");
        }
    }
}
