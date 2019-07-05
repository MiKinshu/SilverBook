package com.kinshuu.silverbook;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SubjectAdapter.itemclicked, PopupDiaogue.PopupDialogueListener {

    PieChart pieChart;//for setting up PieChart in detail frag.
    ArrayList<Subject> subjectsmain=new ArrayList<>();//Main local Subjects ArrayList
    ArrayList<SubjectSync> subjectSyncArrayList=new ArrayList<>();//ArrayList synced from firebase.

    Integer indexmain=0;
    Integer firsttime;

    //For managing userdata.
    Integer Signed_In=1;
    Integer Eligible=0;
    Integer YearOfJoining=2018;
    String College="IIIT-A";
    String Branch="IT";
    private String mUsername="ANONYMOUS";
    String TAG="MyLOGS";

    //Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference msubjectsDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(YearOfJoining>2017&&College.equals("IIIT-A")&&Signed_In==1)
            Eligible=1;
        setContentView(R.layout.activity_main);

        //Checking if the user has signed in for the first time.
        SharedPreferences faveditor=getSharedPreferences("com.kinshuu.silverbook.ft",MODE_PRIVATE);
        firsttime=faveditor.getInt("FT",1);

        mFirebaseDatabase=FirebaseDatabase.getInstance();
        msubjectsDatabaseReference=mFirebaseDatabase.getReference().child(College).child(YearOfJoining.toString()).child(Branch);

        subjectSyncArrayList=SyncData();
        subjectsmain = LoadData();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DetailFrag detailFrag =(DetailFrag)fm.findFragmentById(R.id.detail_frag);

        //Following code sends data to the list frag, what's important is Activity's OnCreate finishes before Fragments OnActivityCreated starts.
        ListFrag listFrag= (ListFrag) fm.findFragmentById(R.id.list_frag);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("arraylist",subjectsmain);
        bundle.putInt("elegible",Eligible);
        Objects.requireNonNull(listFrag).getArgs(bundle);

        if(firsttime==1)
            Toast.makeText(this, "Hang in tight, while we load your data.", Toast.LENGTH_SHORT).show();

        //Following code hides detail frag in portrait mode.
        if(findViewById(R.id.layout_portrait)!=null) {
            if (detailFrag != null) {
                ft.hide(detailFrag);
            }
            ft.show(listFrag);
            ft.commit();
        }
        else {
            ft.show(Objects.requireNonNull(detailFrag));
            ft.show(listFrag);
            ft.commit();
            if(firsttime!=1)
                setdetailfrag(0);
        }

        //following code is to manage the firebase sync data.
        final ArrayList<SubjectSync> subjectSyncArrayListtemp=new ArrayList<>();
        mChildEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                SubjectSync subject= dataSnapshot.getValue(SubjectSync.class);
                subjectSyncArrayListtemp.add(subject);
                Log.d(TAG, "onChildAdded: New child added");
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        msubjectsDatabaseReference.addChildEventListener(mChildEventListener);
        msubjectsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjectSyncArrayList=subjectSyncArrayListtemp;
                if(firsttime==1){
                    //changing firsttime to 0.
                    SharedPreferences.Editor faveditor=getSharedPreferences("com.kinshuu.silverbook.ft",MODE_PRIVATE).edit();
                    faveditor.putInt("FT",0);
                    faveditor.apply();
                    Log.d(TAG, "onDataChange: Saved shared preference");

                    subjectsmain=InitialiseSub();
                    Log.d(TAG, "onDataChange: initialised subjects main");
                    recreate();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //This function is used to update the visibility of views as per the constraints.
    private void UpdateViewVisibility(int i) {
        Group group1, group2,group3,group4,group5,groupGPI;
        TextView TVGPAhead;
        group1=findViewById(R.id.group1);
        group2=findViewById(R.id.group2);
        group3=findViewById(R.id.group3);
        group4=findViewById(R.id.group4);
        group5=findViewById(R.id.group5);
        groupGPI=findViewById(R.id.groupGPI);
        TVGPAhead=findViewById(R.id.TVGPAhead);
        EditText ETuserscore1=findViewById(R.id.ETuserscore1);
        EditText ETuserscore2=findViewById(R.id.ETuserscore2);
        EditText ETuserscore3=findViewById(R.id.ETuserscore3);
        EditText ETuserscore4=findViewById(R.id.ETuserscore4);
        EditText ETuserscore5=findViewById(R.id.ETuserscore5);

        if(Eligible==0){
            group1.setVisibility(View.GONE);
            group2.setVisibility(View.GONE);
            group3.setVisibility(View.GONE);
            group4.setVisibility(View.GONE);
            group5.setVisibility(View.GONE);
            groupGPI.setVisibility(View.GONE);
            TVGPAhead.setText("GPA forcast is only available for IIIT-A batch 2k18 or later. If you belong to the group, please Sign-In.");
        }
        else{
            TextView TVtestnames=findViewById(R.id.TVtestnames);
            TVtestnames.setText(subjectSyncArrayList.get(i).getExamName());
            switch (subjectSyncArrayList.get(i).getNoOfTests()){
                case 1:{
                    group2.setVisibility(View.GONE);
                    group3.setVisibility(View.GONE);
                    group4.setVisibility(View.GONE);
                    group5.setVisibility(View.GONE);
                    TextView TVtestname1=findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0)+"");
                    TextView TVmaxscore1=findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0)+"");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0]+"");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1]+"");
                    ETuserscore3.setText(subjectsmain.get(i).getMarks()[2]+"");
                    ETuserscore4.setText(subjectsmain.get(i).getMarks()[3]+"");
                    break;
                }
                case 2:{
                    group3.setVisibility(View.GONE);
                    group4.setVisibility(View.GONE);
                    group5.setVisibility(View.GONE);
                    TextView TVtestname1=findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0)+"");
                    TextView TVmaxscore1=findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0)+"");
                    TextView TVtestname2=findViewById(R.id.TVtestname2);
                    TVtestname2.setText(subjectSyncArrayList.get(i).getTestNames().get(1)+"");
                    TextView TVmaxscore2=findViewById(R.id.TVmaxscore2);
                    TVmaxscore2.setText(subjectSyncArrayList.get(i).getMaxScores().get(1)+"");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0]+"");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1]+"");
                    ETuserscore3.setText(subjectsmain.get(i).getMarks()[2]+"");
                    ETuserscore4.setText(subjectsmain.get(i).getMarks()[3]+"");
                    break;
                }
                case 3:{
                    group4.setVisibility(View.GONE);
                    group5.setVisibility(View.GONE);
                    TextView TVtestname1=findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0)+"");
                    TextView TVmaxscore1=findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0)+"");
                    TextView TVtestname2=findViewById(R.id.TVtestname2);
                    TVtestname2.setText(subjectSyncArrayList.get(i).getTestNames().get(1)+"");
                    TextView TVmaxscore2=findViewById(R.id.TVmaxscore2);
                    TVmaxscore2.setText(subjectSyncArrayList.get(i).getMaxScores().get(1)+"");
                    TextView TVtestname3=findViewById(R.id.TVtestname3);
                    TVtestname3.setText(subjectSyncArrayList.get(i).getTestNames().get(2)+"");
                    TextView TVmaxscore3=findViewById(R.id.TVmaxscore3);
                    TVmaxscore3.setText(subjectSyncArrayList.get(i).getMaxScores().get(2)+"");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0]+"");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1]+"");
                    ETuserscore3.setText(subjectsmain.get(i).getMarks()[2]+"");
                    ETuserscore4.setText(subjectsmain.get(i).getMarks()[3]+"");
                    break;
                }
                case 4:{
                    group5.setVisibility(View.GONE);
                    TextView TVtestname1=findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0)+"");
                    TextView TVmaxscore1=findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0)+"");
                    TextView TVtestname2=findViewById(R.id.TVtestname2);
                    TVtestname2.setText(subjectSyncArrayList.get(i).getTestNames().get(1)+"");
                    TextView TVmaxscore2=findViewById(R.id.TVmaxscore2);
                    TVmaxscore2.setText(subjectSyncArrayList.get(i).getMaxScores().get(1)+"");
                    TextView TVtestname3=findViewById(R.id.TVtestname3);
                    TVtestname3.setText(subjectSyncArrayList.get(i).getTestNames().get(2)+"");
                    TextView TVmaxscore3=findViewById(R.id.TVmaxscore3);
                    TVmaxscore3.setText(subjectSyncArrayList.get(i).getMaxScores().get(2)+"");
                    TextView TVtestname4=findViewById(R.id.TVtestname4);
                    TVtestname4.setText(subjectSyncArrayList.get(i).getTestNames().get(3)+"");
                    TextView TVmaxscore4=findViewById(R.id.TVmaxscore4);
                    TVmaxscore4.setText(subjectSyncArrayList.get(i).getMaxScores().get(3)+"");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0]+"");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1]+"");
                    ETuserscore3.setText(subjectsmain.get(i).getMarks()[2]+"");
                    ETuserscore4.setText(subjectsmain.get(i).getMarks()[3]+"");
                    break;
                }
                case 5:{
                    TextView TVtestname1=findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0)+"");
                    TextView TVmaxscore1=findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0)+"");
                    TextView TVtestname2=findViewById(R.id.TVtestname2);
                    TVtestname2.setText(subjectSyncArrayList.get(i).getTestNames().get(1)+"");
                    TextView TVmaxscore2=findViewById(R.id.TVmaxscore2);
                    TVmaxscore2.setText(subjectSyncArrayList.get(i).getMaxScores().get(1)+"");
                    TextView TVtestname3=findViewById(R.id.TVtestname3);
                    TVtestname3.setText(subjectSyncArrayList.get(i).getTestNames().get(2)+"");
                    TextView TVmaxscore3=findViewById(R.id.TVmaxscore3);
                    TVmaxscore3.setText(subjectSyncArrayList.get(i).getMaxScores().get(2)+"");
                    TextView TVtestname4=findViewById(R.id.TVtestname4);
                    TVtestname4.setText(subjectSyncArrayList.get(i).getTestNames().get(3)+"");
                    TextView TVmaxscore4=findViewById(R.id.TVmaxscore4);
                    TVmaxscore4.setText(subjectSyncArrayList.get(i).getMaxScores().get(3)+"");
                    TextView TVtestname5=findViewById(R.id.TVtestname5);
                    TVtestname5.setText(subjectSyncArrayList.get(i).getTestNames().get(4)+"");
                    TextView TVmaxscore5=findViewById(R.id.TVmaxscore5);
                    TVmaxscore5.setText(subjectSyncArrayList.get(i).getMaxScores().get(4)+"");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0]+"");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1]+"");
                    ETuserscore3.setText(subjectsmain.get(i).getMarks()[2]+"");
                    ETuserscore4.setText(subjectsmain.get(i).getMarks()[3]+"");
                    break;
                }
            }
        }
    }

    //This Function is used to set up subjects for an eligible user when he signs in for the 1st time.
    private ArrayList<Subject> InitialiseSub() {
        ArrayList<Subject> subjects=new ArrayList<>();
        if(Eligible==1) {
            for (int i = 0; i < subjectSyncArrayList.size(); i++)
                subjects.add(new Subject(subjectSyncArrayList.get(i).getSub_name()));
            Log.d(TAG, "InitialiseSub: Adding subjects from synced list to local list");
        }
        else {
            subjects.add(new Subject("Enter your"));
            subjects.add(new Subject("subjects"));
        }
        return subjects;
    }

    //This function is used to setup the PieChart.
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

    //This is the interface method, it receives an index and the sets DetailFrag for that object from ArrayList.
    @Override
    public void onItemClicked(final int index) {
        indexmain=index;
        setdetailfrag(index);
    }

    private void OpenPopup() {
        PopupDiaogue popupDiaogue= new PopupDiaogue();
        popupDiaogue.show(getSupportFragmentManager(),"EditAttendance Popup");
    }

    //This is an interface method, it receives info from the popup and sets it accordingly.
    @Override
    public void applytexts(Integer classesattended, Integer totalclasses) {
        subjectsmain.get(indexmain).setPresent(classesattended);
        subjectsmain.get(indexmain).setTotaldays(totalclasses);
        subjectsmain.get(indexmain).calculatepercent();
        setdetailfragnoback(indexmain);
        FragmentManager fm = getSupportFragmentManager();
        final ListFrag listFrag = (ListFrag) fm.findFragmentById(R.id.list_frag);
        listFrag.myadapter.notifyDataSetChanged();
    }

    //This function is used to set up the detail frag when an item is clicked on list frag.
    public void setdetailfrag(final int index){
        Log.d("Inonitemclicked","at beginning");
        addDataset(pieChart, subjectsmain, index);//setting up pie chart
        //now setting up detail frag.

        FragmentManager fm = getSupportFragmentManager();
        DetailFrag detailFrag = (DetailFrag) fm.findFragmentById(R.id.detail_frag);
        final ListFrag listFrag = (ListFrag) fm.findFragmentById(R.id.list_frag);

        UpdateViewVisibility(index);
        TextView TVsubjectnameDF;
        TextView TVattendancefraction;
        Button BTNeditattendance;
        TVattendancefraction=findViewById(R.id.TVAttendanceFraction);
        TVsubjectnameDF=findViewById(R.id.TVSubjectNameDF);
        TVsubjectnameDF.setText(subjectsmain.get(index).getSub_name());
        TextView TVGPAforcastDF=findViewById(R.id.TVGPAforcastDF);
        TVGPAforcastDF.setText("Press Calculate to know your GPA.");
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

        Button BTNcalculateGPA=findViewById(R.id.BTNcalculateGPA);
        BTNcalculateGPA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateGPA(index);
                listFrag.myadapter.notifyDataSetChanged();
            }
        });

        if(findViewById(R.id.layout_portrait)!=null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (detailFrag != null) {
                ft.show(detailFrag);
            }
            if (listFrag != null)
                ft.hide(listFrag);
            ft.commit();
            ft.addToBackStack(null);
        }
    }

    //This function does GPA calculation and flashes the result on screen
    public void calculateGPA(Integer i){
        Double sum=0.0,Tsum=0.0;
        ArrayList<Integer> numArrayList= new ArrayList<>();
        EditText ETuserscore1=findViewById(R.id.ETuserscore1);
        EditText ETuserscore2=findViewById(R.id.ETuserscore2);
        EditText ETuserscore3=findViewById(R.id.ETuserscore3);
        EditText ETuserscore4=findViewById(R.id.ETuserscore4);
        EditText ETuserscore5=findViewById(R.id.ETuserscore5);
        if(i<subjectSyncArrayList.size()) {
            switch (subjectSyncArrayList.get(i).getNoOfTests()) {
                case 1: {
                    if (ETuserscore1.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0)) {
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    } else {
                        subjectsmain.get(i).getMarks()[0] = Integer.parseInt(ETuserscore1.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore1.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if(GPA > 10.0)
                            GPA = 10.0;
                        String returns = "Current GPA is " + Double.toString(GPA);
                        TextView TVGPAforcastDF = findViewById(R.id.TVGPAforcastDF);
                        TVGPAforcastDF.setText(returns);
                        subjectsmain.get(i).setGPA(GPA);
                    }
                    break;
                }
                case 2: {
                    if (ETuserscore1.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0) ||
                            ETuserscore2.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore2.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(1))
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    else {
                        subjectsmain.get(i).getMarks()[0] = Integer.parseInt(ETuserscore1.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore1.getText().toString()));
                        subjectsmain.get(i).getMarks()[1] = Integer.parseInt(ETuserscore2.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore2.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if(GPA > 10.0)
                            GPA = 10.0;
                        String returns = "Current GPA is " + Double.toString(GPA);
                        TextView TVGPAforcastDF = findViewById(R.id.TVGPAforcastDF);
                        TVGPAforcastDF.setText(returns);
                        subjectsmain.get(i).setGPA(GPA);
                    }
                    break;
                }
                case 3: {
                    if (ETuserscore1.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0) ||
                            ETuserscore2.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore2.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(1) ||
                            ETuserscore3.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore3.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(2))
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    else {
                        subjectsmain.get(i).getMarks()[0] = Integer.parseInt(ETuserscore1.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore1.getText().toString()));
                        subjectsmain.get(i).getMarks()[1] = Integer.parseInt(ETuserscore2.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore2.getText().toString()));
                        subjectsmain.get(i).getMarks()[2] = Integer.parseInt(ETuserscore3.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore3.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if(GPA > 10.0)
                            GPA = 10.0;
                        String returns = "Current GPA is " + Double.toString(GPA);
                        TextView TVGPAforcastDF = findViewById(R.id.TVGPAforcastDF);
                        TVGPAforcastDF.setText(returns);
                        subjectsmain.get(i).setGPA(GPA);
                    }
                    break;
                }
                case 4: {
                    if (ETuserscore1.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0) ||
                            ETuserscore2.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore2.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(1) ||
                            ETuserscore3.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore3.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(2) ||
                            ETuserscore4.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore4.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(3))
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    else {
                        subjectsmain.get(i).getMarks()[0] = Integer.parseInt(ETuserscore1.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore1.getText().toString()));
                        subjectsmain.get(i).getMarks()[1] = Integer.parseInt(ETuserscore2.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore2.getText().toString()));
                        subjectsmain.get(i).getMarks()[2] = Integer.parseInt(ETuserscore3.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore3.getText().toString()));
                        subjectsmain.get(i).getMarks()[3] = Integer.parseInt(ETuserscore4.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore4.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if(GPA > 10.0)
                            GPA = 10.0;
                        String returns = "Current GPA is " + Double.toString(GPA);
                        TextView TVGPAforcastDF = findViewById(R.id.TVGPAforcastDF);
                        TVGPAforcastDF.setText(returns);
                        subjectsmain.get(i).setGPA(GPA);
                    }
                    break;
                }
                case 5: {
                    if (ETuserscore1.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0) ||
                            ETuserscore2.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore2.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(1) ||
                            ETuserscore3.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore3.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(2) ||
                            ETuserscore4.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore4.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(3) ||
                            ETuserscore5.getText().toString().equals("") ||
                            Integer.parseInt(ETuserscore5.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(4))
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    else {
                        subjectsmain.get(i).getMarks()[0] = Integer.parseInt(ETuserscore1.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore1.getText().toString()));
                        subjectsmain.get(i).getMarks()[1] = Integer.parseInt(ETuserscore2.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore2.getText().toString()));
                        subjectsmain.get(i).getMarks()[2] = Integer.parseInt(ETuserscore3.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore3.getText().toString()));
                        subjectsmain.get(i).getMarks()[3] = Integer.parseInt(ETuserscore4.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore4.getText().toString()));
                        subjectsmain.get(i).getMarks()[4] = Integer.parseInt(ETuserscore5.getText().toString());
                        numArrayList.add(Integer.parseInt(ETuserscore5.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if(GPA > 10.0)
                            GPA = 10.0;
                        String returns = "Current GPA is " + Double.toString(GPA);
                        TextView TVGPAforcastDF = findViewById(R.id.TVGPAforcastDF);
                        TVGPAforcastDF.setText(returns);
                        subjectsmain.get(i).setGPA(GPA);
                    }
                    break;
                }
            }
        }
        else
            Toast.makeText(this, "Forcast not available for this Subject.", Toast.LENGTH_SHORT).show();
    }

    //This function also sets up detail frag but without adding to the back stack.
    public void setdetailfragnoback(final int index){
        Log.d("Inonitemclicked","at beginning");
        addDataset(pieChart, subjectsmain, index);//setting up pie chart
        //now setting up detail frag.
        UpdateViewVisibility(index);
        TextView TVsubjectnameDF;
        TextView TVattendancefraction;
        Button BTNeditattendance;
        TVattendancefraction=findViewById(R.id.TVAttendanceFraction);
        TVsubjectnameDF=findViewById(R.id.TVSubjectNameDF);
        TVsubjectnameDF.setText(subjectsmain.get(index).getSub_name());
        BTNeditattendance=findViewById(R.id.BTNEditAttendance);
        TextView TVGPAforcastDF=findViewById(R.id.TVGPAforcastDF);
        TVGPAforcastDF.setText("Press Calculate to know your GPA.");
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

        Button BTNcalculateGPA=findViewById(R.id.BTNcalculateGPA);
        BTNcalculateGPA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //calculateGPA(index);
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
        }
    }// Dont forget to update this function as per setdetailfrag.

    //This function saves data locally.
    private void SaveData(){
        SharedPreferences SPSubjectsMain = Objects.requireNonNull(this).getSharedPreferences("SubjectsMainArrayList",MODE_PRIVATE);
        SharedPreferences.Editor editor=SPSubjectsMain.edit();
        Gson gson= new Gson ();
        String json=gson.toJson(subjectsmain);
        editor.putString("subjectslist",json);
        editor.apply();

        SharedPreferences SPSubjectsSync = Objects.requireNonNull(this).getSharedPreferences("SubjectsSyncArrayList",MODE_PRIVATE);
        SharedPreferences.Editor editor1=SPSubjectsSync.edit();
        Gson gson1= new Gson ();
        String json1=gson1.toJson(subjectSyncArrayList);
        editor1.putString("subjectssynclist",json1);
        editor1.apply();
        Log.d(TAG, "SaveData: ArrayLists saved!");
    }

    //This function loads data from the local storage.
    private ArrayList<Subject> LoadData(){
        ArrayList<Subject> subjectsmain;
        SharedPreferences SPSubjectsMain = Objects.requireNonNull(this).getSharedPreferences("SubjectsMainArrayList",MODE_PRIVATE);
        Gson gson= new Gson();
        String json = SPSubjectsMain.getString("subjectslist",null);
        Type type= new TypeToken<ArrayList<Subject>>(){}.getType();
        subjectsmain=gson.fromJson(json,type);
        if(subjectsmain==null){
            subjectsmain=new ArrayList<>();
        }
        return subjectsmain;
    }

    //This function is used to set up SubjectsSync for an eligible user.
    private ArrayList<SubjectSync> SyncData() {
        ArrayList<SubjectSync> list=new ArrayList<>();
        if(Eligible==1) {
            SharedPreferences SPSubjectsSync = Objects.requireNonNull(this).getSharedPreferences("SubjectsSyncArrayList",MODE_PRIVATE);
            Gson gson= new Gson();
            String json = SPSubjectsSync.getString("subjectssynclist",null);
            Type type= new TypeToken<ArrayList<SubjectSync>>(){}.getType();
            list=gson.fromJson(json,type);
        }
        if(list==null)
            list=new ArrayList<>();
        return list;
    }

    @Override
    protected void onPause() {
        SaveData();
        super.onPause();
    }
}





            /*ArrayList<Integer> topperScore=new ArrayList<>();
            topperScore.add(41);
            ArrayList<String> testName=new ArrayList<>();
            testName.add("C1");
            ArrayList<Integer> maxScore = new ArrayList<>();
            maxScore.add(12);
            ArrayList<Integer> ScoresToConsider=new ArrayList<>();
            ScoresToConsider.add(1);
            ScoresToConsider.add(-1);
            ScoresToConsider.add(-1);
            ScoresToConsider.add(-1);
            ScoresToConsider.add(-1);
            list.add(new SubjectSync("DST", 1, "Enter marks for C1", topperScore, testName, maxScore,ScoresToConsider));
            topperScore.add(11);
            testName.add("Quiz-2");
            maxScore.add(16);
            ArrayList<Integer> ScoresToConsider2=new ArrayList<>();
            ScoresToConsider2.add(1);
            ScoresToConsider2.add(1);
            ScoresToConsider2.add(-1);
            ScoresToConsider2.add(-1);
            ScoresToConsider2.add(-1);
            list.add(new SubjectSync("UMC", 2, "Enter marks for C1, Quiz-2", topperScore, testName, maxScore,ScoresToConsider2));*/

