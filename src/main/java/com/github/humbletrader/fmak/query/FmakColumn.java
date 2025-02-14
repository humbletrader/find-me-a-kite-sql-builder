package com.github.humbletrader.fmak.query;

public interface FmakColumn {
    String colName();
    SqlType sqlType();
    FmakTable table();

    default String prefixedColumnName(){
        return table().prefix() + "." + colName();
    }
}
