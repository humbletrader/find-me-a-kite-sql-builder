package com.github.humbletrader.fmak.criteria;

import com.github.humbletrader.fmak.query.FmakColumn;
import com.github.humbletrader.fmak.tables.ProductAttributesTable;
import com.github.humbletrader.fmak.tables.ProductTable;
import com.github.humbletrader.fmak.tables.ShopTable;

import java.util.HashMap;
import java.util.Map;

public enum SupportedFilter {
    country("country", ShopTable.country, 10_000),

    category("category", ProductTable.category, 20_000),
    brand("brand", ProductTable.brand, 5_000),
    product_name("product_name", ProductTable.product_name, 1_000),
    subprod_name("subprod_name", ProductTable.subprod_name, 10),
    version("version", ProductTable.version, 5),
    condition("condition", ProductTable.condition, 3_000),
    year("year", ProductTable.year, 2_000),

    size("size", ProductAttributesTable.size, 500),
    price("price", ProductAttributesTable.price, 300);

    //inspired by this: https://stackoverflow.com/questions/27703119/convert-from-string-to-a-java-enum-with-large-amount-of-values/27703839#27703839
    private static class Holder {
        static Map<String, SupportedFilter> STRING_TO_FILTER = new HashMap<>();
    }


    private final String nameInWebsite;
    private final FmakColumn column;
    private final int priorityInSqlWhereClause;

    SupportedFilter(String nameInWebsite, FmakColumn column, int priorityInSqlWhereClause){
        this.nameInWebsite = nameInWebsite;
        this.column = column;
        this.priorityInSqlWhereClause = priorityInSqlWhereClause;
        Holder.STRING_TO_FILTER.put(nameInWebsite, this);
    }

    public FmakColumn getColumn(){
        return column;
    }

    public int getPriorityInSqlWhereClause(){
        return priorityInSqlWhereClause;
    }

    public String getNameInWebsite(){
        return nameInWebsite;
    }

    public static SupportedFilter filterFromName(String filterNameInWeb){
        return Holder.STRING_TO_FILTER.get(filterNameInWeb);
    }
}
