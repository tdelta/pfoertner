package de.tu_darmstadt.epool.pfoertnerpanel.repositories;

import de.tu_darmstadt.epool.pfoertnerpanel.PanelApplication;
import de.tu_darmstadt.epool.pfoertnerpanel.db.PanelDatabase;

public class PanelRepository {
    private final MemberCalendarInfoRepository memberCalendarInfoRepo;

    public PanelRepository(final PanelDatabase db, final PanelApplication app) {
        this.memberCalendarInfoRepo = new MemberCalendarInfoRepository(db,app);
    }

    public MemberCalendarInfoRepository getMemberCalendarInfoRepo() {
        return memberCalendarInfoRepo;
    }
}
