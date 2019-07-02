package com.kinshuu.silverbook;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SubjectAdapter.itemclicked, PopupDiaogue.PopupDialogueListener {

    PieChart pieChart;
    ArrayList<Subject> subjectsmain,subjectssync;
    Integer indexmain;
    TextView TVsubjectnameDF;
    TextView TVattendancefraction;
    Button BTNeditattendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        subjectsmain = LoadData();
        if(subjectsmain.size()==0){
            subjectsmain.add(new Subject("Due to some"));
            subjectsmain.add(new Subject("Reason, Not able"));
            subjectsmain.add(new Subject("to load Data :(("));
        }
        FragmentManager fm= getSupportFragmentManager();
        FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
        DetailFrag detailFrag=(DetailFrag)fm.findFragmentById(R.id.detail_frag);
        ListFrag listFrag= (ListFrag) fm.findFragmentById(R.id.list_frag);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("arraylist",subjectsmain);
        listFrag.getArgs(bundle);

        if(findViewById(R.id.layout_portrait)!=null) {
            if (detailFrag != null) {
                ft.hide(detailFrag);
            }
            ft.show(listFrag);
            ft.commit();
        }
        else {
            Toast.makeText(this, "Please use portrait mode for better visuals :)", Toast.LENGTH_LONG).show();
            ft.show(detailFrag);
            ft.show(listFrag);
            ft.commit();
            setdetailfrag(0);
        }
    }

    private void addDataset(PieChart pieChart, ArrayList<Subject> subjectsmain, int index) {// This method sets up the piechart
        pieChart = findViewById(R.id.piechart);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        ArrayList<PieEntry> yenteries=new ArrayList<>();
        ArrayList<String> xenteries=new ArrayList<>();
        String[] xdata={"absent","present"};
        ArrayList<Integer> colors= new ArrayList<>();
        colors.add(Color.RED);
        colors.add(Color.CYAN);
        yenteries.add(new PieEntry((subjectsmain.get(index).getTotaldays())-subjectsmain.get(index).getPresent(),"Absent"));
        yenteries.add(new PieEntry(subjectsmain.get(index).getPresent(),"Present"));

        for(int i=0;i<2;i++){
            xenteries.add(xdata[i]);
        }
        Description description= pieChart.getDescription();
        description.setText("Your Attendance in days");
        PieDataSet pieDataSet = new PieDataSet(yenteries,"(In days)");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setNoDataText("A Pie  here would show your attendance");
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    public void onItemClicked(final int index)     {
        setdetailfrag(index);
    }

    private void OpenPopup() {
        PopupDiaogue popupDiaogue= new PopupDiaogue();
        popupDiaogue.show(getSupportFragmentManager(),"EditAttendance Popup");
    }

    @Override
    public void applytexts(Integer classesattended, Integer totalclasses) {
        subjectsmain.get(indexmain).setPresent(classesattended);
        subjectsmain.get(indexmain).setTotaldays(totalclasses);
        setdetailfragnoback(indexmain);
    }

    public void setdetailfrag(final int index){
        Log.d("Inonitemclicked","at beginning");
        addDataset(pieChart, subjectsmain, index);//setting up pie chart

        //now setting up detail frag.
        TVattendancefraction=findViewById(R.id.TVAttendanceFraction);
        TVsubjectnameDF=findViewById(R.id.TVSubjectNameDF);
        TVsubjectnameDF.setText(subjectsmain.get(index).getSub_name());
        BTNeditattendance=findViewById(R.id.BTNEditAttendance);
        String attendance= subjectsmain.get(index).getPresent()+"/"+subjectsmain.get(index).getTotaldays();
        TVattendancefraction.setText(attendance);
        BTNeditattendance=findViewById(R.id.BTNEditAttendance);
        BTNeditattendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPopup();
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

    public void setdetailfragnoback(final int index){
        Log.d("Inonitemclicked","at beginning");
        addDataset(pieChart, subjectsmain, index);//setting up pie chart

        //now setting up detail frag.
        TVattendancefraction=findViewById(R.id.TVAttendanceFraction);
        TVsubjectnameDF=findViewById(R.id.TVSubjectNameDF);
        TVsubjectnameDF.setText(subjectsmain.get(index).getSub_name());
        BTNeditattendance=findViewById(R.id.BTNEditAttendance);
        String attendance= subjectsmain.get(index).getPresent()+"/"+subjectsmain.get(index).getTotaldays();
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

    private void SaveData(ArrayList<Subject> subjectsmain){
        SharedPreferences SParraylist = Objects.requireNonNull(this).getSharedPreferences("SubjectsArrayList",MODE_PRIVATE);
        SharedPreferences.Editor editor=SParraylist.edit();
        Gson gson= new Gson ();
        String json=gson.toJson(subjectsmain);
        editor.putString("subjectslist",json);
        editor.apply();
    }

    private ArrayList<Subject> LoadData(){
        ArrayList<Subject> subjectsmain = new ArrayList<Subject>();
        SharedPreferences SParraylist = Objects.requireNonNull(this).getSharedPreferences("SubjectsArrayList",MODE_PRIVATE);
        Gson gson= new Gson();
        String json = SParraylist.getString("subjectslist",null);
        Type type= new TypeToken<ArrayList<Subject>>(){}.getType();
        subjectsmain=gson.fromJson(json,type);
        if(subjectsmain==null){
            subjectsmain=new ArrayList<Subject>();
        }
        return subjectsmain;
    }

    @Override
    protected void onPause() {
        SaveData(subjectsmain);
        super.onPause();
    }
}
