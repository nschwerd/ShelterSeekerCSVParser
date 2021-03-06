package com.nschwerd.csv;

import java.util.ArrayList;

public class Shelter {
    public Shelter() {};
    
    public Shelter(String id, String n, String capS, String res, String addr, String note, String ph, int capN, double lat, double lon, Gender gen, AgeRest age, boolean vet) {
        uid = id;
        name = n;
        capacityStr = capS;
        restrictions = res;
        address = addr;
        notes = note;
        phone = ph;
        capacityNum = capN;
        this.lat = lat;
        this.lon = lon;
        gender = gen;
        age = ageRest;
        veteran = vet;
        reservations = new ArrayList<>();
    }
    
    public boolean equals(Object o) {
        if (o.getClass() != Shelter.class)
            return false;
        
        Shelter temp = (Shelter) o;
        
        return temp.getUID().equals(uid) && temp.getName().equals(name) && temp.getCapacityStr().equals(capacityStr) &&
                temp.getRestrictions().equals(restrictions) && temp.getAddress().equals(address) && temp.getNotes().equals(notes) &&
                temp.getPhone().equals(phone) && temp.getCapacityNum() == capacityNum && temp.getLat() == lat &&
                temp.getLon() == lon && temp.getGender().equals(gender) && temp.getAgeRest().equals(ageRest) && temp.getVeteran() == veteran &&
                alDeepCompare(reservations,temp.getReservations());
    }
    
    private boolean alDeepCompare(ArrayList<String> a1, ArrayList<String> a2) {
        if (a1 == null || a2 == null || a1.size() != a2.size())
            return false;
        
        for (String s: a1)
            if (!a2.contains(s))
                return false;
        
        return true;
    }
    
    public String getUID() {
        return uid;
    }
    public void setUID(String uid) {
        this.uid = uid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCapacityStr() {
        return capacityStr;
    }
    public void setCapacityStr(String capacityStr) {
        this.capacityStr = capacityStr;
    }
    public String getRestrictions() {
        return restrictions;
    }
    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public int getCapacityNum() {
        return capacityNum;
    }
    public void setCapacityNum(int capacityNum) {
        this.capacityNum = capacityNum;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLon() {
        return lon;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    public AgeRest getAgeRest() {
        return ageRest;
    }
    public void setAgeRest(AgeRest ageRest) {
        this.ageRest = ageRest;
    }
    public boolean getVeteran() {
        return veteran;
    }
    public void setVeteran(boolean veteran) {
        this.veteran = veteran;
    }
    public ArrayList<String> getReservations() {
        return reservations;
    }
    public void setReservations(ArrayList<String> reservations) {
        this.reservations = reservations;
    }

    public enum Gender {
        MALE, FEMALE, ALL;
    }
    
    public enum AgeRest {
        FAMILIESWITHNEWBORNS, CHILDREN, YOUNGADULTS, ALL;
    }
    
    private String uid, name, capacityStr, restrictions, address, notes, phone;
    private int capacityNum;
    private double lat, lon;
    private Gender gender;
    private AgeRest ageRest;
    private boolean veteran;
    private ArrayList<String> reservations;
}
