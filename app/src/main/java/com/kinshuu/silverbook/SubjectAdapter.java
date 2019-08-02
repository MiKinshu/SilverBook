package com.kinshuu.silverbook;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;

import java.util.ArrayList;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.viewholder> {

    itemclicked activity;
    private ArrayList<Subject> subjects;
    Integer Elegibility;
    public interface itemclicked{
        void onItemClicked(int index);
    }

    public SubjectAdapter(Context context, ArrayList<Subject> list, int elegible){
        subjects=list;
        Elegibility=elegible;
        activity=(itemclicked)context;
    }

    public class viewholder extends RecyclerView.ViewHolder{

        TextView TVsubjectname,TVGPA,TVattendance,TVforcast,Textview1;
        Button BTNpresent,BTNabsent;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            TVattendance=itemView.findViewById(R.id.TVattendance);
            TVGPA=itemView.findViewById(R.id.TVGPA);
            TVsubjectname=itemView.findViewById(R.id.TVsubjectNameLF);
            BTNpresent=itemView.findViewById(R.id.BTNpresent);
            BTNabsent=itemView.findViewById(R.id.BTNabsent);
            TVforcast=itemView.findViewById(R.id.TVforcast);
            Textview1=itemView.findViewById(R.id.textView1);
            if(Elegibility==0) {
                TVGPA.setVisibility(View.GONE);
                Textview1.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemClicked(subjects.indexOf((Subject)v.getTag()));
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete Subject")
                            .setMessage("Are you sure you want to delete this Subject? This cannot be undone.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                    notifyItemRemoved(subjects.indexOf((Subject)v.getTag()));
                                    subjects.remove(subjects.indexOf((Subject)v.getTag()));
                                    Toast.makeText(v.getContext(), "Subject Deleted!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return true;
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
        viewHolder.TVGPA.setText( Double.toString(subjects.get(i).getGPA()));
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
