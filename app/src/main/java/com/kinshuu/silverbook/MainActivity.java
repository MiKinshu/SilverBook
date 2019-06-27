package com.kinshuu.silverbook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SubjectAdapter.itemclicked {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("INONCREAT","on creatis being executed");

    }

    @Override
    public void onItemClicked(int index) {
        Toast.makeText(this, "itemclicked", Toast.LENGTH_SHORT).show();
    }
}
