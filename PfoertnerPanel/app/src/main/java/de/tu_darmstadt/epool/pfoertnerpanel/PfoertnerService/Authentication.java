package de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService;

public class Authentication {
  public final String id;
  public final int ttl;
  public final String created;
  public final int userId;

  public Authentication(
    final String id,
    final int ttl,
    final String created,
    final int userId
  ) {
    this.id = id;
    this.ttl = ttl;
    this.created = created;
    this.userId = userId;
  }
}
