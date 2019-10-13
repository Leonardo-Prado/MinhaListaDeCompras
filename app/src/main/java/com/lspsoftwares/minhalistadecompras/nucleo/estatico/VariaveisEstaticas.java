package com.lspsoftwares.minhalistadecompras.nucleo.estatico;

import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariaveisEstaticas {
    private static VariaveisEstaticas instancia;
    private static Usuario usuario = new Usuario();
    private static List<ListaCompras> listaCompras = new ArrayList<>();
    private static Map<String,Item> itemMap = new HashMap<>();


    private VariaveisEstaticas(){ }

    public static synchronized VariaveisEstaticas getInstance(){
        if(instancia==null)
            instancia = new VariaveisEstaticas();
        return instancia;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario usuario) {
        VariaveisEstaticas.usuario = usuario;
    }

    public static List<ListaCompras> getListaCompras() {
        return listaCompras;
    }

    public static void setListaCompras(List<ListaCompras> listaCompras) {
        VariaveisEstaticas.listaCompras = listaCompras;
    }

    public static Map<String, Item> getItemMap() {
        return itemMap;
    }

    public static void setItemMap(Map<String, Item> itemMap) {
        VariaveisEstaticas.itemMap = itemMap;
    }
}
