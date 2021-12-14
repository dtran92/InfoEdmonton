package com.macewan.infoedmonton;

import java.util.Collections;
import java.util.List;

public class Neighborhood {
    private final String neighborhoodName;
    private final List<PropertyAssessment> propertyList;
    private long sum;
    private int min;
    private int max;

    public Neighborhood(String neighborhoodName, List<PropertyAssessment> propertyList) {
        this.neighborhoodName = neighborhoodName;
        this.propertyList = propertyList;
        this.sum = 0;
    }

    public String getNeighborhoodName() {
        return this.neighborhoodName;
    }

    public List<PropertyAssessment> getPropertyList() {
        return this.propertyList;
    }

    public void addSum(long num) {
        this.sum += num;
    }

    // min
    public int getMin() {
        return this.min;
    }

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
            return propertyList.get(size / 2).getAssessedValue();
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
