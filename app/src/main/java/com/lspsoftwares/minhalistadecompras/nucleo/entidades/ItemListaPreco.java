package com.lspsoftwares.minhalistadecompras.nucleo.entidades;

public class ItemListaPreco {
    private String ItemUid;
    private double preco = 0.00;
    private boolean concluido = false;

    public String getItemUid() {
        return ItemUid;
    }

    public void setItemUid(String itemUid) {
        ItemUid = itemUid;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public boolean isConcluido() {
        return concluido;
    }

    public void setConcluido(boolean concluido) {
        this.concluido = concluido;
    }
}
