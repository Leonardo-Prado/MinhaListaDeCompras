package com.lspsoftwares.minhalistadecompras.objetos_auxiliares;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCriador {
    private static String algoritmo = "SHA-256";
    public static byte[] encriptar(String texto) {
        byte [] encriptado = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(algoritmo);
            encriptado = digest.digest(texto.getBytes("UTF-8"));
            return encriptado;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encriptado;
    }
    public static String encriptarToString(String texto){
        byte [] encriptado = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(algoritmo);
            encriptado = digest.digest(texto.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%0" + (encriptado.length*2) + "X", new BigInteger(1, encriptado));
    }
}
