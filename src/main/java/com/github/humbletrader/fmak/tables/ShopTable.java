package com.github.humbletrader.fmak.tables;

import com.github.humbletrader.fmak.query.FmakColumn;
import com.github.humbletrader.fmak.query.FmakTable;
import com.github.humbletrader.fmak.query.SqlType;

import static com.github.humbletrader.fmak.query.SqlType.VARCHAR_TYPE;

public enum ShopTable implements FmakColumn {

    country("country", VARCHAR_TYPE);

    private final String colName;
    private final SqlType sqlType;

    ShopTable(String colName, SqlType sqlType) {
        this.colName = colName;
        this.sqlType = sqlType;
    }

    @Override
    public String colName() {
        return colName;
    }

    @Override
    public SqlType sqlType() {
        return sqlType;
    }

    @Override
    public FmakTable table() {
        return FmakTable.SHOPS;
    }
}
