package com.lspsoftwares.minhalistadecompras.objetos_auxiliares;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ManipuladorDataTempo {
    private long dataInt;
    private long tempoInt;
    private String dataString;
    private String tempoString;
    private String padraoHora;
    private String padraoData;

    public ManipuladorDataTempo() {
    }

    public ManipuladorDataTempo(String dataString) {
        this.dataString = dataString;
    }

    public ManipuladorDataTempo(long dataInt) {
        this.dataInt = dataInt;
    }
    public ManipuladorDataTempo(Date data) throws ParseException {
        setPadraoData("dd-MM-yyyy");
        setPadraoHora("HH:mm");
        SimpleDateFormat formataData = new SimpleDateFormat(getPadraoData());
        this.dataString = formataData.format(data);
        this.dataInt = dataStringToDataInt(getDataString());
        formataData = new SimpleDateFormat(getPadraoHora());
        this.tempoString = formataData.format(data);
        this.tempoInt =  tempoStringToTempoInt(getTempoString());

    }

    public long getDataInt() {
        return dataInt;
    }

    public void setDataInt(long dataInt) {
        this.dataInt = dataInt;
    }

    public long getTempoInt() {
        return tempoInt;
    }

    public void setTempoInt(long tempoInt) {
        this.tempoInt = tempoInt;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    private String getTempoString() {
        return tempoString;
    }

    public void setTempoString(String tempoString) {
        this.tempoString = tempoString;
    }
    public static String dataIntToDataString(Long dataInt) {
        SimpleDateFormat formataData = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date(dataInt);
        return formataData.format(date);
    }
    public static String dataIntToDataString(Long dataInt, String pattern) {
        SimpleDateFormat formataData = new SimpleDateFormat(pattern);
        Date date = new Date(dataInt);
        String d = formataData.format(date);
        return d;
    }
    public static Long dataStringToDataInt(String dataString) throws ParseException {
        SimpleDateFormat formataData = new SimpleDateFormat("dd-MM-yyyy");
        formataData.parse(dataString);
        Long l = formataData.getCalendar().getTimeInMillis();
        return  l;
    }
    public static String tempoIntToTempoString(Long dataInt) {
        SimpleDateFormat formataData = new SimpleDateFormat("HH:mm");
        Date date = new Date(dataInt);
        return formataData.format(date);
    }
    public static Long tempoStringToTempoInt(String dataString) throws ParseException {
        SimpleDateFormat formataData = new SimpleDateFormat("HH:mm");
        Long tempo = 0L;
        try {
            formataData.parse(dataString);
            tempo = formataData.getCalendar().getTimeInMillis();
        }
        catch(Exception a)
        {
            Log.e("erro tempo",a.getMessage());
        }


        return  tempo;
    }

    /**
     *
     * @param tempoInt um valor de data no formato inteiro
     *
     * @return numero de horas correspondente
     */
    public static double horas(long tempoInt){
        SimpleDateFormat formataData = new SimpleDateFormat("HH:mm");
        Date date = new Date(tempoInt);
        String s = formataData.format(date);
        String[] splited = s.split(":");
        return Double.parseDouble(splited[0])+ Double.parseDouble(splited[1])/60;
    }

    public String getPadraoHora() {
        return padraoHora;
    }

    public void setPadraoHora(String padraoHora) {
        this.padraoHora = padraoHora;
    }

    public String getPadraoData() {
        return padraoData;
    }

    public void setPadraoData(String padraoData) {
        this.padraoData = padraoData;
    }
}
