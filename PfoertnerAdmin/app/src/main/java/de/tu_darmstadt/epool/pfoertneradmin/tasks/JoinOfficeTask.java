package de.tu_darmstadt.epool.pfoertneradmin.tasks;

import android.app.Service;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.OfficeJoinCode;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Person;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PersonCreationData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertneradmin.State;

public class JoinOfficeTask extends AsyncTask<Void, Void, Void> {

    private final State state = State.getInstance();


    private PfoertnerService service;
    private SharedPreferences settings;

    private Authentication authtoken;
    private Office office;

    private String firstName;
    private String lastName;

    public JoinOfficeTask(
            PfoertnerService service,
            SharedPreferences settings,
            Authentication authtoken,
            Office office,
            String firstName,
            String lastName
    ){
        this.service = service;
        this.settings = settings;
        this.authtoken = authtoken;
        this.office = office;

        this.firstName = firstName;
        this.lastName = lastName;
    }


    @Override
    public Void doInBackground(final Void ... parameters){


        try {
            // Join Office
            service.joinOffice(authtoken.id ,office.id,new OfficeJoinCode(office.userJoinCode)).execute();
            // Create Person
            Person.loadPerson(new PersonCreationData(lastName,firstName), settings, service, authtoken);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
