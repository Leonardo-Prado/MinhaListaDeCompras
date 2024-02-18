package com.lspsoftwares.minhalistadecompras.entidades;

import java.util.ArrayList;
import java.util.List;

public class Compras {
    private String compraUid;
    private String listaUid;
    private List<ItemListaPreco> itemPrecos = new ArrayList<>();
    private boolean concluida;
    private double precoTotal;
    private String precoTotalString;
    public String getCompraUid() {
        return compraUid;
    }
    public void setCompraUid(String compraUid) {
        this.compraUid = compraUid;
    }
    public String getListaUid() {
        return listaUid;
    }
    public void setListaUid(String listaUid) {
        this.listaUid = listaUid;
    }
    public List<ItemListaPreco> getItemPrecos() {
        return itemPrecos;
    }
    public void setItemPrecos(List<ItemListaPreco> itemPrecos) {
        this.itemPrecos = itemPrecos;
    }
    public boolean isConcluida() {
        return concluida;
    }
    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }
    public double getPrecoTotal() {
        return precoTotal;

    }
    public void setPrecoTotal(double precoTotal) {
        this.precoTotal = precoTotal;
        setPrecoTotalString(Double.toString(getPrecoTotal()));
    }
    public String getPrecoTotalString() {

        return String.format("%,.2f",precoTotal);
    }
    public void setPrecoTotalString(String precoTotalString) {
        this.precoTotalString = precoTotalString;
    }
}
