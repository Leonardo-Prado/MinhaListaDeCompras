package com.lspsoftwares.minhalistadecompras.objetos_auxiliares;

import java.util.Random;

public class GeradorCodigosUnicos {
    static String code = "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";
    int digitos = 8;

    public GeradorCodigosUnicos() {
    }

    public GeradorCodigosUnicos(int digitos) {
        this.digitos = digitos;
    }

    public String gerarCodigos(){
        String a = "";
        for(int i = 0;i<digitos;i++){
            Random r = new Random();
            int p = r.nextInt(code.length()-1);
            a = a + code.charAt(p);
        }
        return  a;
    }
}
