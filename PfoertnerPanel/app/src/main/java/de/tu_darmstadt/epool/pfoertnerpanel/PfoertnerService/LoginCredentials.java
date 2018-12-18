package de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService;

public class LoginCredentials {
  public final String password;
  public final String email;

  public LoginCredentials(
    final String password,
    final String email
  ) {
    this.password = password;
    this.email = email;
  }
}
