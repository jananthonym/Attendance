package com.mobileapp.attendance.model;

/**
 * Created by Jan on 11/26/15.
 */
public class Student {
    private String name;
    private String mac;
    private int id;

    public Student(){}

    public Student(String name, String mac){
        this.name=name;
        this.mac=mac;
    }

    public Student(int id, String name, String mac){
        this.id=id;
        this.name=name;
        this.mac=mac;
    }

    // setter
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMac(String mac){
        this.mac=mac;
    }

    // getter
    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getMac() {
        return this.mac;
    }
}
