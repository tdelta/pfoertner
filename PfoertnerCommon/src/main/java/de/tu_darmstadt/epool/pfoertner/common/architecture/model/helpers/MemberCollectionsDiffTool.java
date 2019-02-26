package de.tu_darmstadt.epool.pfoertner.common.architecture.model.helpers;

import de.tu_darmstadt.epool.pfoertner.common.architecture.helpers.CollectionsDiffTool;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;

public class MemberCollectionsDiffTool extends CollectionsDiffTool<Member> {
    @Override
    protected boolean haveSameId(Member lhs, Member rhs) {
        return lhs.getId() == rhs.getId();
    }
}
