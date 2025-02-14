package com.github.humbletrader.fmak.criteria;

import com.github.humbletrader.fmak.query.FmakColumn;
import com.github.humbletrader.fmak.tables.ProductAttributesTable;
import com.github.humbletrader.fmak.tables.ProductTable;
import com.github.humbletrader.fmak.tables.ShopTable;

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


    private String nameInWebsite;
    private FmakColumn column;
    private int priorityInSqlWhereClause;

    SupportedFilter(String nameInWebsite, FmakColumn column, int priorityInSqlWhereClause){
        this.nameInWebsite = nameInWebsite;
        this.column = column;
        this.priorityInSqlWhereClause = priorityInSqlWhereClause;
    }

    public FmakColumn getColumn(){
        return column;
    }

    public int getPriorityInSqlWhereClause(){
        return priorityInSqlWhereClause;
    }

    public static SupportedFilter filterFromName(String filterNameInWeb){
        //todo: use a map instead of iterating
        for(SupportedFilter filter : SupportedFilter.values()){
            if(filter.nameInWebsite.equals(filterNameInWeb)){
                return filter;
            }
        }
        return null;
    }
}
