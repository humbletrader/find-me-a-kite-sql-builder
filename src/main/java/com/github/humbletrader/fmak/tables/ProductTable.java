package com.github.humbletrader.fmak.tables;

import com.github.humbletrader.fmak.query.FmakColumn;
import com.github.humbletrader.fmak.query.FmakTable;
import com.github.humbletrader.fmak.query.SqlType;

import static com.github.humbletrader.fmak.query.SqlType.INT_TYPE;
import static com.github.humbletrader.fmak.query.SqlType.VARCHAR_TYPE;

public enum ProductTable implements FmakColumn {

    brand("brand", VARCHAR_TYPE),
    product_name("product_name", VARCHAR_TYPE),
    version("version", VARCHAR_TYPE),
    year("year", INT_TYPE),
    link("link", VARCHAR_TYPE),
    category("category", VARCHAR_TYPE),
    condition("condition", VARCHAR_TYPE),
    subprod_name("subprod_name", VARCHAR_TYPE),
    construction_tech("construction_tech", VARCHAR_TYPE);

    private final String colName;
    private final SqlType sqlType;

    ProductTable(String colName, SqlType sqlType) {
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
        return FmakTable.PRODUCTS;
    }
}
