package com.github.humbletrader.fmak.query;

import java.util.ArrayList;
import java.util.List;

public class ParamStmtBuilder {

    private StringBuilder sql;
    private List<Object> values;

    public ParamStmtBuilder(){
        sql = new StringBuilder();
        values = new ArrayList<>();
    }

    public ParamStmtBuilder append(String sqlPart){
        sql.append(sqlPart);
        return this;
    }

    public ParamStmtBuilder append(String sqlPart, List<Object> params){
        sql.append(sqlPart);
        values.addAll(params);
        return this;
    }

    public ParamStmtBuilder append(String sqlPart, Object param){
        sql.append(sqlPart);
        values.add(param);
        return this;
    }

    public ParamStmtBuilder append(ParamStmtBuilder another){
        sql.append(another.sql);
        values.addAll(another.values);
        return this;
    }

    public ParameterizedStatement build(){
        return new ParameterizedStatement(sql.toString(), values);
    }
}
