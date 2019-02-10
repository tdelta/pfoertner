package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

// Only fields with the Expose annotation will be sent to the server
// Other fields will only be persisted in local memory

public class LoginCredentials {
  @Expose public final String password;

  public LoginCredentials(
    final String password
  ) {
    this.password = password;
  }
}
