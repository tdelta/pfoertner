package de.tu_darmstadt.epool.pfoertner.common.retrofit;

public class OfficeJoinData {
  private final String joinCode;
  private final String firstName;
  private final String lastName;

  public OfficeJoinData(
          final String joinCode,
          final String firstName,
          final String lastName
  ){
    this.joinCode = joinCode;
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
