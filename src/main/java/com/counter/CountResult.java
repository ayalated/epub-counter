package com.counter;

public class CountResult {

    private int total;

    public void add(int count) {
        total += count;
    }

    public int getTotal() {
        return total;
    }

    public void print() {
        System.out.println(getTotal());
    }


}
