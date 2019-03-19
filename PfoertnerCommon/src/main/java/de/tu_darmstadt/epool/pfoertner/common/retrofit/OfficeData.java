package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

// Only fields with the Expose annotation will be sent to the server
// Other fields will only be persisted in local memory

/**
 * Marshalling class. Body of the response from the server, when a new office is created.
 */
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
