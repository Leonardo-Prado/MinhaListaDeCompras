package com.lspsoftwares.minhalistadecompras.entidades;

public class Classificacao {
    private String uId;
    private int numAval = 0;
    private double val = 0;

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public int getNumAval() {
        return numAval;
    }

    public void setNumAval(int numAval) {
        this.numAval = numAval;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }
}
