package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

public class OfficeJoinData {
  @Expose private final String joinCode;
  @Expose private final String firstName;
  @Expose private final String lastName;
  @Expose private final String status;


  public OfficeJoinData(final String joinCode, final String firstName, final String lastName, final String status){
    this.joinCode = joinCode;
    this.firstName = firstName;
    this.lastName = lastName;
    this.status = status;
  }
}
