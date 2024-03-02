package com.lspsoftwares.minhalistadecompras.entidades;

import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;

import java.util.ArrayList;
import java.util.List;

public class ListaCompras {
    private String uId;
    private String nome;
    private String descricao;
    private String uIdCategoriaLista;
    private List<ItemLista> itens = new ArrayList<>();
    private int itensCount = 0;
    private String criadorUid;
    private long dataCriacao;
    private long horaCriacao;
    private int numAval;
    private double val;
    private int icon = 0;
    private List<String> tags = new ArrayList<>();

    public ListaCompras() {
    }

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

    public List<ItemLista> getItens() {
        return itens;
    }

    public void setItens(List<ItemLista> itens) {
        this.itens = itens;
    }

    public int getItensCount() {
        return itensCount;
    }

    public void setItensCount(int itensCount) {
        this.itensCount = itensCount;
    }

    public String getCriadorUid() {
        return criadorUid;
    }

    public void setCriadorUid(String criador) {
        this.criadorUid = criador;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getuIdCategoriaLista() {
        return uIdCategoriaLista;
    }

    public void setuIdCategoriaLista(String uIdCategoriaLista) {
        this.uIdCategoriaLista = uIdCategoriaLista;
    }
    public void avaliar(double val){
        setVal((val+getVal()*getNumAval())/(getNumAval()+1));
        setNumAval(getNumAval()+1);

    }
    public void reavaliar(double val,double antigo){
        setVal((getVal()*getNumAval()-antigo)/(getNumAval()-1));
        setVal((val+getVal()*getNumAval())/(getNumAval()));
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String shareText(){
        String myShareText = "";
        myShareText += "*"+getNome() + "*\n";
        myShareText += getDescricao() + "\n\n\t";
        String uID = getuId();
        List<Item> items = VariaveisEstaticas.getItemMap().get(uID);
        for(ItemLista i : getItens()){
            Item item = new Item();
            for (Item it: items){
                if(it.getuId().contains(i.getItemUid())){
                    item = it;
                    break;
                }
            }
            myShareText += "* "+ item.getNome() + "\t- "+ i.getQuantidade() + i.getUnidade() + "\n\t";
        }
        return myShareText;
    }

    public String shareHtml(){
        String myShareText = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Home</title>\n" +
                "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
                "</head>\n" +
                "<body>";
        myShareText +="<div><h4>" + getNome() + "</h4></div><br>";
        myShareText += "<div><span>" + getDescricao() + "</span></div><br><ul>";
        String uID = getuId();
        List<Item> items = VariaveisEstaticas.getItemMap().get(uID);
        for(ItemLista i : getItens()){
            Item item = new Item();
            for (Item it: items){
                if(it.getuId().contains(i.getItemUid())){
                    item = it;
                    break;
                }
            }
            myShareText +="<li>" +item.getNome() + "\t"+ i.getQuantidade() + i.getUnidade() + "</li>";
        }
        myShareText += "</ul></body>\n" +
                "</html>";
        return myShareText;
    }


}