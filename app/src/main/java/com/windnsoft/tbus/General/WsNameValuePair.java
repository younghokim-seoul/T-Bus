package com.windnsoft.tbus.General;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class WsNameValuePair {
    String name;
    String value;

    public WsNameValuePair(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName(){
        return name;
    }

    public String getValue(){
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
