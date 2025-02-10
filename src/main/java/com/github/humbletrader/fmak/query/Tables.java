package com.github.humbletrader.fmak.query;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.humbletrader.fmak.query.SqlType.*;
import static java.util.Collections.emptySet;

public enum Tables {

    SHOPS(Set.of(
            new Column("country", VARCHAR_TYPE)), "s"
    ),
    PRODUCTS(Set.of(
            new Column("brand", VARCHAR_TYPE),
            new Column("product_name", VARCHAR_TYPE),
            new Column("version", VARCHAR_TYPE),
            new Column("year", INT_TYPE),
            new Column("link", VARCHAR_TYPE),
            new Column("category", VARCHAR_TYPE),
            new Column("condition", VARCHAR_TYPE),
            new Column("subprod_name", VARCHAR_TYPE)), "p"
    ),
    PRODUCT_ATTRIBUTES(Set.of(
            new Column("price", DOUBLE_TYPE),
            new Column("size", VARCHAR_TYPE)), "a"
    );

    /**
     * the list of user-facing columns
     */
    private final Map<String, Column> publicColumns;

    private final String prefixInSql;

    Tables(Set<Column> columns, String prefixInSql){
        this.prefixInSql= prefixInSql;
        this.publicColumns = columns.stream().map(c ->
                new AbstractMap.SimpleImmutableEntry<>(c.name(), c)
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
    public Column getColumn(String columnName){
        return publicColumns.get(columnName);
    }

    public String getSqlPrefix(){
        return prefixInSql;
    }

}
