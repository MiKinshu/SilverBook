package com.kinshuu.silverbook;

import android.os.Parcel;
import android.os.Parcelable;

public class Subject implements Parcelable {
    private int totaldays,present;
    private int[] marks= new int[5];
    private String sub_name;
    double SGPI=0,AttendancePercent=0;

    // Getters and Setters for the class.

    Subject(String sub_name) {
        this.sub_name = sub_name;
        this.present=0;
        this.totaldays=0;
    }

    public void calculatepercent(){
        this.AttendancePercent=(this.present*100.0)/this.totaldays;
        this.AttendancePercent=(double)Math.round(this.AttendancePercent*100.0)/100.0;
    }

    public int getTotaldays() {
        return totaldays;
    }

    public void setTotaldays(int totaldays) {
        this.totaldays = totaldays;
    }

    public int getPresent() {
        return present;
    }

    public void setPresent(int present) {
        this.present = present;
    }


    public String getSub_name() {
        return sub_name;
    }

    public void setSub_name(String sub_name) {
        this.sub_name = sub_name;
    }

    public double getSGPI() {
        return SGPI;
    }

    public void setSGPI(double SGPI) {
        this.SGPI = SGPI;
    }

    public double getAttendancePercent() {
        return AttendancePercent;
    }

    public void setAttendancePercent(double attendancePercent) {
        AttendancePercent = attendancePercent;
    }

    // The methods given below were set up so as to enable me to pass an ArrayList of Subject
    // objects from MainActivity to ListFrag.

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sub_name);
        dest.writeInt(totaldays);
        dest.writeInt(present);
        dest.writeDouble(SGPI);
        dest.writeDouble(AttendancePercent);
        dest.writeIntArray(marks);
    }

    public Subject(Parcel source) {
        this.sub_name=source.readString();
        this.totaldays=source.readInt();
        this.present=source.readInt();
        this.SGPI=source.readDouble();
        this.AttendancePercent=source.readDouble();
        this.marks=source.createIntArray();
    }

    public static final Parcelable.Creator<Subject> CREATOR = new Parcelable.Creator<Subject>(){

        @Override
        public Subject createFromParcel(Parcel source) {
            return new Subject(source);
        }

        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

}
