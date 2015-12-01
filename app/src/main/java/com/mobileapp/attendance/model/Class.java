package com.mobileapp.attendance.model;

/**
 * Created by Jan on 11/26/15.
 */
public class Class {
    private String name;
    private int id;

    public Class(){}

    public Class(String name){
        this.name=name;
    }

    public Class(int id, String name){
        this.id=id;
        this.name=name;
    }

    // setter
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    // getter
    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
