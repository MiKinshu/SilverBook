package com.kinshuu.silverbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class PopupDiaogue extends AppCompatDialogFragment {

    private EditText ETclassesattended;
    private EditText ETtotalclasses;
    private PopupDialogueListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater= Objects.requireNonNull(getActivity()).getLayoutInflater();
        final View view=inflater.inflate(R.layout.popupdialogue, null);
        ETclassesattended=view.findViewById(R.id.ETclassesatttended);
        ETtotalclasses=view.findViewById(R.id.ETtotalclasses);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String present=ETclassesattended.getText().toString();
                        String total=ETtotalclasses.getText().toString();
                        if(present.equals("")||total.equals(""))
                            Toast.makeText(getContext(), "Enter Valid Numbers", Toast.LENGTH_SHORT).show();
                        else {
                            Integer classesattended = Integer.parseInt(present);
                            Integer totalclasses = Integer.parseInt(total);
                            if (totalclasses < classesattended) {
                                Toast.makeText(getContext(), "Enter Valid Numbers", Toast.LENGTH_SHORT).show();
                            }
                            else
                                listener.applytexts(classesattended,totalclasses);
                        }
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener= (PopupDialogueListener)context;
    }

    public interface PopupDialogueListener{
        void applytexts(Integer classesattended, Integer totalclasses);
    }
}
