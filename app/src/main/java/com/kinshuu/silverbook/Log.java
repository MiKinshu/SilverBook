package com.kinshuu.silverbook;

import android.os.Parcel;
import android.os.Parcelable;

public class Log implements Parcelable {
    private String action;
    private String time;

    public Log(String time){
        this.time=time;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(time);
        dest.writeString(action);
    }

    public Log(Parcel source) {
        this.time=source.readString();
        this.action=source.readString();
    }

    public static final Parcelable.Creator<Log> CREATOR = new Parcelable.Creator<Log>(){

        @Override
        public Log createFromParcel(Parcel source) {
            return new Log(source);
        }

        @Override
        public Log[] newArray(int size) {
            return new Log[size];
        }
    };
}
