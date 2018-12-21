package service;

public class LoginCredentials {
  public final String password;
  public final int userid;

  public LoginCredentials(
    final String password,
    final int userid
  ) {
    this.password = password;
    this.userid = userid;
  }
}
