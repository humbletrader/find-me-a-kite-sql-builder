package com.github.humbletrader.fmak.query;


import java.util.List;

public class ParameterizedStatement {

    private final String sqlWithoutParameters;
    private final List<Object> paramValues;


    public ParameterizedStatement(String sqlWithoutParameters, List<Object> values){
        this.sqlWithoutParameters = sqlWithoutParameters;
        this.paramValues = values;
    }

    public String getSqlWithoutParameters() {
        return sqlWithoutParameters;
    }

    public List<Object> getParamValues() {
        return paramValues;
    }

    @Override
    public String toString() {
        return "ParameterizedStatement{" +
                "sqlWithoutParameters='" + sqlWithoutParameters + '\'' +
                ", paramValues=" + paramValues +
                '}';
    }
}

