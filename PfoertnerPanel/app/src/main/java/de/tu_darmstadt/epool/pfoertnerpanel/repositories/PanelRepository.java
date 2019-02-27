package de.tu_darmstadt.epool.pfoertnerpanel.repositories;

import de.tu_darmstadt.epool.pfoertnerpanel.db.PanelDatabase;

public class PanelRepository {
    private final MemberCalendarInfoRepository memberCalendarInfoRepo;

    public PanelRepository(final PanelDatabase db) {
        this.memberCalendarInfoRepo = new MemberCalendarInfoRepository(db);
    }

    public MemberCalendarInfoRepository getMemberCalendarInfoRepo() {
        return memberCalendarInfoRepo;
    }
}
