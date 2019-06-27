package com.kinshuu.silverbook;

import java.util.ArrayList;

public class Subject {
    private int totaldays=0,present=0,absent=0;
    private ArrayList<Integer> marks;
    private String sub_name;
    double SGPI=0,AttendancePercent=0;


    public Subject(String sub_name) {
        this.sub_name = sub_name;
        this.present=0;
        this.absent=0;
        this.totaldays=0;
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

    public int getAbsent() {
        return absent;
    }

    public void setAbsent(int absent) {
        this.absent = absent;
    }

    public ArrayList<Integer> getMarks() {
        return marks;
    }

    public void setMarks(ArrayList<Integer> marks) {
        this.marks = marks;
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
}
