package com.kinshuu.silverbook;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SubjectAdapter.itemclicked, PopupDiaogue.PopupDialogueListener {

    PieChart pieChart;
    private Integer[] ydata ={25,75};
    private String[] xdata={"absent","present"};
    ArrayList<Subject> subjectsmain;
    Integer indexmain;
    TextView TVsubjectnameDF;
    TextView TVattendancefraction;
    Button BTNeditattendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.layout_portrait)!=null) {
            FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
            FragmentManager fm=getSupportFragmentManager();
            DetailFrag detailFrag=(DetailFrag )fm.findFragmentById(R.id.detail_frag);
            if(detailFrag!=null) {
                ft.hide(detailFrag);
                Log.d("inOnitemclicked", "onItemClicked: detailfrag!=null");
            }
            ListFrag listFrag=(ListFrag)fm.findFragmentById(R.id.list_frag);
            if(listFrag!=null)
                ft.show(listFrag);
            ft.commit();
            Log.d("Inonitemclicked","fragment comitted");
        }
        else
            Toast.makeText(this, "Please use portrait mode for better visuals :)", Toast.LENGTH_LONG).show();
    }

    private void addDataset(PieChart pieChart, ArrayList<Subject> subjects, int index) {// This method sets up the piechart
        subjectsmain=subjects;
        pieChart = findViewById(R.id.piechart);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        ArrayList<PieEntry> yenteries=new ArrayList<>();
        ArrayList<String> xenteries=new ArrayList<>();
        ArrayList<Integer> colors= new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.CYAN);
        yenteries.add(new PieEntry((subjects.get(index).getTotaldays())-subjects.get(index).getPresent(),"Absent"));
        yenteries.add(new PieEntry(subjects.get(index).getPresent(),"Present"));

        for(int i=0;i<ydata.length;i++){
            xenteries.add(xdata[i]);
        }
        Description description= pieChart.getDescription();
        description.setText("Your Attendance in days");
        PieDataSet pieDataSet = new PieDataSet(yenteries,"(In days)");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setNoDataText("A Pie chart would show if you mark Present or Absent");
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    public void onItemClicked(final int index, ArrayList<Subject> subjects)     {
        setdetailfrag(index,subjects);
    }

    private void OpenPopup() {
        PopupDiaogue popupDiaogue= new PopupDiaogue();
        popupDiaogue.show(getSupportFragmentManager(),"EditAttendance Popup");
    }

    @Override
    public void applytexts(Integer classesattended, Integer totalclasses) {
        subjectsmain.get(indexmain).setPresent(classesattended);
        subjectsmain.get(indexmain).setTotaldays(totalclasses);
        setdetailfragnoback(indexmain,subjectsmain);
    }

    public void setdetailfrag(final int index, ArrayList<Subject> subjects){
        Log.d("Inonitemclicked","at beginning");
        addDataset(pieChart, subjects, index);//setting up pie chart

        //now setting up detail frag.
        TVattendancefraction=findViewById(R.id.TVAttendanceFraction);
        TVsubjectnameDF=findViewById(R.id.TVSubjectNameDF);
        TVsubjectnameDF.setText(subjects.get(index).getSub_name());
        BTNeditattendance=findViewById(R.id.BTNEditAttendance);
        String attendance= subjects.get(index).getPresent()+"/"+subjects.get(index).getTotaldays();
        TVattendancefraction.setText(attendance);
        BTNeditattendance=findViewById(R.id.BTNEditAttendance);
        BTNeditattendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPopup();
                //Toast.makeText(MainActivity.this, "Button clicked", Toast.LENGTH_SHORT).show();
                indexmain=index;
            }
        });
        if(findViewById(R.id.layout_portrait)!=null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            FragmentManager fm = getSupportFragmentManager();
            DetailFrag detailFrag = (DetailFrag) fm.findFragmentById(R.id.detail_frag);
            if (detailFrag != null) {
                ft.show(detailFrag);
            }
            ListFrag listFrag = (ListFrag) fm.findFragmentById(R.id.list_frag);
            if (listFrag != null)
                ft.hide(listFrag);
            ft.commit();
            ft.addToBackStack(null);
        }
    }

    public void setdetailfragnoback(final int index, ArrayList<Subject> subjects){
        Log.d("Inonitemclicked","at beginning");
        addDataset(pieChart, subjects, index);//setting up pie chart

        //now setting up detail frag.
        TVattendancefraction=findViewById(R.id.TVAttendanceFraction);
        TVsubjectnameDF=findViewById(R.id.TVSubjectNameDF);
        TVsubjectnameDF.setText(subjects.get(index).getSub_name());
        BTNeditattendance=findViewById(R.id.BTNEditAttendance);
        String attendance= subjects.get(index).getPresent()+"/"+subjects.get(index).getTotaldays();
        TVattendancefraction.setText(attendance);
        if(findViewById(R.id.layout_portrait)!=null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            FragmentManager fm = getSupportFragmentManager();
            DetailFrag detailFrag = (DetailFrag) fm.findFragmentById(R.id.detail_frag);
            if (detailFrag != null) {
                ft.show(detailFrag);
            }
            ListFrag listFrag = (ListFrag) fm.findFragmentById(R.id.list_frag);
            if (listFrag != null)
                ft.hide(listFrag);
            ft.commit();
            ft.addToBackStack(null);
        }

        BTNeditattendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPopup();
                indexmain=index;
            }
        });
    }
}
