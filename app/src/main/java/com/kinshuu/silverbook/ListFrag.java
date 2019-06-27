package com.kinshuu.silverbook;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFrag extends Fragment {

    RecyclerView recyclerview;
    RecyclerView.Adapter myadapter;
    RecyclerView.LayoutManager layoutManager;
    View view;

    public ListFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerview =view.findViewById(R.id.list);
        recyclerview.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this.getActivity());
        recyclerview.setLayoutManager(layoutManager);

        ArrayList<Subject> subjects;
        subjects= new ArrayList<>();
        subjects.add(new Subject("DST"));
        subjects.add(new Subject("PCE"));
        subjects.add(new Subject("UMC"));
        subjects.add(new Subject("LAL"));
        subjects.add(new Subject("DST Lab"));
        subjects.add(new Subject("COA"));
        subjects.add(new Subject("PFC"));
        subjects.add(new Subject("FEE"));
        myadapter=new SubjectAdapter(this.getActivity(),subjects);
        recyclerview.setAdapter(myadapter);
    }
}
