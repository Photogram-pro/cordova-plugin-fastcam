package com.cordovapluginfastcam;

import android.util.Log;

import java.util.ArrayList;


public class PerformanceAnalysis {
    private String name;
    private ArrayList<Double> values;
    public PerformanceAnalysis(String name) {
        this.name = name;
        this.values = new ArrayList<Double>();
    }

    public void addValue(double val) {
        int size = this.values.size();
        int indexToAdd = size;
        if (size > 0) {
            int lastElemIndex = this.values.size() - 1;
            double lastValue = this.values.get(lastElemIndex);
            if (lastValue > val) {
                indexToAdd = lastElemIndex;
            }
        }

        this.values.add(indexToAdd, val);
    }

    public double getMean() {
        double sum = this.values.stream().reduce(0d, (subtotal, element) -> subtotal + element);
        return  (sum / (double) this.values.size());
    }

    public double getMedian() {
        Double size = new Double(this.values.size());
        boolean isEven = (size % 2) == 0;
        double median = 0l;
        if (isEven) {
            Double index = (size / 2) - 1;
            median = (this.values.get(index.intValue()) + this.values.get(index.intValue() + 1)) / 2;
        } else {
            int index = new Double((Math.ceil(size / 2) - 1)).intValue();
            median = this.values.get(index);
        }

        return median;
    }

    public double getMax() {
        return this.values.get(this.values.size() - 1);
    }

    public String getInfo() {
        double mean = this.getMean();
        double median = this.getMedian();
        double max = this.getMax();
        return "\n[ PerformanceAnalyis of " + this.name + " ]:\n" +
                "\tMean: " + mean + "\n" +
                "\tMedian: " + median + "\n" +
                "\tMax: " + max + "\n";
    }
}
