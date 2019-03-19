package de.tu_darmstadt.epool.pfoertner.common.spion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;


import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
//import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.R;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Service for taking pictures secretly (e. g. from the background)
 */
public class Spion extends HiddenCameraService {

    private static final String TAG = "Spion";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Called by HiddenCameraService after a picture gets taken
     * @param imageFile The resulting picture
     */
    @Override
    public void onImageCapture(@NonNull File imageFile) {

        Log.d("SpionNew", "We are in onImageCaptured and the takePhoto is about to get send");


        // Do something with the image...
        Completable.fromAction(
                () -> sendSpion(imageFile)
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d(TAG, "Successfully completed uploading spion picture."),
                        throwable -> Log.e(TAG, "Failed to upload spion picture.", throwable)
                );
        stopSelf();
    }


    /**
     * Sends spion picture to the server
     * @param file picture file
     * @throws IOException
     */
    private void sendSpion(File file) throws IOException {


        PfoertnerApplication app = PfoertnerApplication.get(getApplicationContext());
        PfoertnerService service = app.getService();


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
                .uploadSpion(app.getAuthentication().id,description, body, app.getOffice().getId())
                .execute()
                .body();

        if (response == null) {
            throw new RuntimeException("Uploading spion picture failed!");
        }
    }


    /**
     * Called when an intent to this service is started. initialize camera and require permissions
     * @param intent The intent to start the service
     * @param flags The flags of the intent
     * @param startId The id of the intent
     * @return Service execution mode START_NOT_STICKY, if the service is killed it will not be restarted.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "We are in onStartCommand");


        Log.d(TAG, "We ask for pressmisions now");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {


            Log.d(TAG, "We have all needed permessions");

            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                CameraConfig cameraConfig = new CameraConfig()
                        .getBuilder(this)
                        .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                        .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                        //.setCameraFocus(CameraFocus.AUTO)
                        .build();

                Log.d(TAG, "Camera is about to start");

                startCamera(cameraConfig);

                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(DemoCamService.this,
                        //        "Capturing image.", Toast.LENGTH_SHORT).show();


                        Log.d(TAG, "We are about to start the takePicture()");
                        takePicture();
                    }
                }, 2000L);
            } else {

                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {

            Log.d(TAG,"We didnt have all the needed permissions");

            //TODO Ask your parent activity for providing runtime permission
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
    }


    /**
     * error handler if there are errors while taking a picture, called by HiddenCameraService
     * @param errorCode The type of error that occurred
     */
    @Override
    public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {

        Log.d(TAG, "Something went wrong");

        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(this, R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
