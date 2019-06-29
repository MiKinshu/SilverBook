package com.kinshuu.silverbook;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFrag extends Fragment {

    RecyclerView recyclerview;
    RecyclerView.Adapter myadapter;
    RecyclerView.LayoutManager layoutManager;
    View view;
    ArrayList<Subject> subjects;

    public ListFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("INLISTFRAG","In oncreat Listfrag");
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

        subjects=LoadData();
        if(subjects.size()==0) {
            subjects.add(new Subject("DST"));
            subjects.add(new Subject("PCE"));
            subjects.add(new Subject("UMC"));
            subjects.add(new Subject("LAL"));
            subjects.add(new Subject("DST Lab"));
            subjects.add(new Subject("COA"));
            subjects.add(new Subject("PFC"));
            subjects.add(new Subject("FEE"));
        }
        myadapter = new SubjectAdapter(this.getActivity(), subjects);
        recyclerview.setAdapter(myadapter);

    }

    private void SaveData(ArrayList<Subject> subjects){
        SharedPreferences SParraylist = Objects.requireNonNull(this.getActivity()).getSharedPreferences("SubjectsArrayList",MODE_PRIVATE);
        SharedPreferences.Editor editor=SParraylist.edit();
        Gson gson= new Gson ();
        String json=gson.toJson(subjects);
        editor.putString("subjectslist",json);
        editor.apply();
    }

    private ArrayList<Subject> LoadData(){
        ArrayList<Subject> subjects = new ArrayList<Subject>();
        SharedPreferences SParraylist = Objects.requireNonNull(this.getActivity()).getSharedPreferences("SubjectsArrayList",MODE_PRIVATE);
        Gson gson= new Gson();
        String json = SParraylist.getString("subjectslist",null);
        Type type= new TypeToken<ArrayList<Subject>>(){}.getType();
        subjects=gson.fromJson(json,type);
        if(subjects==null){
            subjects=new ArrayList<Subject>();
        }
        return subjects;
    }

    @Override
    public void onPause() {
        SaveData(subjects);
        super.onPause();
    }
}
