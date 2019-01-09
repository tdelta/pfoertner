package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import android.content.SharedPreferences;

import java.io.IOException;

import retrofit2.Call;

public class Person {

    public final int id;
    public final String lastName;
    public final String firstName;

    public Person(int id, String lastName, String firstName){
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public static Person loadPerson(
            final PersonCreationData creationData,
            final SharedPreferences registrationInfo,
            final PfoertnerService service,
            final Authentication auth
    ) {
        Person person;
        int deviceID = registrationInfo.getInt("UserId", -1);

        if (registrationInfo.contains("PersonId") /*person already registered*/) {
            person = new Person(
                    registrationInfo.getInt("PersonId", -1),
                    registrationInfo.getString("PersonFirstName", ""),
                    registrationInfo.getString("PersonLastName", "")
            );
        }

        else {
            // Create person
            try {
                final Call<Person> personCall = service.createPerson(auth.id, deviceID ,creationData);
                person = personCall.execute().body();

                if (person != null) {
                    final SharedPreferences.Editor e = registrationInfo.edit();

                    e.putInt("PersonId", person.id);
                    e.putString("PersonFirstName", person.firstName);
                    e.putString("PersonLastName", person.lastName);

                    e.apply();
                }
            }

            catch (final IOException e) {
                e.printStackTrace();
                person = null;
                // the if below will handle further steps
            }
        }

        if (person == null) {
            throw new RuntimeException("Could not register a new person at the server.");
        }

        return person;
    }
}
