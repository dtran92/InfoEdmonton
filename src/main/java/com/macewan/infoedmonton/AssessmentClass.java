package com.macewan.infoedmonton;

import java.util.Collections;
import java.util.List;

public class AssessmentClass {
    private final String assessmentClassName;
    private final List<PropertyAssessment> propertyList;
    private long sum;
    private int min;
    private int max;

    public AssessmentClass(String assessmentClassName, List<PropertyAssessment> propertyList) {
        this.assessmentClassName = assessmentClassName;
        this.propertyList = propertyList;
        this.sum = 0;
    }

    public String getAssessmentClassName() {
        return assessmentClassName;
    }

    public List<PropertyAssessment> getPropertyList() {
        return propertyList;
    }

    public void addSum(long num) {
        this.sum += num;
    }

    public int getMin() {
        return this.min;
    }

    // min
    public void setMin(int num) {
        this.min = num;
    }

    // max
    public int getMax() {
        return this.max;
    }

    public void setMax(int num) {
        this.max = num;
    }

    public int mean() {
        return (int) ((double) this.sum / propertyList.size());
    }

    public int median() {
        Collections.sort(propertyList);
        int size = propertyList.size();
        if (size % 2 == 1) {
            return propertyList.get((size / 2)).getAssessedValue();
        } else {
            return (propertyList.get(size / 2).getAssessedValue() + propertyList.get(size / 2 - 1).getAssessedValue()) / 2;
        }
    }

    public int size() {
        return propertyList.size();
    }

    public long getSum() {
        return sum;
    }
}
    
