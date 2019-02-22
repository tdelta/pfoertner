package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.support.annotation.NonNull;
import android.content.Context;

import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.R;

public class MemberGrid extends GridView{

    private static List<MemberEntity> members;
    private static int height;

    public MemberGrid(Context context) {
        super(context);
        setup(context);
    }

    public MemberGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public MemberGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    public void setMembers(List<MemberEntity> members) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) getAdapter();
        adapter.clear();
        adapter.addAll(members);
        adapter.add(members.get(0));
        adapter.add(members.get(0));

        this.members = members;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private void setup(Context context) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MemberArrayAdapter adapter = new MemberArrayAdapter(inflater.getContext(), new ArrayList<>());
        setAdapter(adapter);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = getDefaultSize(getHeight(), heightMeasureSpec) - getVerticalSpacing() - getPaddingTop() - getPaddingBottom();
    }

    private class MemberArrayAdapter extends ArrayAdapter<MemberEntity> {
        private List<MemberEntity> values;

        private MemberArrayAdapter(Context context, List<MemberEntity> values) {
            super(context, -1, values);
            this.values = values;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            MemberView memberView =  new MemberView(getContext(), values.get(position));

            memberView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, MemberGrid.this.height / 2));

            return memberView;
        }
    }
}
