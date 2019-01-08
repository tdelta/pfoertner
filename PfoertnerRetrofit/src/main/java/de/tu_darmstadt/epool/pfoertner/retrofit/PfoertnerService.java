package de.tu_darmstadt.epool.pfoertner.retrofit;

import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.Call;

public interface PfoertnerService {
  @POST("/api/devices")
  Call<User> createUser(@Body final Password password);

  @POST("/api/devices/login")
  Call<Authentication> login(@Body final LoginCredentials credentials);

  @POST("/api/offices")
  Call<Office> createOffice(@Header("Authorization") String authToken);

  @PUT("/api/offices/{id}/join")
  Call<Office> joinOffice(@Header("Authorization") String authToken, @Path("id") int id, @Body OfficeJoinCode joinCode);
}
  
