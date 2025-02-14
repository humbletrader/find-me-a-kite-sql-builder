package com.github.humbletrader.fmak.criteria;

import com.github.humbletrader.fmak.query.FmakColumn;
import com.github.humbletrader.fmak.tables.ProductAttributesTable;
import com.github.humbletrader.fmak.tables.ProductTable;
import com.github.humbletrader.fmak.tables.ShopTable;

public enum SupportedFilter {
    country("country", ShopTable.country),

    category("category", ProductTable.category),
    brand("brand", ProductTable.brand),
    product_name("product_name", ProductTable.product_name),
    subprod_name("subprod_name", ProductTable.subprod_name),
    version("version", ProductTable.version),
    condition("condition", ProductTable.condition),
    year("year", ProductTable.year),

    size("size", ProductAttributesTable.size),
    price("price", ProductAttributesTable.price);


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
