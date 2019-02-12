package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.GsonBuilder;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

import static de.tu_darmstadt.epool.pfoertner.common.Config.SERVER_ADDR;

public interface PfoertnerService {
  @POST("/devices")
  Call<User> createUser(@Body final Password password);

  @PATCH("/devices/{id}/fcmToken")
  Call<Void> setFcmToken(@Header("Authorization") String authToken, @Path("id") int id, @Body FcmTokenCreationData fcmTokenCreationData);

  @POST("/devices/{id}/authToken")
  Call<Authentication> login(@Path("id") int deviceId, @Body final LoginCredentials credentials);

  @POST("/offices")
  Call<OfficeData> createOffice(@Header("Authorization") String authToken);

  @GET("/offices/{id}")
  Call<OfficeData> loadOffice(@Header("Authorization") String authToken, @Path("id") int officeId);

  @POST("/offices/{id}/members")
  Call<MemberData> joinOffice(@Header("Authorization") String authToken, @Path("id") int id, @Body OfficeJoinData data);

  @GET("/offices/{id}/members")
  Call<MemberData[]> getOfficeMembers(@Header("Authorization") String authToken, @Path("id") int id);

  @PATCH("/offices/{id}")
  Call<OfficeData> updateOfficeData(@Header("Authorization") String authToken,@Path("id") int id,@Body OfficeData office);

  @GET("/officemembers/{id}")
  Call<MemberData> loadMember(@Header("Authorization") String authToken, @Path("id") int memberId);

  @PATCH("/officemembers/{id}")
  Call<MemberData> updateMember(@Header("Authorization") String authToken, @Path("id") int id, @Body MemberData member);

  @Multipart
  @PATCH("/officemembers/{id}/picture")
  Call<ResponseBody> uploadPicture(@Part("description") RequestBody description, @Part MultipartBody.Part file, @Path("id") int id);

  @GET("/officemembers/{id}/picture")
  @Streaming
  Call<ResponseBody> downloadPicture(@Path("id") int id);

  @PATCH("officemembers/{id}/calendar")
  Call<ResponseBody> createdCalendar(@Header("Authorization") String authToken, @Path("id") int id);

  @POST("officemembers/{id}/appointment")//TODO:
  Call<ResponseBody> createNewAppointment(@Path("id") int id);

  //@POST("/api/devices/{id}/person")
  //Call<MemberData> createPerson(@Header("Authorization") String authToken, @Path("id") int deviceInt,@Body PersonCreationData personData);

  static PfoertnerService makeService() {
    // Debug logging
    final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(interceptor).build();

    final Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl(SERVER_ADDR)
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
            .build();

    final PfoertnerService service = retrofit.create(PfoertnerService.class);

    return service;
  }
}
  
