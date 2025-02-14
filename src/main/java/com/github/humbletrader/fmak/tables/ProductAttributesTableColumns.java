package com.github.humbletrader.fmak.tables;

import com.github.humbletrader.fmak.query.FmakColumn;
import com.github.humbletrader.fmak.query.RenameMeTable;
import com.github.humbletrader.fmak.query.SqlType;

import static com.github.humbletrader.fmak.query.SqlType.DOUBLE_TYPE;
import static com.github.humbletrader.fmak.query.SqlType.VARCHAR_TYPE;

public enum ProductAttributesTableColumns implements FmakColumn {

    price("price", DOUBLE_TYPE),
    size("size", VARCHAR_TYPE)
    ;

    private final String colName;
    private final SqlType sqlType;

    ProductAttributesTableColumns(String colName, SqlType sqlType) {
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
    public RenameMeTable table() {
        return RenameMeTable.PRODUCT_ATTRIBUTES;
    }

}
