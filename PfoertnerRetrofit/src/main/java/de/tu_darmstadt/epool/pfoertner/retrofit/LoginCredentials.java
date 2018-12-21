package de.tu_darmstadt.epool.pfoertner.retrofit;

public class LoginCredentials {
  public final String password;
  public final int username;

  public LoginCredentials(
    final String password,
    final int username
  ) {
    this.password = password;
    this.username = username;
  }
}
