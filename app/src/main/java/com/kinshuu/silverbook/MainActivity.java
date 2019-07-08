package com.kinshuu.silverbook;

import android.content.Intent;
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

import com.firebase.ui.auth.AuthUI;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SubjectAdapter.itemclicked, PopupDiaogue.PopupDialogueListener {
    //request codes for ActivityResult
    private static final int RC_SIGN_IN = 1;
    private static final int RC_USER_PREF=23;

    //declaring all views.
    PieChart pieChart;//for setting up PieChart in detail frag.

    //variables for runtime manipulation.
    Integer indexmain=0;
    Integer firsttime;
    Integer gotpreference=0;
    ArrayList<Subject> subjectsmain=new ArrayList<>();//Main local Subjects ArrayList
    ArrayList<SubjectSync> subjectSyncArrayList=new ArrayList<>();//ArrayList synced from firebase.

    //For managing userdata.
    Integer Signed_In=0;
    Integer Eligible=0;
    Integer YearOfJoining=0;
    String College="null";
    String Branch="null";
    private String mUsername="ANONYMOUS";
    String TAG="MyLOGS";

    //Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference msubjectsDatabaseReference;
    private ChildEventListener mChildEventListener;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthStateListener;

    //two major fragments to be used.
    ListFrag listFrag;
    DetailFrag detailFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starting OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listFrag=new ListFrag();
        detailFrag = new DetailFrag();
        if(findViewById(R.id.layout_portrait)==null) {
            getSupportFragmentManager().beginTransaction().add(R.id.list_frag_cont, listFrag,"listfrag").commit();
            getSupportFragmentManager().beginTransaction().add(R.id.detail_frag_cont, detailFrag,"detailfrag").commit();
            getSupportFragmentManager().executePendingTransactions();
            listFrag=(ListFrag)getSupportFragmentManager().findFragmentByTag("listfrag");
            detailFrag=(DetailFrag)getSupportFragmentManager().findFragmentByTag("detailfrag");
        }
        else{
            getSupportFragmentManager().beginTransaction().add(R.id.fragCont_portrait, listFrag, "listfrag").commit();
            getSupportFragmentManager().executePendingTransactions();
            listFrag=(ListFrag)getSupportFragmentManager().findFragmentByTag("listfrag");
        }
        //Getting user preferences.
        SharedPreferences faveditor=getSharedPreferences("com.kinshuu.silverbook.ft",MODE_PRIVATE);
        firsttime=faveditor.getInt("FT",1);
        faveditor=getSharedPreferences("com.kinshuu.silverbook.ref",MODE_PRIVATE);
        College=faveditor.getString("College","null");
        Toast.makeText(this, "College is "+College, Toast.LENGTH_SHORT).show();
        Branch=faveditor.getString("Branch","null");
        Toast.makeText(this, "Branch is "+Branch, Toast.LENGTH_SHORT).show();
        YearOfJoining=faveditor.getInt("YearOfJoining",0);

        if(College.equals("null")||Branch.equals("null")||YearOfJoining==0) {
            Log.d(TAG, "onCreate: Going to take preference from user!");
            Intent intent = new Intent(MainActivity.this, com.kinshuu.silverbook.UserOrientation.class);
            startActivityForResult(intent, RC_USER_PREF);
            gotpreference=0;
        }

        if(YearOfJoining>2017&&College.equals("IIIT-A"))
            Eligible=1;

        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mFirebaseAuth=FirebaseAuth.getInstance();
        msubjectsDatabaseReference=mFirebaseDatabase.getReference().child(College).child(YearOfJoining.toString()).child(Branch);
        Log.d(TAG, "onCreate: Present Database Reference is");
        Log.d(TAG, "onCreate: College is "+College);
        Log.d(TAG, "onCreate: Branch is "+Branch);
        Log.d(TAG, "onCreate: YearOfJoining is "+YearOfJoining);

        subjectSyncArrayList=SyncData();
        subjectsmain = LoadData();

        //Following code sends data to the list frag, what's important is Activity's OnCreate finishes before Fragments OnActivityCreated starts.
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("arraylist",subjectsmain);
        bundle.putInt("elegible",Eligible);
        if (listFrag != null) {
            listFrag.getArgs(bundle);
        }
        else
            Log.d(TAG, "onCreate: List frag is null");
        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user!=null){
                    //user is signed in.
                    Toast.makeText(MainActivity.this, "Welcome to the App you Son of a Gun", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onAuthStateChanged: User is signed in");
                    OnSignedInInitialise(user.getDisplayName());
                }
                else{
                    //user is signed out.
                    Log.d(TAG, "onAuthStateChanged: User is signed out.");
                    //OnSignedOutInitialise();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)//Experiment with this.
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        Log.d(TAG, "onCreate: OnCreate ends");
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
            TVGPAhead.setText("GPA forcast is only available for IIIT-A batch 2k18 or later.");
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
        Log.d(TAG, "InitialiseSub: Starts");
        ArrayList<Subject> subjects=new ArrayList<>();
        if(Eligible==1) {
            for (int i = 0; i < subjectSyncArrayList.size(); i++)
                subjects.add(new Subject(subjectSyncArrayList.get(i).getSub_name()));
            Log.d(TAG, "InitialiseSub: Adding subjects from synced list to local list");
        }
        else {
            subjects.add(new Subject("Enter your"));
            subjects.add(new Subject("subjects"));
            Log.d(TAG, "InitialiseSub: User not eligible therefore loading 2 default subjects.");
        }
        Log.d(TAG, "InitialiseSub: Exitting InitialiseSub");
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
        listFrag.myadapter.notifyDataSetChanged();
    }

    //This function is used to set up the detail frag when an item is clicked on list frag.
    public void setdetailfrag(final int index){

        if(findViewById(R.id.layout_portrait)!=null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragCont_portrait,detailFrag,"detailfrag");
            ft.commit();
            ft.addToBackStack(null);
            getSupportFragmentManager().executePendingTransactions();
        }
        else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.list_frag_cont,listFrag,"listfrag");
            ft.replace(R.id.detail_frag_cont,detailFrag,"detailfrag");
            ft.commit();
            ft.addToBackStack(null);
            getSupportFragmentManager().executePendingTransactions();
        }
        addDataset(pieChart, subjectsmain, index);//setting up pie chart
        //now setting up detail frag.
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
            if (detailFrag != null) {
                ft.show(detailFrag);
            }
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
        Log.d(TAG, "SaveData: ArrayLists saved!+ size of synclist is "+subjectSyncArrayList.size());
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
        Log.d(TAG, "LoadData: Loaded Subjects main from disk and size is "+subjectsmain.size());
        return subjectsmain;
    }

    //This function is used to set up SubjectsSync for an eligible user.
    private ArrayList<SubjectSync> SyncData() {
        Log.d(TAG, "SyncData: Eligible is "+Eligible);
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
        Log.d(TAG, "SyncData: Data loaded from disk and size of SubjectSync is "+list.size());
        return list;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: OnPause Starts");
        SaveData();
        super.onPause();
        if(mAuthStateListener!=null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            Log.d(TAG, "onPause: Detached AuthStateListener");
        }
        if(mChildEventListener!=null) {
            msubjectsDatabaseReference.removeEventListener(mChildEventListener);
            Log.d(TAG, "onPause: DetachedChildEventListener");
            mChildEventListener = null;
        }
        Log.d(TAG, "onPause: Exitting OnPause.");
    }

    @Override
    protected void onResume(){
        Log.d(TAG, "onResume: OnResume starts");
        super.onResume();
        if(gotpreference==1||firsttime==0) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
            Log.d(TAG, "onResume: Attached AuthStateListener");
        }
        //setting up detail frag to 0th element if phone is in landscape mode.
        if(findViewById(R.id.layout_portrait)==null){
            setdetailfrag(0);
        }
        Log.d(TAG, "onResume: OnResume Ends");
    }


    private void OnSignedOutInitialise() {
        Signed_In=0;
        mUsername="ANONYMOUS";
        if(mChildEventListener!=null) {
            msubjectsDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void OnSignedInInitialise(String displayName) {
        Log.d(TAG, "OnSignedInInitialise: Starts");
        mUsername=displayName;
        attachDatabaseReadListener();
        Log.d(TAG, "OnSignedInInitialise: Attached DatabaseListener");
        Log.d(TAG, "OnSignedInInitialise: Ends");
    }

    private void attachDatabaseReadListener() {
        //following code is to manage the firebase sync data.
        Log.d(TAG, "attachDatabaseReadListener: Starts");
        final ArrayList<SubjectSync> subjectSyncArrayListtemp = new ArrayList<>();
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    SubjectSync subject = dataSnapshot.getValue(SubjectSync.class);
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
                    Toast.makeText(MainActivity.this, "Data Updated :)", Toast.LENGTH_SHORT).show();
                    subjectSyncArrayList = subjectSyncArrayListtemp;
                    Log.d(TAG, "onDataChange: Data Synced from cloud");
                    if (firsttime == 1) {
                        //changing firsttime to 0.
                        SharedPreferences.Editor faveditor = getSharedPreferences("com.kinshuu.silverbook.ft", MODE_PRIVATE).edit();
                        faveditor.putInt("FT", 0);
                        faveditor.apply();
                        Log.d(TAG, "onDataChange: Saved shared preference");
                        Log.d(TAG, "onDataChange: Firsttime==1 and thus going to InitialiseSub");
                        subjectsmain = InitialiseSub();
                        Log.d(TAG, "onDataChange: initialised subjects main");
                        recreate();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: In ActivityResult.");
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_USER_PREF){
            Log.d(TAG, "onActivityResult: Recieved User Preference!");
            if(resultCode==RESULT_OK){
                Branch=data.getStringExtra("Branch");
                College=data.getStringExtra("College");
                YearOfJoining=data.getIntExtra("Batch",0);
                msubjectsDatabaseReference=mFirebaseDatabase.getReference().child(College).child(YearOfJoining.toString()).child(Branch);
                SharedPreferences.Editor faveditor = getSharedPreferences("com.kinshuu.silverbook.ref", MODE_PRIVATE).edit();
                faveditor.putString("Branch", Branch);
                faveditor.putString("College",College);
                faveditor.putInt("YearOfJoining",YearOfJoining);
                faveditor.apply();
                gotpreference=1;
                if(YearOfJoining>2017&&College.equals("IIIT-A"))
                    Eligible=1;
                Log.d(TAG, "In MainActivity class College, Branch and Batch recieved are "+College+","+Branch+","+YearOfJoining);
            }
            if(resultCode==RESULT_CANCELED){
                Toast.makeText(this, "Cannot work until you provide given info", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        if(requestCode==RC_SIGN_IN){
            if(resultCode==RESULT_CANCELED) {
                SharedPreferences.Editor faveditor = getSharedPreferences("com.kinshuu.silverbook.ref", MODE_PRIVATE).edit();
                faveditor.putString("Branch", "null");
                faveditor.putString("College", "null");
                faveditor.putInt("YearOfJoining", 0);
                faveditor.apply();
                Toast.makeText(this, "Cannot work until you Sign-In", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        Log.d(TAG, "onActivityResult: Exiting ActivityResult");
    }
}