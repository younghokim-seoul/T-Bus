package com.windnsoft.tbus.Http;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class ResponseMessageEntity {
    public int code;
    public String msg;

    public ResponseMessageEntity(){}

    public ResponseMessageEntity(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
