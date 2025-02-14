package com.github.humbletrader.fmak.query;

import com.github.humbletrader.fmak.tables.ProductAttributesTableColumns;
import com.github.humbletrader.fmak.tables.ProductTableColumns;
import com.github.humbletrader.fmak.tables.ShopTableColumns;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.humbletrader.fmak.query.SqlType.*;

public enum FmakTables {

    SHOPS(Set.of(ShopTableColumns.values()), "s"),
    PRODUCTS(Set.of(ProductTableColumns.values()), "p"),
    PRODUCT_ATTRIBUTES(Set.of(ProductAttributesTableColumns.values()), "a")
    ;

    /**
     * the list of user-facing columns
     */
    private final Map<String, FmakColumn> publicColumns;

    private final String prefixInSql;

    FmakTables(Set<FmakColumn> columns, String prefixInSql){
        this.prefixInSql= prefixInSql;
        this.publicColumns = columns.stream().map(c ->
                new AbstractMap.SimpleImmutableEntry<>(c.colName(), c)
        ).collect(Collectors.toMap(
                k -> k.getKey(),
                c -> c.getValue()
        ));
    }

    public Set<String> getColumnNames(){
        return publicColumns.keySet();
    }

    public boolean hasColumn(String columnName){
        return publicColumns.containsKey(columnName);
    }

    /**
     * may return null if the column cannot be found
     * @param columnName
     * @return
     */
    public FmakColumn getColumn(String columnName){
        return publicColumns.get(columnName);
    }

    public String getSqlPrefix(){
        return prefixInSql;
    }

}
