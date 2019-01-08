package de.tu_darmstadt.epool.pfoertneradmin;

import de.tu_darmstadt.epool.pfoertner.retrofit.Authentication;

public class State {

    public Authentication authtoken;

    private static State single_instance = null;

    private State(){}

    public static State getInstance(){
        if(single_instance == null){
            single_instance = new State();
        }
        return single_instance;
    }
}
