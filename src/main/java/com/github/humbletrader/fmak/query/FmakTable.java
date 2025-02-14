package com.github.humbletrader.fmak.query;

public enum FmakTable {
    SHOPS("s"),
    PRODUCTS("p"),
    PRODUCT_ATTRIBUTES("a");

    private String prefixInSql;

    FmakTable(String prefixInSql){
        this.prefixInSql= prefixInSql;
    }

    public String prefix(){
        return prefixInSql;
    }
}
