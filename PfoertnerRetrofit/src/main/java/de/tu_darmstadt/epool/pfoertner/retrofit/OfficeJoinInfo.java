package de.tu_darmstadt.epool.pfoertner.retrofit;

public class OfficeJoinInfo {
    public final int officeId;
    public final String joinCode;

    public OfficeJoinInfo(int officeId, String joinCode) {
        this.officeId = officeId;
        this.joinCode = joinCode;
    }

    public static OfficeJoinInfo loadJoinCode(final PfoertnerService service, final Office office, final Authentication auth) {
        return new OfficeJoinInfo(office.id, "TODO"); // TODO
    }
}
