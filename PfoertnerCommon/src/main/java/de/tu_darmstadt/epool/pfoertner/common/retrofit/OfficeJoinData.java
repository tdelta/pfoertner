package de.tu_darmstadt.epool.pfoertner.common.retrofit;

public class OfficeJoinData {
  private final String joinCode;
  private final String firstName;
  private final String lastName;
  private final String status;

  public OfficeJoinData(
          final String joinCode,
          final String firstName,
          final String lastName,
          final String status
  ){
    this.joinCode = joinCode;
    this.firstName = firstName;
    this.lastName = lastName;
    this.status = status;
  }
}
