package com.kinshuu.silverbook;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SubjectAdapter.itemclicked {

    PieChart pieChart;
    private Integer[] ydata ={25,75};
    private String[] xdata={"absent","present"};
    TextView TVsubjectnameDF;
    TextView TVattendancefraction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
        FragmentManager fm=getSupportFragmentManager();
        DetailFrag detailFrag=(DetailFrag  )fm.findFragmentById(R.id.fragment7);
        if(detailFrag!=null) {
            ft.hide(detailFrag);
            Log.d("inOnitemclicked", "onItemClicked: detailfrag!=null");
        }
        ListFrag listFrag=(ListFrag)fm.findFragmentById(R.id.fragment5);
        if(listFrag!=null)
            ft.show(listFrag);
        ft.commit();
        Log.d("Inonitemclicked","fragment comitted");

    }

    private void addDataset(PieChart pieChart, ArrayList<Subject> subjects, int index) {
        ArrayList<PieEntry> yenteries=new ArrayList<>();
        ArrayList<String> xenteries=new ArrayList<>();
        ArrayList<Integer> colors= new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.CYAN);
        yenteries.add(new PieEntry((subjects.get(index).getTotaldays())-subjects.get(index).getPresent()));
        yenteries.add(new PieEntry(subjects.get(index).getPresent()));

        for(int i=0;i<ydata.length;i++){
            xenteries.add(xdata[i]);
        }
        Description description= pieChart.getDescription();
        description.setText("Your Attendance");
        PieDataSet pieDataSet = new PieDataSet(yenteries,"Attendance");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    public void onItemClicked(int index, ArrayList<Subject> subjects) {
        Log.d("Inonitemclicked","at beginning");
        pieChart = findViewById(R.id.piechart);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        addDataset(pieChart, subjects, index);
        TVattendancefraction=findViewById(R.id.TVAttendanceFraction);
        TVsubjectnameDF=findViewById(R.id.TVSubjectNameDF);
        TVsubjectnameDF.setText(subjects.get(index).getSub_name());
        String attendance= subjects.get(index).getPresent()+"/"+subjects.get(index).getTotaldays();
        TVattendancefraction.setText(attendance);
        FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
        FragmentManager fm=getSupportFragmentManager();
        DetailFrag detailFrag=(DetailFrag)fm.findFragmentById(R.id.fragment7);
        if(detailFrag!=null) {
            ft.show(detailFrag);
            Log.d("inOnitemclicked", "onItemClicked: detailfrag!=null");
        }
        ListFrag listFrag=(ListFrag)fm.findFragmentById(R.id.fragment5);
        if(listFrag!=null)
            ft.hide(listFrag);
        ft.addToBackStack(null);
        ft.commit();
        Log.d("Inonitemclicked","fragment comitted");

    }

}
