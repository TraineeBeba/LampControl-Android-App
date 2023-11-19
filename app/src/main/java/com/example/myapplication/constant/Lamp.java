package com.example.myapplication.constant;


public enum Lamp {

    ON, OFF;

    public static Lamp getToggle(Lamp readLampState) {
        if(readLampState == OFF) return ON;
        else return OFF;
    }






}