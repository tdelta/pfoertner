package de.tu_darmstadt.epool.pfoertnerpanel.member;

import androidx.annotation.NonNull;
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

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Timeslot;

/**
 * A GridView subclass that ensures that its views take half of its height
 * Children views are office members
 */
public class MemberGrid extends GridView{

    /**
     * The height of the Grid in pixels
     */
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

    /**
     * Set the adapter that handles the creation of the views of the gridview
     */
    private void init() {
        // method is called when view is created so we can conveniently set the adapter here
        final LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MemberArrayAdapter adapter = new MemberArrayAdapter(inflater.getContext(), new ArrayList<>());
        setAdapter(adapter);
    }

    /**
     * Set or update the time slots for a member
     * @param id a unique id that is associated with a member
     * @param timeslots a list of all available time slots
     */
    public void setTimeslots(int id, List<Timeslot> timeslots) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) getAdapter();
        if (adapter != null) {
            adapter.setTimeslots(id, timeslots);
        }

        }

    /**
     * Set or update the members of an office
     * @param members a complete list of all members
     */
    public void setMembers(List<Member> members) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) getAdapter();

        if (adapter != null) {
            adapter.clear();
            adapter.addAll(members);
        }
    }

    /**
     * This is called to find out how big a view should be.
     * The parent supplies constraint information in the width and height parameters. 
     * Saves the height (without margins and paddings) to determine the height for the gridviews children
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = getDefaultSize(getHeight(), heightMeasureSpec) - getVerticalSpacing() - getPaddingTop() - getPaddingBottom();
    }

    /**
     * ArrayAdapter that generates the member views from member data
     */
    private class MemberArrayAdapter extends ArrayAdapter<Member> {
        /**
         * List of members
         */
        private List<Member> values;
        /**
         * Mapping from member IDs to time slots
         */
        private Map<Integer, List<Timeslot>> timeslotMap;

        private MemberArrayAdapter(Context context, List<Member> values) {
            super(context, -1, values);
            this.timeslotMap =  new HashMap<>();
            this.values = values;
        }

        /**
         * Update the time slots for a member
         * @param id the unique member id
         * @param timeslots the associated time slots
         */
        public void setTimeslots(int id, List<Timeslot> timeslots) {
            timeslotMap.put(id, timeslots);
            notifyDataSetChanged();
        }

        /**
         * Creates the member views from the member data and time slots
         * @param position the position in the gridview
         * @param convertView the old view if a view is overridden, else null
         * @param parent the parent view
         */
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            Member member = values.get(position);
            List<Timeslot> timeslots = timeslotMap.getOrDefault(member.getId(), new ArrayList<>());
            final MemberView memberView =  new MemberView(getContext(), member, timeslots);

            memberView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, MemberGrid.this.height / 2));

            return memberView;
        }
    }
}
