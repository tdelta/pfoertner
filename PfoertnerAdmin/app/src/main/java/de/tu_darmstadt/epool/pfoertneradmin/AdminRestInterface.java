package de.tu_darmstadt.epool.pfoertneradmin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AdminRestInterface {

//    @GET("/users/{user}/repos")
//    Call<String> test(
//            @Path("user") String user
//    );

    @PUT("")
    Call<> createUser();

    @POST("")
    Call<> joinOffice();
}
