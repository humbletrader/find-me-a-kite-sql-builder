package com.github.humbletrader.fmak.query;

import com.github.humbletrader.fmak.criteria.FilterOpVal;
import com.github.humbletrader.fmak.criteria.SupportedFilter;
import com.github.humbletrader.fmak.tables.ProductAttributesTable;
import com.github.humbletrader.fmak.tables.ProductTable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 *  CONVENTION: the resulting sql is lower case
 */
public class FmakSqlBuilder {

    public final int rowsPerPage;

    /**
     * creates a new instance of the sql builder
     * @param rowsPerPage rows per page configuration
     */
    public FmakSqlBuilder(int rowsPerPage){
        this.rowsPerPage = rowsPerPage;
    }

    private SequencedSet<FilterOpVal> webFiltersToInternalFilters(Map<String, SequencedSet<SearchValAndOp>> webFilters){
        return webFilters.entrySet()
                .stream()
                .map(entry ->
                    new FilterOpVal(
                            SupportedFilter.filterFromName(entry.getKey()),
                            entry.getValue()
                    )
                )
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public ParameterizedStatement buildDistinctValuesSql(Map<String, SequencedSet<SearchValAndOp>> criteria,
                                                         String column){
        var filters = webFiltersToInternalFilters(criteria);
        return buildDistinctValuesSql(filters, SupportedFilter.filterFromName(column));
    }

    /**
     * builds the sql for 'distinct values call"
     * @param criteria the criteria for the search
     * @param distinctColumn    the column for which we check the distinct values
     * @return  the sql statement to be executed in order to get the distinct values from db
     */
    public ParameterizedStatement buildDistinctValuesSql(SequencedSet<FilterOpVal> criteria,
                                                         SupportedFilter distinctColumn){
        ParamStmtBuilder selectStatement = new ParamStmtBuilder()
                .append("select distinct")
                .append(" ").append(distinctColumn.getColumn().prefixedColumnName())
                .append(" from products p")
                .append(" inner join shops s on s.id = p.shop_id");

        if(FmakTable.PRODUCT_ATTRIBUTES == distinctColumn.getColumn().table() ||
                criteria.stream().anyMatch(filterOperatorValues -> filterOperatorValues.filter().getColumn().table().equals(FmakTable.PRODUCT_ATTRIBUTES))){
            selectStatement.append(" inner join product_attributes a on p.id = a.product_id");
        }

        selectStatement
                .append(whereFromCriteria(criteria))
                .append(avoidForbiddenValues(distinctColumn.getColumn()))
                .append(" order by ").append(distinctColumn.getColumn().prefixedColumnName());
        return selectStatement.build();
    }

    public ParameterizedStatement buildSearchSqlForWebFilters(Map<String, SequencedSet<SearchValAndOp>> criteria, int page) {
        var filters = webFiltersToInternalFilters(criteria);
        return buildSearchSql(filters, page);
    }


    /**
     * builds the sql statement to retrieve the db items for the given criteria
     * @param criteria  the criteria (ie. brand=DUOTONE, etc)
     * @param page  the new page requested
     * @return  the sql to be executed against the db
     */
    public ParameterizedStatement buildSearchSql(SequencedSet<FilterOpVal> criteria, int page) {
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
    ParamStmtBuilder whereFromCriteria(SequencedSet<FilterOpVal> criteria){
        ParamStmtBuilder result = new ParamStmtBuilder();
        result.append(" where");

        //first we build sql for the mandatory params ( category, country )
        Set<SearchValAndOp> categoryValuesAndOps = criteria.stream()
                .filter(filterOpVal -> filterOpVal.filter().equals(SupportedFilter.category))
                .findFirst()
                .map(FilterOpVal::values)
                .orElseThrow(() -> new RuntimeException("category is mandatory"));
        result.append(" ").append(SupportedFilter.category.getColumn().prefixedColumnName());
        categoryValuesAndOps.forEach(categoryValAndOp ->
                result.append(buildSqlOperatorFor(SupportedFilter.category.getColumn(), categoryValAndOp))
        );

        Set<SearchValAndOp> countryValuesAndOps = criteria.stream()
                .filter(filterOpVal -> filterOpVal.filter().equals(SupportedFilter.country))
                .findFirst()
                .map(FilterOpVal::values)
                .orElseThrow(() -> new RuntimeException("country is mandatory"));
        result.append(" and ").append(SupportedFilter.country.getColumn().prefixedColumnName());
        countryValuesAndOps.forEach(countryValAndOp ->
                result.append(buildSqlOperatorFor(SupportedFilter.country.getColumn(), countryValAndOp))
        );

        //then we check the rest
        criteria.forEach(currentCriteria -> {
            SupportedFilter currentKey = currentCriteria.filter();
            Set<SearchValAndOp> currentValuesAndOps = currentCriteria.values();
            currentValuesAndOps.forEach(currentValAndOp -> {
                //avoid the two filters already processed
                if (!currentKey.equals(SupportedFilter.category) && !currentKey.equals(SupportedFilter.country)) {
                    FmakColumn column = currentKey.getColumn();
                    result.append(" and ")
                            .append(column.prefixedColumnName())
                            .append(buildSqlOperatorFor(column, currentValAndOp));
                }
            });
        });
        return result;
    }

    private ParamStmtBuilder buildSqlOperatorFor(FmakColumn column, SearchValAndOp searchValAndOp){
        ParamStmtBuilder result = new ParamStmtBuilder();
        var sqlOp = SqlOperators.forJs(searchValAndOp.op());
        return switch(sqlOp){
            case ANY -> result.append(" in ( ? )", searchValAndOp.value(), column.sqlType());
            default -> result.append(" " + sqlOp.getSqlOperator() + " ?", searchValAndOp.value(), column.sqlType());
        };
    }

    @Deprecated //todo: as the parser gets better we don't need this method anymore
    String avoidForbiddenValues(FmakColumn column){
        return
                switch(column){
                    case ProductTable.brand -> " and brand <> 'unknown'"; //no longer needed
                    case ProductTable.year -> " and year <> -1 and year <> -2"; //still needed
                    case ProductTable.version -> " and version <> 'not needed' and version <> 'unknown'"; //is this still needed ?
                    case ProductAttributesTable.size -> " and size <> 'unknown'"; //no longer needed
                    case ProductTable.product_name, ProductTable.condition, ProductTable.subprod_name, ProductAttributesTable.price -> "";
                    default -> throw new RuntimeException("impossible to avoid forbidden values for column " + column);
                };
    }
}
