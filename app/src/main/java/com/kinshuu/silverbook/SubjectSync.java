package com.kinshuu.silverbook;

import java.util.ArrayList;

public class SubjectSync {
    String Sub_name;
    int NoOfTests;
    String ExamName;
    ArrayList<Integer> TopperScore;
    ArrayList<String> TestNames;
    ArrayList<Integer> MaxScores;
    Integer ScoreToConsider[]={-1,-1,-1,-1,-1};

    public Integer[] getScoreToConsider() {
        return ScoreToConsider;
    }

    public void setScoreToConsider(Integer[] scoreToConsider) {
        ScoreToConsider = scoreToConsider;
    }

    public SubjectSync(String sub_name, int noOfTests, String examName, ArrayList<Integer> topperScore, ArrayList<String> testNames, ArrayList<Integer> maxScores, Integer[] scoresToConsider) {
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

    public void setNoOfTests(int NoOfTests) {
        this.NoOfTests = NoOfTests;
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

    public void setTopperScore(ArrayList<Integer> TopperScore
    ) {
        this.TopperScore = TopperScore;
    }
}


