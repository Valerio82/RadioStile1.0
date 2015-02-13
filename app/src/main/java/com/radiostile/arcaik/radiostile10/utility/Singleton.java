package com.radiostile.arcaik.radiostile10.utility;

/**
 * Created by arcaik on 27/11/2014.
 */
public class Singleton {
    private static Singleton ourInstance = new Singleton();
    private boolean statoPulsante;
    private Singleton() {
        statoPulsante=false;
    }

   public static Singleton getInstance() {
        if(ourInstance==null)
        {
            ourInstance=new Singleton();
        }
        return ourInstance;
            }
    public boolean getBoolean(){
        return statoPulsante;
    }
    public void setBoolean(boolean stato){
        statoPulsante=stato;
    }

}
