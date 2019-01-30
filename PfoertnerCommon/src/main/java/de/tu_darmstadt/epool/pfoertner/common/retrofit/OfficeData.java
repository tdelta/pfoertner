package de.tu_darmstadt.epool.pfoertner.common.retrofit;

public class OfficeData {
  public final int id;
  public String joinCode;
  public String status;

  public OfficeData(final int id, final String joinCode, final String status) {
    this.id = id;
    this.joinCode = joinCode;
    this.status = status;
  }
}
