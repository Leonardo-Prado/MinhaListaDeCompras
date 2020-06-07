package com.lspsoftwares.minhalistadecompras.nucleo.entidades;

import java.util.ArrayList;
import java.util.List;

public class Tag {
    private String tag;
    private List<Classificacao> classificacoes = new ArrayList<>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Classificacao> getClassificacoes() {
        return classificacoes;
    }

    public void setClassificacoes(List<Classificacao> classificacoes) {
        this.classificacoes = classificacoes;
    }
}