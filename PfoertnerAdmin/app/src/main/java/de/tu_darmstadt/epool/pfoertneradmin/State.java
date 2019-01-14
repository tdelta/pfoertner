package de.tu_darmstadt.epool.pfoertneradmin;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static de.tu_darmstadt.epool.pfoertner.common.Config.SERVER_ADDR;

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
        return PfoertnerService.makeService();
    }
}
