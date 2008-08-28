package com.meidusa.amoeba.oracle.accessor;

public class T4CCharAccessor extends CharAccessor {

    public String getString() {
        String s = super.getString();
        if (s != null && definedColumnSize > 0 && s.length() > definedColumnSize) {
            s = s.substring(0, definedColumnSize);
        }
        return s;
    }

}
