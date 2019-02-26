package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.support.annotation.NonNull;
import android.content.Context;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;


public class MemberGrid extends GridView{

    // Plz dun h8
    private static List<Member> members;
    private int height;

    public MemberGrid(Context context) {
        super(context);
    }

    public MemberGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MemberGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMembers(List<Member> members) {
        final MemberArrayAdapter adapter = (MemberArrayAdapter) getAdapter();
        adapter.clear();
        adapter.addAll(members);

        this.members = members;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = getDefaultSize(getHeight(), heightMeasureSpec) - getVerticalSpacing() - getPaddingTop() - getPaddingBottom();


        // method is called when view is created so we can conveniently set the adapter here
        final LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MemberArrayAdapter adapter = new MemberArrayAdapter(inflater.getContext(), new ArrayList<>());
        setAdapter(adapter);
        if (members != null) {
            adapter.addAll(members);
        }

    }

    private class MemberArrayAdapter extends ArrayAdapter<Member> {
        private List<Member> values;

        private MemberArrayAdapter(Context context, List<Member> values) {
            super(context, -1, values);
            this.values = values;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final MemberView memberView =  new MemberView(getContext(), values.get(position));

            memberView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, MemberGrid.this.height / 2));

            return memberView;
        }
    }
}
