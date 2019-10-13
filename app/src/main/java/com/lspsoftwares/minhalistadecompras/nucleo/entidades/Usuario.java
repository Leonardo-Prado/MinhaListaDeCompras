package com.lspsoftwares.minhalistadecompras.nucleo.entidades;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String uid;
    private String nome;
    private String email;
    private boolean downloadListaItem;
    private List<String> listas = new ArrayList<>();

    public Usuario() {    }

    public Usuario(String nome) {
        this.nome = nome;
    }

    public Usuario(String uid, String nome) {
        this.uid = uid;
        this.nome = nome;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getListas() {
        return listas;
    }

    public void setListas(List<String> listas) {
        this.listas = listas;
    }

    public boolean isDownloadListaItem() {
        return downloadListaItem;
    }

    public void setDownloadListaItem(boolean downloadListaItem) {
        this.downloadListaItem = downloadListaItem;
    }
}
