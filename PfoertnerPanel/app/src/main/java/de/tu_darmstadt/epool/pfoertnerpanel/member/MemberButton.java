package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

public class MemberButton extends FloatingActionButton {
    private int memberId;

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public MemberButton(Context context) {
        super(context);
    }
    public MemberButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MemberButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
