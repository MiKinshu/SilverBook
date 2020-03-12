package com.kinshuu.silverbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.navigation.NavigationView;
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
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SubjectAdapter.itemclicked, PopupDiaogue.PopupDialogueListener, NavigationView.OnNavigationItemSelectedListener {
    //request codes for ActivityResult
    private static final int RC_SIGN_IN = 1;
    private static final int RC_USER_PREF = 23;

    //declaring all views.
    PieChart pieChart;//for setting up PieChart in detail frag.
    TextView TVnavheadname;

    //variables for runtime manipulation.
    Integer indexmain = 0;
    Integer firsttime;
    ArrayList<Subject> subjectsmain = new ArrayList<>();//Main local Subjects ArrayList
    ArrayList<SubjectSync> subjectSyncArrayList = new ArrayList<>();//ArrayList synced from firebase.
    ArrayList<com.kinshuu.silverbook.Log> LogArrayList = new ArrayList<>();//Throughout the code, this list is handled with subjectsmain.

    //For managing userdata.
    Integer Eligible = 0;
    Integer YearOfJoining = 0;
    String College = "null";
    String Branch = "null";
    private String mUsername = "ANONYMOUS";
    String TAG = "MyLOGS";

    //Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference msubjectsDatabaseReference;
    private ChildEventListener mChildEventListener;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthStateListener;

    //two major fragments to be used.
    ListFrag listFrag;
    DetailFrag detailFrag;
    BlankFragment blankFrag;

    //for navigation drawer
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    View header;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starting OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.navigation_drawer);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        listFrag = new ListFrag();
        detailFrag = new DetailFrag();
        blankFrag = new BlankFragment();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "onAuthStateChanged: In listener");
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    //user is signed in.
                    Log.d(TAG, "onAuthStateChanged: User is signed in");
                    getUserPreference();
                    OnSignedInInitialise(user.getDisplayName());
                    subjectsmain = LoadData(); //getting data from disk.
                    subjectSyncArrayList = SyncData();
                    LogArrayList = getlog(); // getting log from disk.
                    sendDataToListFrag();
                    SetUpFragments(); //this function sets up the fragments according to screen orientation.

                } else {
                    //user is signed out.
                    Log.d(TAG, "onAuthStateChanged: User is signed out.");

                    //Getting user preferences because a signed out user implies that no preference have been stored yet.
                    getUserPreference();
                }
            }
        };
        Log.d(TAG, "onCreate: OnCreate ends");
    }

    private void sendDataToListFrag() {
        //Following code sends data to the list frag, what's important is Activity's OnCreate finishes before Fragments OnActivityCreated starts.
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("arraylist", subjectsmain);
        bundle.putInt("elegible", Eligible);
        bundle.putParcelableArrayList("loglist", LogArrayList);
        bundle.putInt("size", subjectSyncArrayList.size());
        if (listFrag != null) {
            listFrag.getArgs(bundle);
        } else
            Log.d(TAG, "onCreate: List frag is null");
    }

    private void getUserPreference() {
        Log.d(TAG, "getUserPreference: Loading user preference");
        SharedPreferences faveditor = getSharedPreferences("com.kinshuu.silverbook.ref", MODE_PRIVATE);
        firsttime = faveditor.getInt("FT", 1);
        Log.d(TAG, "onCreate: firsttime recieved is " + firsttime);
        faveditor = getSharedPreferences("com.kinshuu.silverbook.ref", MODE_PRIVATE);
        College = faveditor.getString("College", "null");
        Branch = faveditor.getString("Branch", "null");
        YearOfJoining = faveditor.getInt("YearOfJoining", 0);

        if (YearOfJoining > 2017 && College.equals("IIIT-A"))
            Eligible = 1;
        msubjectsDatabaseReference = mFirebaseDatabase.getReference().child(College).child(YearOfJoining.toString()).child(Branch);

        //The following code is called only if the user is signed out. i.e. signing in for the first time.
        if (College.equals("null") || Branch.equals("null") || YearOfJoining == 0) {
            Log.d(TAG, "getUserPreference: User preference not found.");
            Log.d(TAG, "getUserPreference: Going to take preference from user!");
            Intent intent = new Intent(MainActivity.this, com.kinshuu.silverbook.UserOrientation.class);
            startActivityForResult(intent, RC_USER_PREF);
        }
    }

    private void SetUpFragments() {
        Log.d(TAG, "SetUpFragments: Starting");

        SetUpNavigationHeader();
        navigationView.setCheckedItem(R.id.nav_home);

        if (findViewById(R.id.layout_portrait) == null) {
            if (subjectsmain.size() == 0) {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.list_frag_cont, blankFrag, "blankfrag").commit();
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.detail_frag_cont, new BlankFragment(), "blankfragdetail").commit();
            }
            else{
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.list_frag_cont, listFrag, "listfrag").commit();
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.detail_frag_cont, detailFrag, "detailfrag").commit();
                getSupportFragmentManager().executePendingTransactions();
                listFrag = (ListFrag) getSupportFragmentManager().findFragmentByTag("listfrag");
                detailFrag = (DetailFrag) getSupportFragmentManager().findFragmentByTag("detailfrag");
                if(subjectsmain!=null)
                    setdetailfrag(0);
            }
        } else {
            if (subjectsmain.size() == 0) {
                Log.d(TAG, "onResume: Now inflating blannkfrag layout");
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.fragCont_portrait, blankFrag, "blankfrag").commit();
            }
            else{
                Log.d(TAG, "onResume: Now inflating listfrag layout.");
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.fragCont_portrait, listFrag, "listfrag").commit();
                getSupportFragmentManager().executePendingTransactions();
                listFrag = (ListFrag) getSupportFragmentManager().findFragmentByTag("listfrag");
            }
        }
        Log.d(TAG, "SetUpFragments: Exiting.");
    }

    private void SetUpNavigationHeader() {
        TVnavheadname = header.findViewById(R.id.TVnavheadname);
        if (!mUsername.equals("ANONYMOUS")) {
            TVnavheadname.setText("Hi! " + mUsername);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //This function is used to update the visibility of views as per the constraints.
    private void UpdateViewVisibility(int i) {
        Group group1, group2, group3, group4, group5, groupGPI;
        TextView TVGPAhead;
        group1 = findViewById(R.id.group1);
        group2 = findViewById(R.id.group2);
        group3 = findViewById(R.id.group3);
        group4 = findViewById(R.id.group4);
        group5 = findViewById(R.id.group5);
        groupGPI = findViewById(R.id.groupGPI);
        TVGPAhead = findViewById(R.id.TVGPAhead);
        EditText ETuserscore1 = findViewById(R.id.ETuserscore1);
        EditText ETuserscore2 = findViewById(R.id.ETuserscore2);
        EditText ETuserscore3 = findViewById(R.id.ETuserscore3);
        EditText ETuserscore4 = findViewById(R.id.ETuserscore4);
        TextView TVtestname4 = findViewById(R.id.TVtestname4);
        TextView TVmaxscore4 = findViewById(R.id.TVmaxscore4);


        if (Eligible == 0) {
            group1.setVisibility(View.GONE);
            group2.setVisibility(View.GONE);
            group3.setVisibility(View.GONE);
            group4.setVisibility(View.INVISIBLE);
            group5.setVisibility(View.GONE);
            groupGPI.setVisibility(View.GONE);
            TVGPAhead.setText("At the moment, GPA forecast is only available for IIIT-A batch 2k18 or later.");
        } else if (i >= subjectSyncArrayList.size()) {
            group1.setVisibility(View.GONE);
            group2.setVisibility(View.GONE);
            group3.setVisibility(View.GONE);
            group4.setVisibility(View.GONE);
            group5.setVisibility(View.GONE);
            groupGPI.setVisibility(View.GONE);
            TVGPAhead.setText("forecast not available for this subject");
        } else {
            TextView TVtestnames = findViewById(R.id.TVtestnames);
            TVtestnames.setText(subjectSyncArrayList.get(i).getExamName());
            switch (subjectSyncArrayList.get(i).getNoOfTests()) {
                case 0: {
                    group1.setVisibility(View.GONE);
                    group2.setVisibility(View.GONE);
                    group3.setVisibility(View.GONE);
                    group4.setVisibility(View.GONE);
                    group5.setVisibility(View.GONE);
                    groupGPI.setVisibility(View.GONE);
                    TVGPAhead.setText("GPA forecast would be visible once the results for the first test are out :)");
                    break;
                }

                case 1: {
                    group2.setVisibility(View.GONE);
                    group3.setVisibility(View.GONE);
                    group4.setVisibility(View.GONE);
                    group5.setVisibility(View.GONE);
                    TextView TVtestname1 = findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0) + "");
                    TextView TVmaxscore1 = findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0) + "");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0] + "");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1] + "");
                    ETuserscore3.setText(subjectsmain.get(i).getMarks()[2] + "");
                    ETuserscore4.setText(subjectsmain.get(i).getMarks()[3] + "");
                    break;
                }
                case 2: {
                    group3.setVisibility(View.INVISIBLE);
                    group3.requestLayout();
                    group4.setVisibility(View.INVISIBLE);
                    group5.setVisibility(View.INVISIBLE);
                    TextView TVtestname1 = findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0) + "");
                    TextView TVmaxscore1 = findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0) + "");
                    TextView TVtestname2 = findViewById(R.id.TVtestname2);
                    TVtestname2.setText(subjectSyncArrayList.get(i).getTestNames().get(1) + "");
                    TextView TVmaxscore2 = findViewById(R.id.TVmaxscore2);
                    TVmaxscore2.setText(subjectSyncArrayList.get(i).getMaxScores().get(1) + "");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0] + "");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1] + "");
                    ETuserscore3.setVisibility(View.GONE);
                    ETuserscore4.setVisibility(View.GONE);
                    TVtestname4.setVisibility(View.GONE);
                    TVmaxscore4.setVisibility(View.GONE);

                    Log.d(TAG, "UpdateViewVisibility: Updated visibility");
                    break;
                }
                case 3: {
                    group4.setVisibility(View.GONE);
                    group5.setVisibility(View.GONE);
                    TextView TVtestname1 = findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0) + "");
                    TextView TVmaxscore1 = findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0) + "");
                    TextView TVtestname2 = findViewById(R.id.TVtestname2);
                    TVtestname2.setText(subjectSyncArrayList.get(i).getTestNames().get(1) + "");
                    TextView TVmaxscore2 = findViewById(R.id.TVmaxscore2);
                    TVmaxscore2.setText(subjectSyncArrayList.get(i).getMaxScores().get(1) + "");
                    TextView TVtestname3 = findViewById(R.id.TVtestname3);
                    TVtestname3.setText(subjectSyncArrayList.get(i).getTestNames().get(2) + "");
                    TextView TVmaxscore3 = findViewById(R.id.TVmaxscore3);
                    TVmaxscore3.setText(subjectSyncArrayList.get(i).getMaxScores().get(2) + "");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0] + "");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1] + "");
                    ETuserscore3.setText(subjectsmain.get(i).getMarks()[2] + "");
                    ETuserscore4.setText(subjectsmain.get(i).getMarks()[3] + "");
                    break;
                }
                case 4: {
                    group5.setVisibility(View.GONE);
                    TextView TVtestname1 = findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0) + "");
                    TextView TVmaxscore1 = findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0) + "");
                    TextView TVtestname2 = findViewById(R.id.TVtestname2);
                    TVtestname2.setText(subjectSyncArrayList.get(i).getTestNames().get(1) + "");
                    TextView TVmaxscore2 = findViewById(R.id.TVmaxscore2);
                    TVmaxscore2.setText(subjectSyncArrayList.get(i).getMaxScores().get(1) + "");
                    TextView TVtestname3 = findViewById(R.id.TVtestname3);
                    TVtestname3.setText(subjectSyncArrayList.get(i).getTestNames().get(2) + "");
                    TextView TVmaxscore3 = findViewById(R.id.TVmaxscore3);
                    TVmaxscore3.setText(subjectSyncArrayList.get(i).getMaxScores().get(2) + "");
                    TVtestname4.setText(subjectSyncArrayList.get(i).getTestNames().get(3) + "");
                    TVmaxscore4.setText(subjectSyncArrayList.get(i).getMaxScores().get(3) + "");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0] + "");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1] + "");
                    ETuserscore3.setText(subjectsmain.get(i).getMarks()[2] + "");
                    ETuserscore4.setText(subjectsmain.get(i).getMarks()[3] + "");
                    break;
                }
                case 5: {
                    Log.d(TAG, "UpdateViewVisibility: in case 5");
                    TextView TVtestname1 = findViewById(R.id.TVtestname1);
                    TVtestname1.setText(subjectSyncArrayList.get(i).getTestNames().get(0) + "");
                    TextView TVmaxscore1 = findViewById(R.id.TVmaxscore1);
                    TVmaxscore1.setText(subjectSyncArrayList.get(i).getMaxScores().get(0) + "");
                    TextView TVtestname2 = findViewById(R.id.TVtestname2);
                    TVtestname2.setText(subjectSyncArrayList.get(i).getTestNames().get(1) + "");
                    TextView TVmaxscore2 = findViewById(R.id.TVmaxscore2);
                    TVmaxscore2.setText(subjectSyncArrayList.get(i).getMaxScores().get(1) + "");
                    TextView TVtestname3 = findViewById(R.id.TVtestname3);
                    TVtestname3.setText(subjectSyncArrayList.get(i).getTestNames().get(2) + "");
                    TextView TVmaxscore3 = findViewById(R.id.TVmaxscore3);
                    TVmaxscore3.setText(subjectSyncArrayList.get(i).getMaxScores().get(2) + "");
                    TVtestname4.setText(subjectSyncArrayList.get(i).getTestNames().get(3) + "");
                    TVmaxscore4.setText(subjectSyncArrayList.get(i).getMaxScores().get(3) + "");
                    ETuserscore1.setText(subjectsmain.get(i).getMarks()[0] + "");
                    ETuserscore2.setText(subjectsmain.get(i).getMarks()[1] + "");
                    ETuserscore3.setText(subjectsmain.get(i).getMarks()[2] + "");
                    ETuserscore4.setText(subjectsmain.get(i).getMarks()[3] + "");
                    break;
                }
            }
        }
        Log.d(TAG, "UpdateViewVisibility: exitting function");
    }

    //This Function is used to set up subjects for an eligible user when he signs in for the 1st time.
    private ArrayList<Subject> InitialiseSub() {
        Log.d(TAG, "InitialiseSub: Starts");
        ArrayList<Subject> subjects = new ArrayList<>();
        if (YearOfJoining != 0) {
            for (int i = 0; i < subjectSyncArrayList.size(); i++)
                subjects.add(new Subject(subjectSyncArrayList.get(i).getSub_name()));
            Log.d(TAG, "InitialiseSub: Adding subjects from synced list to local list");
        }
        Log.d(TAG, "YearOfJoining is " + YearOfJoining);
        if (YearOfJoining == 1) {
            Toast.makeText(this, "Add you subjects from navigation drawer.", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "InitialiseSub: Exiting InitialiseSub");
        return subjects;
    }

    //This function is used to setup the PieChart.
    private void addDataset(PieChart pieChart, ArrayList<Subject> subjectsmain, int index) {// This method sets up the piechart
        pieChart = findViewById(R.id.piechart);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);
        ArrayList<PieEntry> yenteries = new ArrayList<>();
        ArrayList<String> xenteries = new ArrayList<>();
        String[] xdata = {"absent", "present"};
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.Pieabsent));
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.Piepresent));
        int absent = (subjectsmain.get(index).getTotaldays()) - subjectsmain.get(index).getPresent();
        yenteries.add(new PieEntry(absent, "Absent"));
        yenteries.add(new PieEntry(subjectsmain.get(index).getPresent(), "Present"));

        for (int i = 0; i < 2; i++) {
            xenteries.add(xdata[i]);
        }
        Description description = pieChart.getDescription();
        description.setText("Your Attendance in days");
        PieDataSet pieDataSet = new PieDataSet(yenteries, "(In days)");
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
        indexmain = index;
        setdetailfrag(index);
    }

    private void OpenPopup() {
        PopupDiaogue popupDiaogue = new PopupDiaogue();
        popupDiaogue.show(getSupportFragmentManager(), "EditAttendance Popup");
    }

    //This is an interface method, it receives info from the popup and sets it accordingly.
    @Override
    public void applytexts(Integer classesattended, Integer totalclasses) {
        subjectsmain.get(indexmain).setPresent((int) classesattended);
        subjectsmain.get(indexmain).setTotaldays((int) totalclasses);
        subjectsmain.get(indexmain).calculatepercent();
        UpdateDetailFrag(indexmain);//Update Detail Fragment.
        listFrag.myadapter.notifyDataSetChanged();//Update List Fragment.
        com.kinshuu.silverbook.Log log = new com.kinshuu.silverbook.Log(Calendar.getInstance().getTime().toString().split("G")[0]);
        log.setAction(subjectsmain.get(indexmain).getSub_name() + " : Present changed to " + classesattended + " and total classes changed to " + totalclasses + " on ");
        LogArrayList.add(log);
    }

    //This function is used to set up the detail frag when an item is clicked on list frag.
    public void setdetailfrag(final int index) {
        if (findViewById(R.id.layout_portrait) != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left);
            ft.replace(R.id.fragCont_portrait, detailFrag, "detailfrag");
            ft.commit();
            ft.addToBackStack(null);
            getSupportFragmentManager().executePendingTransactions();
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left);
            ft.replace(R.id.list_frag_cont, listFrag, "listfrag");
            ft.replace(R.id.detail_frag_cont, detailFrag, "detailfrag");
            ft.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
        UpdateDetailFrag(index);
    }

    //This function does GPA calculation and flashes the result on screen
    public void calculateGPA(Integer i) {
        Double sum = 0.0, Tsum = 0.0;
        ArrayList<Float> numArrayList = new ArrayList<>();
        EditText ETuserscore1 = findViewById(R.id.ETuserscore1);
        EditText ETuserscore2 = findViewById(R.id.ETuserscore2);
        EditText ETuserscore3 = findViewById(R.id.ETuserscore3);
        EditText ETuserscore4 = findViewById(R.id.ETuserscore4);
        if (i < subjectSyncArrayList.size()) {
            switch (subjectSyncArrayList.get(i).getNoOfTests()) {
                case 1: {
                    if (ETuserscore1.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0)) {
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    } else {
                        subjectsmain.get(i).getMarks()[0] = Float.parseFloat(ETuserscore1.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore1.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if (GPA > 10.0)
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
                            Float.parseFloat(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0) ||
                            ETuserscore2.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore2.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(1))
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    else {
                        subjectsmain.get(i).getMarks()[0] = Float.parseFloat(ETuserscore1.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore1.getText().toString()));
                        subjectsmain.get(i).getMarks()[1] = Float.parseFloat(ETuserscore2.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore2.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if (GPA > 10.0)
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
                            Float.parseFloat(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0) ||
                            ETuserscore2.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore2.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(1) ||
                            ETuserscore3.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore3.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(2))
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    else {
                        subjectsmain.get(i).getMarks()[0] = Float.parseFloat(ETuserscore1.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore1.getText().toString()));
                        subjectsmain.get(i).getMarks()[1] = Float.parseFloat(ETuserscore2.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore2.getText().toString()));
                        subjectsmain.get(i).getMarks()[2] = Float.parseFloat(ETuserscore3.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore3.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if (GPA > 10.0)
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
                            Float.parseFloat(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0) ||
                            ETuserscore2.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore2.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(1) ||
                            ETuserscore3.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore3.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(2) ||
                            ETuserscore4.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore4.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(3))
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    else {
                        subjectsmain.get(i).getMarks()[0] = Float.parseFloat(ETuserscore1.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore1.getText().toString()));
                        subjectsmain.get(i).getMarks()[1] = Float.parseFloat(ETuserscore2.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore2.getText().toString()));
                        subjectsmain.get(i).getMarks()[2] = Float.parseFloat(ETuserscore3.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore3.getText().toString()));
                        subjectsmain.get(i).getMarks()[3] = Float.parseFloat(ETuserscore4.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore4.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if (GPA > 10.0)
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
                            Float.parseFloat(ETuserscore1.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(0) ||
                            ETuserscore2.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore2.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(1) ||
                            ETuserscore3.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore3.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(2) ||
                            ETuserscore4.getText().toString().equals("") ||
                            Float.parseFloat(ETuserscore4.getText().toString()) > subjectSyncArrayList.get(i).getMaxScores().get(3))
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                    else {
                        subjectsmain.get(i).getMarks()[0] = Float.parseFloat(ETuserscore1.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore1.getText().toString()));
                        subjectsmain.get(i).getMarks()[1] = Float.parseFloat(ETuserscore2.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore2.getText().toString()));
                        subjectsmain.get(i).getMarks()[2] = Float.parseFloat(ETuserscore3.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore3.getText().toString()));
                        subjectsmain.get(i).getMarks()[3] = Float.parseFloat(ETuserscore4.getText().toString());
                        numArrayList.add(Float.parseFloat(ETuserscore4.getText().toString()));
                        for (int j = 0; j < subjectSyncArrayList.get(i).getNoOfTests(); j++) {
                            if (subjectSyncArrayList.get(i).getScoreToConsider().get(j) != -1) {
                                sum += numArrayList.get(j);
                                Tsum += subjectSyncArrayList.get(i).getTopperScore().get(j);
                            }
                        }
                        Double GPA = sum * 10.0 / Tsum;
                        GPA = (double) Math.round(GPA * 100) / 100;
                        if (GPA > 10.0)
                            GPA = 10.0;
                        String returns = "Current GPA is " + GPA;
                        TextView TVGPAforcastDF = findViewById(R.id.TVGPAforcastDF);
                        TVGPAforcastDF.setText(returns);
                        subjectsmain.get(i).setGPA(GPA);
                    }
                    break;
                }
            }
        } else
            Toast.makeText(this, "forecast not available for this Subject.", Toast.LENGTH_SHORT).show();
    }

    //This function also sets up detail frag but without adding to the back stack.
    public void UpdateDetailFrag(final int index) {
        //now setting up detail frag.
        addDataset(pieChart, subjectsmain, index);//setting up pie chart
        UpdateViewVisibility(index);
        TextView TVsubjectnameDF;
        final TextView TVattendancefraction;
        Button BTNeditattendance;
        final Button subReset = findViewById(R.id.subReset);
        TVattendancefraction = findViewById(R.id.TVAttendanceFraction);
        TVsubjectnameDF = findViewById(R.id.TVSubjectNameDF);
        TVsubjectnameDF.setText(subjectsmain.get(index).getSub_name());
        TextView TVGPAforcastDF = findViewById(R.id.TVGPAforcastDF);
        TVGPAforcastDF.setText("Press Calculate to know your GPA.");
        String attendance = subjectsmain.get(index).getPresent() + "/" + subjectsmain.get(index).getTotaldays();
        TVattendancefraction.setText(attendance);
        subReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Reset Subject");
                builder.setMessage("Are you sure you want to reset the Subject?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        subjectsmain.get(index).setPresent(0);
                        subjectsmain.get(index).setTotaldays(0);
                        addDataset(pieChart, subjectsmain, index);
                        String attendence = subjectsmain.get(index).getPresent() + "/" + subjectsmain.get(index).getTotaldays();
                        TVattendancefraction.setText(attendence);
                        float[] marks = {0, 0, 0, 0, 0};
                        subjectsmain.get(index).setMarks(marks);
                        subjectsmain.get(index).setGPA(0.0);

                        com.kinshuu.silverbook.Log log = new com.kinshuu.silverbook.Log(Calendar.getInstance().getTime().toString().split("G")[0]);
                        log.setAction(subjectsmain.get(indexmain).getSub_name() + " Reset : Present changed to " + 0 + " and total classes changed to " + 0 + " on ");
                        LogArrayList.add(log);

                        Toast.makeText(MainActivity.this, "Subject Resetted Sucessfully", Toast.LENGTH_SHORT).show();


                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
        BTNeditattendance = findViewById(R.id.BTNEditAttendance);
        BTNeditattendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenPopup();
                indexmain = index;
            }
        });
        Button BTNcalculateGPA = findViewById(R.id.BTNcalculateGPA);
        BTNcalculateGPA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateGPA(index);
                listFrag.myadapter.notifyDataSetChanged();
            }
        });
    }

    //This function saves data locally.
    private void SaveData() {
        Log.d(TAG, "SaveData: Saving data to disk");
        SharedPreferences SPSubjectsMain = Objects.requireNonNull(this).getSharedPreferences("SubjectsMainArrayList", MODE_PRIVATE);
        SharedPreferences.Editor editor = SPSubjectsMain.edit();
        Gson gson = new Gson();
        String json = gson.toJson(subjectsmain);
        editor.putString("subjectslist", json);
        editor.apply();

        SharedPreferences SPSubjectsSync = Objects.requireNonNull(this).getSharedPreferences("SubjectsSyncArrayList", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = SPSubjectsSync.edit();
        Gson gson1 = new Gson();
        String json1 = gson1.toJson(subjectSyncArrayList);
        editor1.putString("subjectssynclist", json1);
        editor1.apply();

        SharedPreferences SPLogArraylist = Objects.requireNonNull(this).getSharedPreferences("LogArrayList", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = SPLogArraylist.edit();
        Gson gson2 = new Gson();
        String json2 = gson2.toJson(LogArrayList);
        editor2.putString("loglist", json2);
        editor2.apply();
        Log.d(TAG, "SaveData: Data saved");
    }

    //This function loads logarraylist from local storage.
    private ArrayList<com.kinshuu.silverbook.Log> getlog() {
        ArrayList<com.kinshuu.silverbook.Log> loglist;
        SharedPreferences SPLogArraylist = Objects.requireNonNull(this).getSharedPreferences("LogArrayList", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = SPLogArraylist.getString("loglist", null);
        Type type = new TypeToken<ArrayList<com.kinshuu.silverbook.Log>>() {
        }.getType();
        loglist = gson.fromJson(json, type);
        if (loglist == null) {
            loglist = new ArrayList<>();
        }
        Log.d(TAG, "getlog: Loaded LogArrayList from disk and size is " + loglist.size());
        return loglist;
    }

    //This function loads data from the local storage.
    private ArrayList<Subject> LoadData() {
        ArrayList<Subject> subjectsmain;
        SharedPreferences SPSubjectsMain = Objects.requireNonNull(this).getSharedPreferences("SubjectsMainArrayList", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = SPSubjectsMain.getString("subjectslist", null);
        Type type = new TypeToken<ArrayList<Subject>>() {
        }.getType();
        subjectsmain = gson.fromJson(json, type);
        if (subjectsmain == null) {
            subjectsmain = new ArrayList<>();
        }
        Log.d(TAG, "LoadData: Loaded Subjects main from disk and size is " + subjectsmain.size());
        return subjectsmain;
    }

    //This function is used to set up SubjectsSync for an eligible user.
    private ArrayList<SubjectSync> SyncData() {
        Log.d(TAG, "SyncData: Eligible is " + Eligible);
        ArrayList<SubjectSync> list = new ArrayList<>();
        if (Eligible == 1) {
            SharedPreferences SPSubjectsSync = Objects.requireNonNull(this).getSharedPreferences("SubjectsSyncArrayList", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = SPSubjectsSync.getString("subjectssynclist", null);
            Type type = new TypeToken<ArrayList<SubjectSync>>() {
            }.getType();
            list = gson.fromJson(json, type);
        }
        if (list == null)
            list = new ArrayList<>();
        Log.d(TAG, "SyncData: Data loaded from disk and size of SubjectSync is " + list.size());
        return list;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: OnPause Starts");
        SaveData();
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            Log.d(TAG, "onPause: Detached AuthStateListener");
        }
        if (mChildEventListener != null) {
            msubjectsDatabaseReference.removeEventListener(mChildEventListener);
            Log.d(TAG, "onPause: DetachedChildEventListener");
            mChildEventListener = null;
        }
        Log.d(TAG, "onPause: Exitting OnPause.");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: OnResume starts");
        super.onResume();

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        Log.d(TAG, "onResume: Added AuthStateListener");

        Log.d(TAG, "onResume: OnResume Ends");
    }

    private void OnSignedInInitialise(String displayName) {
        Log.d(TAG, "OnSignedInInitialise: Starts");
        mUsername = displayName;
        attachDatabaseReadListener();
        Log.d(TAG, "OnSignedInInitialise: Attached DatabaseListener");
        Log.d(TAG, "OnSignedInInitialise: Ends");
    }

    private void attachDatabaseReadListener() {
        //following code is to manage the Firebase sync data.
        Log.d(TAG, "attachDatabaseReadListener: Starts");
        final ArrayList<SubjectSync> subjectSyncArrayListtemp = new ArrayList<>();
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    SubjectSync subject = dataSnapshot.getValue(SubjectSync.class);
                    subjectSyncArrayListtemp.add(subject);
                    Log.d(TAG, "onChildAdded: New child added: "+subject.getSub_name());
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
                    subjectSyncArrayList = subjectSyncArrayListtemp;
                    Log.d(TAG, "onDataChange: Data Synced from cloud");
                    Log.d(TAG, "onDataChange: firsttime is "+firsttime);
                    if(subjectsmain.size()!=0&&!subjectSyncArrayList.get(0).getSub_name().equals(subjectsmain.get(0).getSub_name()))
                        firsttime=1;
                    if (firsttime == 1) {
                        //changing firsttime to 0.
                        firsttime = 0;
                        SharedPreferences.Editor faveditor = getSharedPreferences("com.kinshuu.silverbook.ref", MODE_PRIVATE).edit();
                        faveditor.putInt("FT", 0);
                        faveditor.apply();
                        Log.d(TAG, "onDataChange: Saved shared preference");
                        Log.d(TAG, "onDataChange: Firsttime==1 and thus going to InitialiseSub");
                        subjectsmain = InitialiseSub();
                        Log.d(TAG, "onDataChange: initialised subjects main");
                        if (YearOfJoining != 1) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Info")
                                    .setMessage("Tap on a subject to view details.")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Continue with delete operation

                                        }
                                    })
                                    .show();
                        }
                        sendDataToListFrag();
                        SetUpFragments();
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
        if (requestCode == RC_USER_PREF) {
            Log.d(TAG, "onActivityResult: Received User Preference!");
            if (resultCode == RESULT_OK) {
                Branch = data.getStringExtra("Branch");
                College = data.getStringExtra("College");
                YearOfJoining = data.getIntExtra("Batch", 0);

                SharedPreferences.Editor faveditor = getSharedPreferences("com.kinshuu.silverbook.ref", MODE_PRIVATE).edit();
                faveditor.putInt("FT", 1);
                faveditor.putString("Branch", Branch);
                faveditor.putString("College", College);
                faveditor.putInt("YearOfJoining", YearOfJoining);
                faveditor.apply();
                if (YearOfJoining > 2017 && College.equals("IIIT-A"))
                    Eligible = 1;
                Log.d(TAG, "In MainActivity class College, Branch and Batch received are " + College + "," + Branch + "," + YearOfJoining);

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setLogo(R.drawable.ic_graduate)//Experiment with this.
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()))
                                .build(),
                        RC_SIGN_IN);
            }

            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cannot work until you provide given info.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_CANCELED) {
                SharedPreferences.Editor faveditor = getSharedPreferences("com.kinshuu.silverbook.ref", MODE_PRIVATE).edit();
                faveditor.putString("Branch", "null");
                faveditor.putString("College", "null");
                faveditor.putInt("YearOfJoining", 0);
                faveditor.putInt("FT", 1);
                faveditor.apply();
                Toast.makeText(this, "Cannot work until you Sign-In", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        Log.d(TAG, "onActivityResult: Exiting ActivityResult");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (findViewById(R.id.layout_portrait) != null && (getSupportFragmentManager().findFragmentByTag("blankfrag")) == getSupportFragmentManager().findFragmentById(R.id.fragCont_portrait))
            finish();
        else if (findViewById(R.id.layout_portrait) == null && (getSupportFragmentManager().findFragmentByTag("blankfrag")) == getSupportFragmentManager().findFragmentById(R.id.list_frag_cont)) {
            finish();
        } else if (findViewById(R.id.layout_portrait) != null && (getSupportFragmentManager().findFragmentByTag("listfrag")) != getSupportFragmentManager().findFragmentById(R.id.fragCont_portrait)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left);
            ft.replace(R.id.fragCont_portrait, listFrag, "listfrag");
            ft.commit();
            getSupportFragmentManager().executePendingTransactions();

            if (findViewById(R.id.layout_portrait) == null) {
                if (subjectsmain.size() == 0) {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.list_frag_cont, blankFrag, "blankfrag").commit();
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.detail_frag_cont, new BlankFragment(), "blankfragdetail").commit();
                }
            } else {
                if (subjectsmain.size() == 0) {
                    Log.d(TAG, "onResume: Now inflating blannkfrag layout");
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.fragCont_portrait, blankFrag, "blankfrag").commit();
                }
            }
            //setting up detail frag to 0th element if phone is in landscape mode.
            if (findViewById(R.id.layout_portrait) == null && subjectsmain != null && subjectsmain.size() != 0) {
                setdetailfrag(0);
            }

            navigationView.setCheckedItem(R.id.nav_home);
        } else if (findViewById(R.id.layout_portrait) != null && (getSupportFragmentManager().findFragmentByTag("listfrag")) == getSupportFragmentManager().findFragmentById(R.id.fragCont_portrait))
            finish();
        else if (findViewById(R.id.layout_portrait) == null && (getSupportFragmentManager().findFragmentByTag("detailfrag")) != getSupportFragmentManager().findFragmentById(R.id.detail_frag_cont)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left);
            ft.replace(R.id.detail_frag_cont, detailFrag, "detailfrag");
            ft.commit();
            getSupportFragmentManager().executePendingTransactions();
            navigationView.setCheckedItem(R.id.nav_home);
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home: {
                SetUpFragments();
                break;
            }
            case R.id.nav_about: {
                if (findViewById(R.id.layout_portrait) != null) {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).add(R.id.fragCont_portrait, new AboutFrag()).commit();
                    getSupportFragmentManager().executePendingTransactions();
                } else {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).add(R.id.detail_frag_cont, new AboutFrag()).commit();
                    getSupportFragmentManager().executePendingTransactions();
                }
                TextView Abouthead = findViewById(R.id.textView28);
                String head = "SilverBook v1.18 Pilot-1";
                String version = "1.18 Pilot-1";
                try {
                    PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
                    version = pInfo.versionName;
                    Log.d(TAG, "version name found and is " + version);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                head = "SilverBook v" + version;
                Abouthead.setText(head);
                break;
            }
            case R.id.nav_addSub: {
                AddSub addSub = new AddSub();
                if (findViewById(R.id.layout_portrait) != null) {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).add(R.id.fragCont_portrait, addSub).commit();
                    getSupportFragmentManager().executePendingTransactions();
                } else {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).add(R.id.detail_frag_cont, addSub).commit();
                    getSupportFragmentManager().executePendingTransactions();
                }
                Button BTNaddsub = findViewById(R.id.BTNaddsub);
                final EditText ETaddsubpresent = findViewById(R.id.ETaddsubpresent);
                final EditText ETaddsubtotalclass = findViewById(R.id.ETaddsubtotalclass);
                final EditText ETaddsubname = findViewById(R.id.ETaddsubname);

                BTNaddsub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (ETaddsubpresent.getText().toString().equals(""))
                            ETaddsubpresent.setText("0");
                        if (ETaddsubtotalclass.getText().toString().equals(""))
                            ETaddsubtotalclass.setText("0");
                        if (ETaddsubname.getText().toString().equals("") ||
                                Float.parseFloat(ETaddsubtotalclass.getText().toString()) < Float.parseFloat(ETaddsubpresent.getText().toString()))
                            Toast.makeText(MainActivity.this, "Enter Valid numbers :)", Toast.LENGTH_SHORT).show();
                        else {
                            Subject subject = new Subject(ETaddsubname.getText().toString());
                            subject.setPresent((int) Float.parseFloat(ETaddsubpresent.getText().toString()));
                            subject.setTotaldays((int) Float.parseFloat(ETaddsubtotalclass.getText().toString()));
                            subject.calculatepercent();
                            subjectsmain.add(subject);
                            if(listFrag==null)
                                Log.d(TAG, "onClick: Listfrag is null");
                            else if(listFrag.myadapter==null)
                                Log.d(TAG, "onClick: listfrag.myadapter is null.");
                            if(subjectsmain.size()==1)
                                SetUpFragments();
                            listFrag.myadapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Subject Added!", Toast.LENGTH_SHORT).show();
                            if (findViewById(R.id.layout_portrait) != null) {
                                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.fragCont_portrait, listFrag, "listfrag").commit();
                                getSupportFragmentManager().executePendingTransactions();
                            } else {
                                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.list_frag_cont, listFrag, "listfrag").commit();
                                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.detail_frag_cont, detailFrag, "detailfrag").commit();
                                getSupportFragmentManager().executePendingTransactions();
                            }
                            navigationView.setCheckedItem(R.id.nav_home);
                        }
                    }
                });
                break;
            }
            case R.id.nav_feedback: {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "iit2018199@iiita.ac.in"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SilverBook Feedback.");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
                break;
            }
            case R.id.nav_reset: {
                new AlertDialog.Builder(this)
                        .setTitle("Reset App")
                        .setMessage("Are you sure you want to reset the app? This cannot be undone.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                SharedPreferences.Editor faveditor = getSharedPreferences("com.kinshuu.silverbook.ref", MODE_PRIVATE).edit();
                                faveditor.putString("Branch", "null");
                                faveditor.putString("College", "null");
                                faveditor.putInt("YearOfJoining", 0);
                                firsttime = 1;
                                faveditor.putInt("FT", firsttime);
                                faveditor.apply();
                                subjectsmain = null;
                                subjectSyncArrayList = null;
                                LogArrayList = null;
                                AuthUI.getInstance().signOut(MainActivity.this);
                                Toast.makeText(MainActivity.this, "Cleared all data.", Toast.LENGTH_LONG).show();
                                getUserPreference();

                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;
            }
            case R.id.nav_share: {
                Intent shareapp = new Intent(Intent.ACTION_SEND);
                shareapp.setType("text/plain");
                String s = "Hey checkout this cool SilverBook app. It has a lot of awesome features like GPA forecast, attendance forecast, attendance tracking etc. This app is my daily driver :). Check it out here https://github.com/MiKinshu/SilverBook/raw/master/SilverBook.apk ";
                shareapp.putExtra(Intent.EXTRA_TEXT, s);
                startActivity(Intent.createChooser(shareapp, "Share App"));
                break;
            }
            case R.id.nav_deletesub: {
                if (findViewById(R.id.layout_portrait) == null && (getSupportFragmentManager().findFragmentByTag("detailfrag")) != getSupportFragmentManager().findFragmentById(R.id.detail_frag_cont)) {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.detail_frag_cont, detailFrag, "detailfrag").commit();
                    getSupportFragmentManager().executePendingTransactions();
                } else {
                    if (findViewById(R.id.layout_portrait) != null && (getSupportFragmentManager().findFragmentByTag("listfrag")) != getSupportFragmentManager().findFragmentById(R.id.fragCont_portrait)) {
                        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.fragCont_portrait, listFrag, "listfrag").commit();
                        getSupportFragmentManager().executePendingTransactions();
                    }
                }
                Toast.makeText(this, "Long press a subject to delete.", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.nav_logbook: {
                if (findViewById(R.id.layout_portrait) != null) {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.fragCont_portrait, new LogFrag()).commit();
                    getSupportFragmentManager().executePendingTransactions();
                } else {
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fui_slide_in_right,R.anim.fui_slide_out_left).replace(R.id.detail_frag_cont, new LogFrag()).commit();
                    getSupportFragmentManager().executePendingTransactions();
                }
                final ListView TVlog = findViewById(R.id.TVlog);
                //String Log="";
                final ArrayList<String> logList = new ArrayList<>();
                for (int i = LogArrayList.size() - 1; i >= 0; --i) {
                    logList.add(LogArrayList.get(i).getAction() + LogArrayList.get(i).getTime());
                }
                final ArrayAdapter<String> itemsAdapter =
                        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logList);
                TVlog.setAdapter(itemsAdapter);

                //TVlog.setText(Log);
                //TVlog.setMovementMethod(new ScrollingMovementMethod());
                Button BTNresetlog = findViewById(R.id.BTNresetlog);
                BTNresetlog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Clear Log")
                                .setMessage("Are you sure you want to clear the log? This cannot be undone.")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Continue with delete operation
                                        LogArrayList.clear();
                                        logList.clear();
                                        TVlog.setAdapter(itemsAdapter);
                                        //TVlog.setText("");
                                        Toast.makeText(MainActivity.this, "Log Cleared!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
                break;
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}