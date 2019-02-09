package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

// Only fields with the Expose annotation will be sent to the server
// Other fields will only be persisted in local memory

public class PersonCreationData {

    @Expose public final String lastName;
    @Expose public final String firstName;

    public PersonCreationData(String lastName, String firstName){
        this.lastName = lastName;
        this.firstName = firstName;
    }
}
