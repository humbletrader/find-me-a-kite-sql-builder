package com.github.humbletrader.fmak.query;

import java.util.Map;

import static com.github.humbletrader.fmak.query.Tables.*;
import static com.google.common.collect.Sets.intersection;


/**
 * CONVENTION: the resulting sql is lower case
 */
public class FmakSqlBuilder {

    public static final String ANY_OF_IN_JSON ="anyOf";

    private final int rowsPerPage;

    /**
     * creates a new instance of the sql builder
     *
     * @param rowsPerPage rows per page configuration
     */
    public FmakSqlBuilder(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    /**
     * builds the sql for 'distinct values call"
     *
     * @param criteria the criteria for the search
     * @param column   the column for which we check the distinct values
     * @return the sql statement to be executed in order to get the distinct values from db
     */
    public ParameterizedStatement buildDistinctValuesSql(Map<String, SearchValAndOp> criteria,
                                                         String column) {
        ParamStmtBuilder selectStatement = new ParamStmtBuilder()
                .append("select distinct")
                .append(prefixedColumn(column))
                .append(" from products p")
                .append(" inner join shops s on s.id = p.shop_id");

        if (PRODUCT_ATTRIBUTES.hasColumn(column) ||
                !intersection(criteria.keySet(), PRODUCT_ATTRIBUTES.getColumnNames()).isEmpty()) {
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
     *
     * @param criteria the criteria (ie. brand=DUOTONE, etc)
     * @param page     the new page requested
     * @return the sql to be executed against the db
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
                .append(" order by a.price limit ?", rowsPerPage + 1) //request one more row to detect if there is a next page available
                .append(" offset ?", page * rowsPerPage);
        return select.build();
    }

    /**
     * builds the where clause of the sql for the given criteria
     *
     * @param criteria the criteria
     * @return a part of sql with the "where" clause
     */
    ParamStmtBuilder whereFromCriteria(Map<String, SearchValAndOp> criteria) {
        ParamStmtBuilder result = new ParamStmtBuilder();
        result.append(" where");

        //first we build sql for the mandatory params ( category, country )
        SearchValAndOp categoryValueAndOp = criteria.get("category");
        result.append(whereConditionFor(PRODUCTS, PRODUCTS.getColumn("category"), categoryValueAndOp));

        SearchValAndOp countryValueAndOp = criteria.get("country");
        result.append(" and")
                .append(whereConditionFor(SHOPS, SHOPS.getColumn("country"), countryValueAndOp));

        //then we check the rest
        for (Map.Entry<String, SearchValAndOp> currentCriteria : criteria.entrySet()) {
            String currentKey = currentCriteria.getKey();
            SearchValAndOp currentValAndOp = currentCriteria.getValue();
            if (!currentKey.equals("category") && !currentKey.equals("country")) {
                Tables table = findTable(currentKey);
                Column column = table.getColumn(currentKey);
                result.append(" and")
                        .append(whereConditionFor(table, column, currentValAndOp));
            }
        }
        return result;
    }

    private ParamStmtBuilder whereConditionFor(String columnWithPrefix,
                                               String value,
                                               SqlOperators operator,
                                               SqlType sqlType) {
        ParamStmtBuilder result = new ParamStmtBuilder();
        result.append(" ").append(columnWithPrefix).append(" ").append(operator.getSqlOperator()).append(" ?", value, sqlType);
        return result;
    }

    private ParamStmtBuilder whereConditionFor(Tables table, Column column, SearchValAndOp searchValAndOp) {
        ParamStmtBuilder result = new ParamStmtBuilder();

        //any of transformed in multiple "or' operations because:
        // 1. PreparedStatement does not support any of with array of parameters
        // 2. performance concerns
        if (searchValAndOp.op().equals(ANY_OF_IN_JSON)) {
            //using for instead of stream because I don't have a zipWithIndex method ( and I don't want to import codepoetics protonpack )
            for (int i = 0; i < searchValAndOp.values().size(); i++) {
                String currentValue = searchValAndOp.values().get(i);
                if (i == 0) {
                    //first time we add the parenthesis
                    result.append(" (");
                } else {
                    //i > 0
                    result.append(" or");
                }
                result.append(whereConditionFor(table.getSqlPrefix() + "." + column.name(), currentValue, SqlOperators.EQ, column.sqlType()));
            }
            result.append(" )");
        } else {
            var sqlOp = SqlOperators.forJs(searchValAndOp.op());
            result = whereConditionFor(table.getSqlPrefix() + "." + column.name(), searchValAndOp.values().getFirst(), sqlOp, column.sqlType());
        }
        return result;
    }

    private String prefixedColumn(String column) {
        if (PRODUCT_ATTRIBUTES.hasColumn(column)) {
            return " " + PRODUCT_ATTRIBUTES.getSqlPrefix() + "." + column;
        } else {
            return " " + PRODUCTS.getSqlPrefix() + "." + column;
        }
    }

    @Deprecated
        //todo: as the parser gets better we don't need this method anymore
    String avoidForbiddenValues(String column) {
        return
                switch (column) {
                    case "brand" -> " and brand <> 'unknown'"; //no longer needed
                    case "year" -> " and year <> -1 and year <> -2"; //still needed
                    case "version" -> " and version <> 'not needed' and version <> 'unknown'"; //is this still needed ?
                    case "size" -> " and size <> 'unknown'"; //no longer needed
                    case "product_name", "condition", "subprod_name" -> "";
                    default -> throw new RuntimeException("impossible to avoid forbidden values for column " + column);
                };
    }

    private Tables findTable(String columnName) {
        Tables table = null;
        if (PRODUCT_ATTRIBUTES.hasColumn(columnName)) {
            table = PRODUCT_ATTRIBUTES;
        } else {
            if (SHOPS.hasColumn(columnName)) {
                table = SHOPS;
            } else {
                table = PRODUCTS;
            }
        }
        return table;
    }

}
