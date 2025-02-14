package com.github.humbletrader.fmak.query;

public enum RenameMeTable {
    SHOPS("s"),
    PRODUCTS("p"),
    PRODUCT_ATTRIBUTES("a");

    private String prefixInSql;

    RenameMeTable(String prefixInSql){
        this.prefixInSql= prefixInSql;
    }

    public String prefix(){
        return prefixInSql;
    }
}
