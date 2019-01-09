package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.Call;

import static de.tu_darmstadt.epool.pfoertner.common.Config.SERVER_ADDR;

public interface PfoertnerService {
  @POST("/api/devices")
  Call<User> createUser(@Body final Password password);

  @PUT("/api/devices/{id}")
  Call<Void> setFcmToken(@Header("Authorization") String authToken, @Path("id") int id, @Body FcmTokenCreationData fcmTokenCreationData);

  @POST("/api/devices/login")
  Call<Authentication> login(@Body final LoginCredentials credentials);

  @POST("/api/offices")
  Call<Office> createOffice(@Header("Authorization") String authToken);

  @GET("/api/offices")
  Call<Office> loadOffice(@Header("Authorization") String authToken);

  @PUT("/api/offices/{id}/join")
  Call<Void> joinOffice(@Header("Authorization") String authToken, @Path("id") int id, @Body OfficeJoinCode joinCode);

  @POST("/api/devices/{id}/person")
  Call<Person> createPerson(@Header("Authorization") String authToken, @Path("id") int deviceInt,@Body PersonCreationData personData);

  static PfoertnerService makeService() {
    // Debug logging
    final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(interceptor).build();

    final Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl(SERVER_ADDR)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    final PfoertnerService service = retrofit.create(PfoertnerService.class);

    return service;
  }
}
  
