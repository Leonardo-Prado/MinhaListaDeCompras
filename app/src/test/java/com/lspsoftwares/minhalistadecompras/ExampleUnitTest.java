package com.lspsoftwares.minhalistadecompras;

import android.util.Log;

import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Usuario;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void classtest(){
        Method[] methods = Usuario.class.getMethods();
    }
    @Test
    public void randomTest(){
        int i = 1;
        while(i!=3) {
            Random rand = new Random();
            i = rand.nextInt(2);
        }
    }
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}