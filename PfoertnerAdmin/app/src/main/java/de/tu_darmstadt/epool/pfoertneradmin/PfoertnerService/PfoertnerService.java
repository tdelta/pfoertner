package de.tu_darmstadt.epool.pfoertneradmin.PfoertnerService;

import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.Call;

public interface PfoertnerService {
  @POST("/api/devices")
  Call<User> createUser(@Body final LoginCredentials credentials);

  @POST("/api/Users/login")
  Call<Authentication> login(@Body final LoginCredentials credentials);

  @POST("/api/offices")
  Call<Office> createOffice(@Header("Authorization") String authToken);
}
  
