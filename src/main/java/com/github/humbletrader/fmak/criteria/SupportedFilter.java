package com.github.humbletrader.fmak.criteria;

import com.github.humbletrader.fmak.query.FmakColumn;
import com.github.humbletrader.fmak.query.FmakTables;
import com.github.humbletrader.fmak.query.RenameMeTable;
import com.github.humbletrader.fmak.tables.ProductAttributesTableColumns;
import com.github.humbletrader.fmak.tables.ProductTableColumns;
import com.github.humbletrader.fmak.tables.ShopTableColumns;

public enum SupportedFilter {
    country("country", ShopTableColumns.country),

    category("category", ProductTableColumns.category),
    brand("brand", ProductTableColumns.brand),
    product_name("product_name", ProductTableColumns.product_name),
    subprod_name("subprod_name", ProductTableColumns.subprod_name),
    version("version", ProductTableColumns.version),
    condition("condition", ProductTableColumns.condition),
    year("year", ProductTableColumns.year),

    size("size", ProductAttributesTableColumns.size),
    price("price", ProductAttributesTableColumns.price);


    private String nameInWebsite;
    private FmakColumn column;

    SupportedFilter(String nameInWebsite, FmakColumn column){
        this.nameInWebsite = nameInWebsite;
        this.column = column;
    }

    public FmakColumn getColumn(){
        return column;
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
