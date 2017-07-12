package com.xtt.log.trace;

import java.io.Serializable;

public class CryptType implements Serializable{
    private static final long serialVersionUID = 1L;
    private String value;
    public CryptType (){
    }
    public CryptType(String value){
        this.value=value;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return value;
    }
}
