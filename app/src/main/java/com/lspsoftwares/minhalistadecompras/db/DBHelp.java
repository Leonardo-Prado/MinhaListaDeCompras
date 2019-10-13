package com.lspsoftwares.minhalistadecompras.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBHelp extends SQLiteOpenHelper {

    public static final String DBNOME = "MinhaListaDB";
    public static final int VERSAO = 1;
    public DBHelp(Context context) {
        super(context,DBNOME, null, VERSAO);
        try {
            SQLiteDatabase db = this.getWritableDatabase();
        }catch (Exception e){
            Log.e("erro pegar db",e.getMessage());
        }

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
       try {
           sqLiteDatabase.execSQL("CREATE TABLE Item(_id TEXT PRIMARY KEY,CategoriaUid TEXT NOT NULL,DataCriacao INTEGER NOT NULL,Descricao TEXT,HoraCriacao INTEGER NOT NULL, Nome TEXT NOT NULL,Quantidade REAL NOT NULL, Unidade TEXT)");
           sqLiteDatabase.execSQL("CREATE TABLE Config(_id TEXT PRIMARY KEY AUTOINCREMENT,ItensDownload INTEGER NOT NULL)");
       }catch (SQLException e) {
           Log.e("Erro ao criar tabela", e.getMessage());
       }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
       try{

       }catch (SQLException e) {
           Log.e("Erro ao atualizar db", e.getMessage());
       }

    }
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try{
            sqLiteDatabase.execSQL("drop table Item");
            try {
                sqLiteDatabase.execSQL("drop table ItensDownload");
            }catch (Exception e){
                Log.e("Erro ao dropar tabela", e.getMessage());
            }
            onCreate(sqLiteDatabase);
        }catch (SQLException e) {
            Log.e("Erro ao dropar tabela", e.getMessage());
        }
    }


}
