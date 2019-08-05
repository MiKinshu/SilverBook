package com.kinshuu.silverbook;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.viewholder> {

    itemclicked activity;
    private ArrayList<Subject> subjects;
    private  ArrayList<Log> LogList;
    Integer Elegibility, sssize;
    public interface itemclicked{
        void onItemClicked(int index);
    }

    public SubjectAdapter(Context context, ArrayList<Subject> list, int elegible, ArrayList<Log> logList, int subjectsyncsize){
        subjects=list;
        Elegibility=elegible;
        activity=(itemclicked)context;
        LogList=logList;
        sssize=subjectsyncsize;
    }

    public class viewholder extends RecyclerView.ViewHolder{

        TextView TVsubjectname,TVforcast,Textview1;
        Button BTNpresent,BTNabsent;
        PieChart piechartli, piechartliGPA;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            TVsubjectname=itemView.findViewById(R.id.TVsubjectNameLF);
            BTNpresent=itemView.findViewById(R.id.BTNpresent);
            BTNabsent=itemView.findViewById(R.id.BTNabsent);
            TVforcast=itemView.findViewById(R.id.TVforcast);
            Textview1=itemView.findViewById(R.id.textView1);
            piechartli=itemView.findViewById(R.id.piechartli);
            piechartliGPA=itemView.findViewById(R.id.piechartliGPA);
            if(Elegibility==0) {
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
                                    if(subjects.indexOf((Subject)v.getTag())<sssize){
                                        Toast.makeText(v.getContext(), "Cannot delete this subject", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        notifyItemRemoved(subjects.indexOf((Subject) v.getTag()));
                                        subjects.remove(subjects.indexOf((Subject) v.getTag()));
                                        Toast.makeText(v.getContext(), "Subject Deleted!", Toast.LENGTH_SHORT).show();
                                    }
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
        viewHolder.TVforcast.setText(subjects.get(i).getForcast());
        subjects.get(i).calculatepercent();
        if(Elegibility==0){
            viewHolder.piechartliGPA.setVisibility(View.GONE);
            viewHolder.Textview1.setVisibility(View.GONE);
        }

        //setting up attendance piechart
        viewHolder.piechartli.setHoleRadius(90f);
        viewHolder.piechartli.setTransparentCircleAlpha(0);
        ArrayList<PieEntry> yenteries=new ArrayList<>();
        ArrayList<Integer> colors= new ArrayList<>();
        colors.add(Color.WHITE);
        colors.add(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorButtons));
        yenteries.add(new PieEntry((subjects.get(i).getTotaldays())-subjects.get(i).getPresent()));
        yenteries.add(new PieEntry(subjects.get(i).getPresent()));
        Description description= viewHolder.piechartli.getDescription();
        description.setText("");
        PieDataSet pieDataSet = new PieDataSet(yenteries,"");
        pieDataSet.setSliceSpace(0);
        pieDataSet.setValueTextSize(0);
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        viewHolder.piechartli.setNoDataText("A Pie  here would show your attendance");
        viewHolder.piechartli.setData(pieData);
        viewHolder.piechartli.setCenterTextSize(12);
        viewHolder.piechartli.setCenterText(subjects.get(i).getAttendancePercent()+"%");
        viewHolder.piechartli.setTouchEnabled(false);
        Legend legend=viewHolder.piechartli.getLegend();
        legend.setEnabled(false);
        //viewHolder.piechartli.invalidate();

        //setting up GPA piechart
        viewHolder.piechartliGPA.setHoleRadius(90f);
        viewHolder.piechartliGPA.setTransparentCircleAlpha(0);
        ArrayList<PieEntry> yenteriesGPA=new ArrayList<>();
        yenteriesGPA.add(new PieEntry((float)(10-subjects.get(i).getGPA())));
        yenteriesGPA.add(new PieEntry((float)subjects.get(i).getGPA()));
        Description descriptionGPA= viewHolder.piechartliGPA.getDescription();
        descriptionGPA.setText("");
        PieDataSet pieDataSetGPA = new PieDataSet(yenteriesGPA,"");
        pieDataSetGPA.setSliceSpace(0);
        pieDataSetGPA.setValueTextSize(0);
        pieDataSetGPA.setColors(colors);
        PieData pieDataGPA = new PieData(pieDataSetGPA);
        viewHolder.piechartliGPA.setNoDataText("A Pie  here would show your attendance");
        viewHolder.piechartliGPA.setData(pieDataGPA);
        viewHolder.piechartliGPA.setCenterTextSize(12);
        viewHolder.piechartliGPA.setCenterText(subjects.get(i).getGPA()+"");
        viewHolder.piechartliGPA.setTouchEnabled(false);
        Legend legendGPA=viewHolder.piechartliGPA.getLegend();
        legendGPA.setEnabled(false);
        viewHolder.piechartliGPA.invalidate();

        viewHolder.BTNpresent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log log=new Log(Calendar.getInstance().getTime().toString().split("G")[0]);
                subjects.get(i).setPresent(subjects.get(i).getPresent()+1);
                subjects.get(i).setTotaldays(subjects.get(i).getTotaldays()+1);
                subjects.get(i).calculatepercent();
                subjects.get(i).setAttendancePercent(subjects.get(i).getAttendancePercent());
                viewHolder.TVforcast.setText(subjects.get(i).getForcast());
                log.setAction(subjects.get(i).getSub_name()+" : Present marked on ");
                Toast.makeText(v.getContext(), "Present marked for "+subjects.get(i).getSub_name(), Toast.LENGTH_SHORT).show();

                //setting up attendance piechart
                viewHolder.piechartli.setHoleRadius(90f);
                viewHolder.piechartli.setTransparentCircleAlpha(0);
                ArrayList<PieEntry> yenteries=new ArrayList<>();
                ArrayList<Integer> colors= new ArrayList<>();
                colors.add(Color.WHITE);
                colors.add(ContextCompat.getColor(v.getContext(), R.color.colorButtons));
                yenteries.add(new PieEntry((subjects.get(i).getTotaldays())-subjects.get(i).getPresent()));
                yenteries.add(new PieEntry(subjects.get(i).getPresent()));
                Description description= viewHolder.piechartli.getDescription();
                description.setText("");
                PieDataSet pieDataSet = new PieDataSet(yenteries,"");
                pieDataSet.setSliceSpace(0);
                pieDataSet.setValueTextSize(0);
                pieDataSet.setColors(colors);
                PieData pieData = new PieData(pieDataSet);
                viewHolder.piechartli.setNoDataText("A Pie  here would show your attendance");
                viewHolder.piechartli.setData(pieData);
                viewHolder.piechartli.setCenterText(subjects.get(i).getAttendancePercent()+"%");
                viewHolder.piechartli.setTouchEnabled(false);
                Legend legend=viewHolder.piechartli.getLegend();
                legend.setEnabled(false);
                viewHolder.piechartli.invalidate();


                LogList.add(log);
            }
        });
        viewHolder.BTNabsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log log=new Log(Calendar.getInstance().getTime().toString().split("G")[0]);
                subjects.get(i).setTotaldays(subjects.get(i).getTotaldays()+1);
                subjects.get(i).calculatepercent();
                subjects.get(i).setAttendancePercent(subjects.get(i).getAttendancePercent());
                viewHolder.TVforcast.setText(subjects.get(i).getForcast());
                log.setAction(subjects.get(i).getSub_name()+" : Absent marked on ");

                //setting up attendance piechart
                viewHolder.piechartli.setHoleRadius(90f);
                viewHolder.piechartli.setTransparentCircleAlpha(0);
                ArrayList<PieEntry> yenteries=new ArrayList<>();
                ArrayList<Integer> colors= new ArrayList<>();
                colors.add(Color.WHITE);
                colors.add(ContextCompat.getColor(v.getContext(), R.color.colorButtons));
                yenteries.add(new PieEntry((subjects.get(i).getTotaldays())-subjects.get(i).getPresent()));
                yenteries.add(new PieEntry(subjects.get(i).getPresent()));
                Description description= viewHolder.piechartli.getDescription();
                description.setText("");
                PieDataSet pieDataSet = new PieDataSet(yenteries,"");
                pieDataSet.setSliceSpace(0);
                pieDataSet.setValueTextSize(0);
                pieDataSet.setColors(colors);
                PieData pieData = new PieData(pieDataSet);
                viewHolder.piechartli.setNoDataText("A Pie  here would show your attendance");
                viewHolder.piechartli.setData(pieData);
                viewHolder.piechartli.setCenterText(subjects.get(i).getAttendancePercent()+"%");
                viewHolder.piechartli.setTouchEnabled(false);
                Legend legend=viewHolder.piechartli.getLegend();
                legend.setEnabled(false);
                viewHolder.piechartli.invalidate();


                LogList.add(log);
                Toast.makeText(v.getContext(), "Absent marked for "+subjects.get(i).getSub_name(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

}
