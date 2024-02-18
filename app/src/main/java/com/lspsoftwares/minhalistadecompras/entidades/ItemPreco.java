package com.lspsoftwares.minhalistadecompras.entidades;

public class ItemPreco {
    private String uIdLista;
    private String uIdUsuario;
    private String uIdItem;
    private String uIdPreco;
    private double preco;

    public String getuIdLista() {
        return uIdLista;
    }

    public void setuIdLista(String uIdLista) {
        this.uIdLista = uIdLista;
    }

    public String getuIdUsuario() {
        return uIdUsuario;
    }

    public void setuIdUsuario(String uIdUsuario) {
        this.uIdUsuario = uIdUsuario;
    }

    public String getuIdItem() {
        return uIdItem;
    }

    public void setuIdItem(String uIdItem) {
        this.uIdItem = uIdItem;
    }

    public String getuIdPreco() {
        return uIdPreco;
    }

    public void setuIdPreco(String uIdPreco) {
        this.uIdPreco = uIdPreco;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }
}
