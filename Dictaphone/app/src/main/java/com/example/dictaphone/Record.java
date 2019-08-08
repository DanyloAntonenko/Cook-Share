package com.example.dictaphone;

public class Record {
    private int id;
    private String name;
    private int duration;
    private String date;

    public Record(int id, String name, int duration, String date){
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.date = date;
    }

    public Record(String name, int duration, String date){
        this.name = name;
        this.duration = duration;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String toString(){

        return String.valueOf(id) + ": " + name + ", " + String.valueOf(duration) + "\t" + date;
    }
}
