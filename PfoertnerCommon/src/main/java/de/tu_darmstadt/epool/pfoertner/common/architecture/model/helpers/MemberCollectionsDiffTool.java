package de.tu_darmstadt.epool.pfoertner.common.architecture.model.helpers;

import de.tu_darmstadt.epool.pfoertner.common.architecture.helpers.CollectionsDiffTool;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;

public class MemberCollectionsDiffTool extends CollectionsDiffTool<Member> {
    /**
     * @param lhs First instance
     * @param rhs Second instance
     * @return True if the instances have the same id, meaning that they refer to the same entry in the database
     */
    @Override
    protected boolean haveSameId(Member lhs, Member rhs) {
        return lhs.getId() == rhs.getId();
    }
}
