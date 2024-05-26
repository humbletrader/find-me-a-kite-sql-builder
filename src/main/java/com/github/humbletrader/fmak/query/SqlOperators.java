package com.github.humbletrader.fmak.query;

public enum SqlOperators {
    EQ("eq", "="),
    NE("ne", "!="),
    GT("gt", ">"),
    GTE("gte", ">="),
    LT("lt", "<"),
    LTE("lte", "<="),
    ANY("any", "in");

    private final String jsOperator;
    private final String sqlOperator;

    SqlOperators(String jsOperator, String sqlOperator){
        this.jsOperator = jsOperator;
        this.sqlOperator = sqlOperator;
    }

    public String getJsOperator(){
        return jsOperator;
    }

    public String getSqlOperator(){
        return sqlOperator;
    }

    public static SqlOperators forJs(String jsOperator){
        for(SqlOperators operator : SqlOperators.values()){
            if(operator.getJsOperator().equals(jsOperator)){
                return operator;
            }
        }
        throw new IllegalArgumentException("No operator found for js operator: " + jsOperator);
    }
}
