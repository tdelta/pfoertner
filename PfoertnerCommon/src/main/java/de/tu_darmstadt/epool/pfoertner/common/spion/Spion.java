package de.tu_darmstadt.epool.pfoertner.common.spion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.camera.core.internal.utils.ImageUtil;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.ByteString;

/**
 * Service for taking pictures secretly (e. g. from the background)
 */
public class Spion extends LifecycleService {

    private static final String TAG = "Spion";

    private ListenableFuture<ImageCapture> imageCaptureFuture;

    @Override
    public void onCreate() {
        super.onCreate();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // As soon as the CameraProvider is created, we use it to create an ImageCapture
        imageCaptureFuture = Futures.transform(cameraProviderFuture, cameraProvider -> {
            Log.d(TAG, "Camera provider object acquired");
            ImageCapture imageCapture = new ImageCapture.Builder()
                    .build();

            // Unbind use cases before rebinding
            cameraProvider.unbindAll();

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_FRONT_CAMERA, imageCapture);

            return imageCapture;
        }, ContextCompat.getMainExecutor(this));
    }

    @Nullable
    @Override
    public IBinder onBind(@NotNull Intent intent) {
        super.onBind(intent);
        return null;
    }


    /**
     * Called after a picture gets taken
     *
     * @param imageBytes The picture
     * @param format     The format of the picture bytes (e.g. jpeg, png, ...)
     */
    private void onImageCaptured(@NonNull ByteString imageBytes, String format) {

        Log.d(TAG, "We are in onImageCaptured and the takePhoto is about to get send");


        // Do something with the image...
        Disposable result = Completable.fromAction(
                        () -> sendSpion(imageBytes, format)
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
     *
     * @param bytes  image data
     * @param format image data format (e.g. jpeg, png, ...)
     * @throws IOException
     */
    private void sendSpion(ByteString bytes, String format) throws IOException {


        PfoertnerApplication app = PfoertnerApplication.get(getApplicationContext());
        PfoertnerService service = app.getService();


        final RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(format),
                        bytes
                );

        // MultipartBody.Part is used to send also the actual file name
        final MultipartBody.Part body =
                MultipartBody.Part.createFormData("spion", "spion", requestFile);

        // add another part within the multipart request
        final String descriptionString = "spion picture";
        final RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);

        // finally, execute the request
        final ResponseBody response;

        response = service
                .uploadSpion(app.getAuthentication().id, description, body, app.getOffice().getId())
                .execute()
                .body();

        if (response == null) {
            throw new RuntimeException("Uploading spion picture failed!");
        }
    }


    /**
     * Called when an intent to this service is started. initialize camera and require permissions
     *
     * @param intent  The intent to start the service
     * @param flags   The flags of the intent
     * @param startId The id of the intent
     * @return Service execution mode START_NOT_STICKY, if the service is killed it will not be restarted.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "We are in onStartCommand");

        Log.d(TAG, "We ask for permissions now");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            imageCaptureFuture.addListener(() -> {
                try {
                    Log.d(TAG, "ImageCapture object acquired");
                    ImageCapture imageCapture = imageCaptureFuture.get();
                    imageCapture.takePicture(
                            ContextCompat.getMainExecutor(this),
                            new ImageCapture.OnImageCapturedCallback() {
                                @Override
                                public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                                    Log.d(TAG, "Image captured successfully");
                                    if (imageProxy.getFormat() == ImageFormat.JPEG) {
                                        // Avoid converting JPG -> BMP -> JPG
                                        try {
                                            ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
                                            ByteString bytes = ByteString.of(buffer);
                                            onImageCaptured(bytes, "jpeg");
                                            return;
                                        }
                                        catch(Exception e){
                                            // If this goes wrong, we can still convert to BMP
                                            Log.w(TAG, "Failed to send image. Retrying", e);
                                        }
                                    }
                                    Bitmap bitmap = imageProxy.toBitmap();
                                    try (Buffer buffer = new Buffer()) {
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, buffer.outputStream());
                                        if (buffer.size() > Integer.MAX_VALUE)
                                            // We need to cast the image size to int so it can't be larger than 2GB, but this will never happen :-)
                                            throw new IllegalArgumentException("Image is larger than Integer.MAX_VALUE bytes (~2GB)");
                                        ByteString bytes = ByteString.read(buffer.inputStream(), (int) buffer.size());
                                        onImageCaptured(bytes, "jpeg");

                                    } catch (IOException | IllegalArgumentException e) {
                                        Log.e(TAG, "Error processing image", e);
                                    }
                                }

                                @Override
                                public void onError(@NonNull ImageCaptureException e) {
                                    Log.e(TAG, "Image capture failed", e);
                                }
                            });
                }
                catch (ExecutionException | InterruptedException e) {
                    // This means the ImageCapture object is not available, even though the
                    // Future is complete. This should never happen
                    Log.e(TAG,"ImageCapture Object not available",e);
                }
            }, ContextCompat.getMainExecutor(this));
        } else {

            Log.d(TAG, "We didnt have all the needed permissions");

            //TODO It would make more sense to prompt for permissions on the admin app
            //TODO and then trigger a camera permission request from there
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
        }

        return START_NOT_STICKY;
    }
}
