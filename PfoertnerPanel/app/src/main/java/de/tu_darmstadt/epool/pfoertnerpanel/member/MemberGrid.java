package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.content.Context;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.calendar.model.Event;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.viewmodels.OfficeViewModel;


public class MemberGrid extends GridView{

    private int height;

    public MemberGrid(Context context) {
        super(context);
        init();
    }

    public MemberGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MemberGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        // method is called when view is created so we can conveniently set the adapter here
        final LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MemberArrayAdapter adapter = new MemberArrayAdapter(inflater.getContext(), new ArrayList<>());
        setAdapter(adapter);
    }

    public void setEvents(int id, List<Event> events) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) getAdapter();
        if (adapter != null) {
            adapter.setEvents(id, events);
        }

        }

    public void setMembers(List<Member> members) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) getAdapter();

        if (adapter != null) {
            adapter.clear();
            adapter.addAll(members);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = getDefaultSize(getHeight(), heightMeasureSpec) - getVerticalSpacing() - getPaddingTop() - getPaddingBottom();
    }

    private class MemberArrayAdapter extends ArrayAdapter<Member> {
        private List<Member> values;
        private Map<Integer, List<Event>> eventMap;

        private MemberArrayAdapter(Context context, List<Member> values) {
            super(context, -1, values);
            this.eventMap =  new HashMap<>();
            this.values = values;
        }

        public void setEvents(int id, List<Event> events) {
            eventMap.put(id, events);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            Member member = values.get(position);
            List<Event> events = eventMap.getOrDefault(member.getId(), new ArrayList<>());
            final MemberView memberView =  new MemberView(getContext(), member, events);

            memberView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, MemberGrid.this.height / 2));

            return memberView;
        }
    }
}
