package com.github.humbletrader.fmak.query;

public interface FmakColumn {
    String colName();
    SqlType sqlType();
    RenameMeTable table();

    default String prefixedColumnName(){
        return table().prefix() + "." + colName();
    }
}
