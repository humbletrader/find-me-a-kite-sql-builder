package com.github.humbletrader.fmak.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

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

        StringBuilder selectString = new StringBuilder("select distinct");
        selectString.append(prefixedColumn(column));

        boolean isColumnFromProductAttributes = isProductAttributeTableColumn(column);
        StringBuilder fromString = new StringBuilder(" from products p");
        fromString.append(" inner join shops s on s.id = p.shop_id");
        if(isColumnFromProductAttributes || !Sets.intersection(criteria.keySet(), PRODUCT_ATTRIBUTES_COLUMNS).isEmpty()){
            fromString.append(" inner join product_attributes a on p.id = a.product_id");
        }
        selectString.append(fromString);

        ParameterizedStatement whereParameterizedStatement = whereFromCriteria(criteria);
        selectString.append(whereParameterizedStatement.getSqlWithoutParameters());
        selectString.append(avoidForbiddenValues(column));
        selectString.append(" order by ").append(column);
        return new ParameterizedStatement(selectString.toString(), whereParameterizedStatement.getParamValues());
    }


    /**
     * builds the sql statement to retrieve the db items for the given criteria
     * @param criteria  the criteria (ie. brand=DUOTONE, etc)
     * @param page  the new page requested
     * @return  the sql to be executed against the db
     */
    public ParameterizedStatement buildSearchSql(Map<String, SearchValAndOp> criteria, int page) {
        //"brand_name_version", "link", "price", "size"
        StringBuilder select = new StringBuilder("select");
        select.append(" p.brand_name_version, p.link, a.price, a.size, p.condition, p.visible_to_public");
        select.append(" from products p");
        select.append(" inner join shops s on s.id = p.shop_id");
        select.append(" inner join product_attributes a on p.id = a.product_id");

        ParameterizedStatement whereStatement = whereFromCriteria(criteria);
        select.append(whereStatement.getSqlWithoutParameters());
        List<Object> valuesForParameters = whereStatement.getParamValues();

        int startOfPage = page * rowsPerPage;
        select.append(" order by a.price limit ? offset ?");
        valuesForParameters.add(rowsPerPage + 1); //request one more row to detect if there is a next page available
        valuesForParameters.add(startOfPage);

        return new ParameterizedStatement(select.toString(), valuesForParameters);
    }

    /**
     * builds the where clause of the sql for the given criteria
     * @param criteria  the criteria
     * @return  a part of sql with the "where" clause
     */
    ParameterizedStatement whereFromCriteria(Map<String, SearchValAndOp> criteria){
        ParamStmtBuilder result = new ParamStmtBuilder();
        result.append(" where");

        //first we build sql for the mandatory params ( category, country )
        SearchValAndOp categoryValueAndOp = criteria.get("category");
        result.append(" p.category")
                .append(buildSqlOperatorFor(categoryValueAndOp), categoryValueAndOp.values());

        SearchValAndOp countryValueAndOp = criteria.get("country");
        result.append(" and s.country")
                .append(buildSqlOperatorFor(countryValueAndOp), countryValueAndOp.values());

        //then we check the rest
        for (Map.Entry<String, SearchValAndOp> currentCriteria : criteria.entrySet()) {
            String currentKey = currentCriteria.getKey();
            SearchValAndOp currentValAndOp = currentCriteria.getValue();
            if(!currentKey.equals("category") && !currentKey.equals("country")){
                result.append(" and").append(prefixedColumn(currentKey))
                        .append(buildSqlOperatorFor(currentValAndOp), currentValAndOp.values());
//                if(currentKey.equals("year")){
//                    //year is an integer in DB it needs special tretment
//                    valuesForParameters.add(Integer.valueOf(currentValAndOp.value()));
//                }else{
//                    valuesForParameters.add(currentValAndOp.value());
//                }
            }
        }
        return result.build();
    }


    private String buildSqlOperatorFor(SearchValAndOp searchValAndOp){
        var sqlOp = SqlOperators.forJs(searchValAndOp.op());
        return switch(sqlOp){
            case ANY -> " in ( ? )";
            default -> " " + sqlOp.getSqlOperator() + " ?";
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
