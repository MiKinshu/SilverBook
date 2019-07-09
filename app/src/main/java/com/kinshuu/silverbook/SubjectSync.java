package com.kinshuu.silverbook;

import java.util.ArrayList;

public class SubjectSync {
    private String Sub_name;
    private int NoOfTests;
    private String ExamName;
    private ArrayList<Integer> TopperScore;
    private ArrayList<String> TestNames;
    private ArrayList<Integer> MaxScores= new ArrayList<>();
    private ArrayList<Integer> ScoreToConsider= new ArrayList<>();
    private int credit;

    public ArrayList<Integer> getScoreToConsider() {
        return ScoreToConsider;
    }

    public void setScoreToConsider(ArrayList<Integer> scoreToConsider) {
        ScoreToConsider = scoreToConsider;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public SubjectSync(String sub_name, int noOfTests, String examName, ArrayList<Integer> topperScore, ArrayList<String> testNames, ArrayList<Integer> maxScores, ArrayList<Integer> scoresToConsider) {
        Sub_name = sub_name;
        NoOfTests = noOfTests;
        ExamName = examName;
        TopperScore = topperScore;
        TestNames = testNames;
        MaxScores = maxScores;
        ScoreToConsider=scoresToConsider;
    }

    public ArrayList<Integer> getMaxScores() {
        return MaxScores;
    }

    public void setMaxScores(ArrayList<Integer> maxScores) {
        MaxScores = maxScores;
    }

    public ArrayList<String> getTestNames() {
        return TestNames;
    }

    public void setTestNames(ArrayList<String> testNames) {
        TestNames = testNames;
    }

    public String getSub_name() {
        return Sub_name;
    }

    public void setSub_name(String sub_name) {
        Sub_name = sub_name;
    }

    public int getNoOfTests() {
        return NoOfTests;
    }

    public void setNoOfTests(int NoofTests) {
        this.NoOfTests = NoofTests;
    }

    public String getExamName() {
        return ExamName;
    }

    public void setExamName(String examName) {
        ExamName = examName;
    }

    public ArrayList<Integer> getTopperScore() {
        return TopperScore;
    }

    public void setTopperScore(ArrayList<Integer> TopperScore) {
        this.TopperScore = TopperScore;
    }

    public SubjectSync(){}
}


