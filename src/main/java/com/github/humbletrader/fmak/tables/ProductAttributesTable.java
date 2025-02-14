package com.github.humbletrader.fmak.tables;

import com.github.humbletrader.fmak.query.FmakColumn;
import com.github.humbletrader.fmak.query.FmakTable;
import com.github.humbletrader.fmak.query.SqlType;

import static com.github.humbletrader.fmak.query.SqlType.DOUBLE_TYPE;
import static com.github.humbletrader.fmak.query.SqlType.VARCHAR_TYPE;

public enum ProductAttributesTable implements FmakColumn {

    price("price", DOUBLE_TYPE),
    size("size", VARCHAR_TYPE)
    ;

    private final String colName;
    private final SqlType sqlType;

    ProductAttributesTable(String colName, SqlType sqlType) {
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
        return FmakTable.PRODUCT_ATTRIBUTES;
    }

}
