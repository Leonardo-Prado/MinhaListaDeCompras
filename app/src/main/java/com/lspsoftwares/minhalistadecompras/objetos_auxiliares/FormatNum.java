package com.lspsoftwares.minhalistadecompras.objetos_auxiliares;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatNum {
    private double numero;
    private int casasDecimais;
    private String pattern = "00000.##";

    public static double casasDecimais(double valor,int numCasas)
    {
        String padrao = "00000.";
        int i = 0;
        while(i<numCasas){
            padrao = padrao+"#";
            i++;
        }
        String s = null;
        try {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
            DecimalFormat decimalFormat = (DecimalFormat)numberFormat;
            decimalFormat.applyPattern(padrao);
            s = decimalFormat.format(valor);

        }catch (Exception e)
        {
            e.getMessage();
        }
        s.replace(',','.');
        return Double.parseDouble(s);
    }

    public double getNumero() {
        return numero;
    }

    public void setNumero(double numero) {
        this.numero = numero;
    }

    public int getCasasDecimais() {
        return casasDecimais;
    }

    public void setCasasDecimais(int casasDecimais) {
        this.casasDecimais = casasDecimais;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
