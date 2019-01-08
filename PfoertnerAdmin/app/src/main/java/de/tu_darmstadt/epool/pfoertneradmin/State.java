package de.tu_darmstadt.epool.pfoertneradmin;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class State {

    public Authentication authtoken;
    public final PfoertnerService service = createService();

    private static State single_instance = null;

    private State(){}

    public static State getInstance(){
        if(single_instance == null){
            single_instance = new State();
        }
        return single_instance;
    }

    private static PfoertnerService createService(){
        // Base url of our deployment server
        String API_BASE_URL = "http://deh.duckdns.org:3000/api/";

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        Retrofit retrofit = builder.client(httpClient.build()).build();

        return retrofit.create(PfoertnerService.class);
    }
}
