package com.github.humbletrader.fmak.query;

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

public class FmakSqlBuilder {

    private final static Set<String> PRODUCT_ATTRIBUTES_COLUMNS = Set.of("price", "size", "color");

    public final int rowsPerPage;

    /**
     * creates a new instance of the sql builder
     * @param rowsPerPage rows per page configuration
     */
    public FmakSqlBuilder(int rowsPerPage){
        this.rowsPerPage = rowsPerPage;
    }

    /**
     * builds the sql for 'distinct values call"
     * @param criteria the criteria for the search
     * @param column    the column for which we check the distinct values
     * @return  the sql statement to be executed in order to get the distinct values from db
     */
    public ParameterizedStatement buildDistinctValuesSql(Map<String, SearchValAndOp> criteria,
                                                         String column){
        ParamStmtBuilder selectStatement = new ParamStmtBuilder()
                .append("select distinct")
                .append(prefixedColumn(column))
                .append(" from products p")
                .append(" inner join shops s on s.id = p.shop_id");

        boolean isColumnFromProductAttributes = isProductAttributeTableColumn(column);
        if(isColumnFromProductAttributes || !Sets.intersection(criteria.keySet(), PRODUCT_ATTRIBUTES_COLUMNS).isEmpty()){
            selectStatement.append(" inner join product_attributes a on p.id = a.product_id");
        }

        selectStatement
                .append(whereFromCriteria(criteria))
                .append(avoidForbiddenValues(column))
                .append(" order by ").append(column);
        return selectStatement.build();
    }


    /**
     * builds the sql statement to retrieve the db items for the given criteria
     * @param criteria  the criteria (ie. brand=DUOTONE, etc)
     * @param page  the new page requested
     * @return  the sql to be executed against the db
     */
    public ParameterizedStatement buildSearchSql(Map<String, SearchValAndOp> criteria, int page) {
        //"brand_name_version", "link", "price", "size"
        ParamStmtBuilder select = new ParamStmtBuilder()
                .append("select")
                .append(" p.brand_name_version, p.link, a.price, a.size, p.condition, p.visible_to_public")
                .append(" from products p")
                .append(" inner join shops s on s.id = p.shop_id")
                .append(" inner join product_attributes a on p.id = a.product_id")
                .append(whereFromCriteria(criteria))
                .append(" order by a.price limit ?", rowsPerPage+1) //request one more row to detect if there is a next page available
                .append(" offset ?",page * rowsPerPage );
        return select.build();
    }

    /**
     * builds the where clause of the sql for the given criteria
     * @param criteria  the criteria
     * @return  a part of sql with the "where" clause
     */
    ParamStmtBuilder whereFromCriteria(Map<String, SearchValAndOp> criteria){
        ParamStmtBuilder result = new ParamStmtBuilder();
        result.append(" where");

        //first we build sql for the mandatory params ( category, country )
        SearchValAndOp categoryValueAndOp = criteria.get("category");
        result.append(" p.category")
                .append(buildSqlOperatorFor(categoryValueAndOp));

        SearchValAndOp countryValueAndOp = criteria.get("country");
        result.append(" and s.country")
                .append(buildSqlOperatorFor(countryValueAndOp));

        //then we check the rest
        for (Map.Entry<String, SearchValAndOp> currentCriteria : criteria.entrySet()) {
            String currentKey = currentCriteria.getKey();
            SearchValAndOp currentValAndOp = currentCriteria.getValue();
            if(!currentKey.equals("category") && !currentKey.equals("country")){
                result.append(" and").append(prefixedColumn(currentKey))
                        .append(buildSqlOperatorFor(currentValAndOp, currentKey.equals("year")));
            }
        }
        return result;
    }


    private ParamStmtBuilder buildSqlOperatorFor(SearchValAndOp searchValAndOp){
        ParamStmtBuilder result = new ParamStmtBuilder();
        var sqlOp = SqlOperators.forJs(searchValAndOp.op());
        return switch(sqlOp){
            case ANY -> result.append(" in ( ? )", searchValAndOp.value());
            default -> result.append(" " + sqlOp.getSqlOperator() + " ?", searchValAndOp.value());
        };
    }

    @Deprecated
    private ParamStmtBuilder buildSqlOperatorFor(SearchValAndOp searchValAndOp, boolean castToInteger){
        ParamStmtBuilder result = new ParamStmtBuilder();
        Object sqlValue = castToInteger ? Integer.valueOf(searchValAndOp.value()) : searchValAndOp.value();
        var sqlOp = SqlOperators.forJs(searchValAndOp.op());
        return switch(sqlOp){
            case ANY -> result.append(" in ( ? )", sqlValue);
            default -> result.append(" " + sqlOp.getSqlOperator() + " ?", sqlValue);
        };
    }

    private String prefixedColumn(String column){
        if (isProductAttributeTableColumn(column)) {
            return " a."+column;
        }else{
            return " p."+column;
        }
    }

    private boolean isProductAttributeTableColumn(String colName){
        return PRODUCT_ATTRIBUTES_COLUMNS.contains(colName);
    }

    @Deprecated //todo: as the parser gets better we don't need this method anymore
    String avoidForbiddenValues(String column){
        return
                switch(column){
                    case "brand" -> " and brand <> 'unknown'"; //no longer needed
                    case "year" -> " and year <> -1 and year <> -2"; //still needed
                    case "version" -> " and version <> 'not needed' and version <> 'unknown'"; //is this still needed ?
                    case "size" -> " and size <> 'unknown'"; //no longer needed
                    case "product_name", "condition", "subprod_name" -> "";
                    default -> throw new RuntimeException("impossible to avoid forbidden values for column " + column);
                };
    }


}
