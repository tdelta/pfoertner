package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.MainActivity;
import de.tu_darmstadt.epool.pfoertnerpanel.R;

public class MemberListFragment extends ListFragment {

    private int currentMemberId;
    private int position;
    private static List<Member> members;

    public int getCurrentMember(){
        return currentMemberId;
    }

    public void setMembers(List<Member> members) {
        MemberArrayAdapter adapter = (MemberArrayAdapter) getListAdapter();
        adapter.clear();
        adapter.addAll(members);
        this.members = members;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        currentMemberId = ((MemberView)v).getMemberId();
        FragmentTransaction transaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.member_fragment, ((MemberView) v).getFragment());
        transaction.commit();
        this.position = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentMemberId = savedInstanceState.getInt("currentMemberId");
            position = savedInstanceState.getInt("position");

        }
        System.out.println("Pimmel: " + currentMemberId + " | " + position);

        MemberArrayAdapter adapter = new MemberArrayAdapter(inflater.getContext(), new ArrayList<>());
        if (members != null) {
            adapter.addAll(members);
        }

        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putInt("currentMemberId", currentMemberId);
        outState.putInt("position", position);

    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (position != 0) {
            getListView().setItemChecked(position, true);
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
            MemberView memberView =  new MemberView(getContext(), values.get(position));

            if (memberView.getMemberId() == currentMemberId) {
                FragmentTransaction transaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.member_fragment, memberView.getFragment());
                transaction.commit();
            }

            return memberView;
        }
    }
}
