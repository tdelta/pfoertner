package de.tu_darmstadt.epool.pfoertnerpanel;

import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.Office;
import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.User;

public class Data {
    private final User user;
    private final Office office;

    public Data(User user, Office office) {
        this.user = user;
        this.office = office;
    }

    public User getUser() {
        return user;
    }

    public Office getOffice() {
        return office;
    }
}
