package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

public class OfficeData {
  @Expose public final int id;
  @Expose public String joinCode;
  @Expose public String status;

  public OfficeData(final int id, final String joinCode, final String status) {
    this.id = id;
    this.joinCode = joinCode;
    this.status = status;
  }
}
