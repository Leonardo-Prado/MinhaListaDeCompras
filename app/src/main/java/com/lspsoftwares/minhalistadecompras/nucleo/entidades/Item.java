package com.lspsoftwares.minhalistadecompras.nucleo.entidades;

public class Item {
    private String uId;
    private String nome;
    private String descricao;
    private long dataCriacao;
    private long horaCriacao;
    private String criadorUid;
    private String categoriaUid;
    private double quantidade;
    private String unidade;
    private boolean interna;

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public long getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(long dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public long getHoraCriacao() {
        return horaCriacao;
    }

    public void setHoraCriacao(long horaCriacao) {
        this.horaCriacao = horaCriacao;
    }

    public String getCriadorUid() {
        return criadorUid;
    }

    public void setCriadorUid(String criadorUid) {
        this.criadorUid = criadorUid;
    }

    public String getCategoriaUid() {
        return categoriaUid;
    }

    public void setCategoriaUid(String categoriaUid) {
        this.categoriaUid = categoriaUid;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidade() {return unidade;}

    public void setUnidade(String unidade) {this.unidade = unidade;}

    public boolean isInterna() {return interna;}

    public void setInterna(boolean interna) {this.interna = interna;}
}
