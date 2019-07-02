package com.kinshuu.silverbook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.viewholder> {

    itemclicked activity;
    private ArrayList<Subject> subjects;
    public interface itemclicked{
        void onItemClicked(int index);
    }

    public SubjectAdapter(Context context, ArrayList<Subject> list){
        subjects=list;
        activity=(itemclicked)context;
    }

    public class viewholder extends RecyclerView.ViewHolder{

        TextView TVsubjectname,TVSGPI,TVattendance,TVforcast;
        Button BTNpresent,BTNabsent;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            TVattendance=itemView.findViewById(R.id.TVattendance);
            TVSGPI=itemView.findViewById(R.id.TVSGPI);
            TVsubjectname=itemView.findViewById(R.id.TVsubjectNameLF);
            BTNpresent=itemView.findViewById(R.id.BTNpresent);
            BTNabsent=itemView.findViewById(R.id.BTNabsent);
            TVforcast=itemView.findViewById(R.id.TVforcast);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemClicked(subjects.indexOf((Subject)v.getTag()));
                }
            });
        }
    }

    @NonNull
    @Override
    public SubjectAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_items,viewGroup,false);
        return new viewholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final SubjectAdapter.viewholder viewHolder, final int i) {
        viewHolder.itemView.setTag(subjects.get(i));
        viewHolder.TVsubjectname.setText(subjects.get(i).getSub_name());
        viewHolder.TVSGPI.setText( Double.toString(subjects.get(i).getSGPI()));
        viewHolder.TVforcast.setText(subjects.get(i).getForcast());
        subjects.get(i).calculatepercent();
        viewHolder.TVattendance.setText((subjects.get(i).getAttendancePercent())+" %");
        viewHolder.BTNpresent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subjects.get(i).setPresent(subjects.get(i).getPresent()+1);
                subjects.get(i).setTotaldays(subjects.get(i).getTotaldays()+1);
                subjects.get(i).calculatepercent();
                subjects.get(i).setAttendancePercent(subjects.get(i).getAttendancePercent());
                viewHolder.TVattendance.setText((subjects.get(i).getAttendancePercent())+" %");
                viewHolder.TVforcast.setText(subjects.get(i).getForcast());
            }
        });
        viewHolder.BTNabsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subjects.get(i).setTotaldays(subjects.get(i).getTotaldays()+1);
                subjects.get(i).calculatepercent();
                subjects.get(i).setAttendancePercent(subjects.get(i).getAttendancePercent());
                viewHolder.TVattendance.setText((subjects.get(i).getAttendancePercent())+" %");
                viewHolder.TVforcast.setText(subjects.get(i).getForcast());
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

}
