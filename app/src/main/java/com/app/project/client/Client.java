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
}