package com.app.project.client;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import java.util.List;

@Entity(tableName = "clients")
public class Client {

    @PrimaryKey(autoGenerate = true)
    public long clientId;
    
    public String name;
    public String phone;
    
    
    public long getId() {
        return clientId;
    }
    public void setId(long clientId) {
        this.clientId = clientId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }


    
}