package de.tu_darmstadt.epool.pfoertner.common.architecture.webapi;

import com.google.gson.GsonBuilder;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import io.reactivex.Single;
import io.reactivex.subjects.SingleSubject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

import static de.tu_darmstadt.epool.pfoertner.common.Config.SERVER_ADDR;

public interface PfoertnerApi {
    static PfoertnerApi makeApi() {
        // Debug logging
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor).build();

        final Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(SERVER_ADDR)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm")
                                .create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        final PfoertnerApi api = retrofit.create(PfoertnerApi.class);

        return api;
    }

    @GET("/officemembers/{id}")
    Single<MemberEntity> getMember(@Header("Authorization") String authToken, @Path("id") int memberId);

    @GET("/offices/{id}")
    Single<OfficeEntity> getOffice(@Header("Authorization") String authToken, @Path("id") int officeId);

    @PATCH("/offices/{id}")
    Single<OfficeEntity> patchOffice(@Header("Authorization") String authToken,@Path("id") int id, @Body OfficeEntity office);
}
