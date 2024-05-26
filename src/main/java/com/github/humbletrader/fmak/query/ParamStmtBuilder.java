package com.github.humbletrader.fmak.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this is a chunk of sql statement that may also have parameters
 * (which are depicted in the sql string as question marks ) and their value
 * is kept separate in a list of parameters
 */
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

    public ParamStmtBuilder append(String sqlPart,
                                   String stringParam){
        sql.append(sqlPart);
        values.add(stringParam);
        return this;
    }

    public ParamStmtBuilder append(String sqlPart,
                                   Integer intParam){
        sql.append(sqlPart);
        values.add(intParam);
        return this;
    }

    public ParamStmtBuilder append(String sqlPart,
                                   String paramStrValue,
                                   SqlType sqlType){
        sql.append(sqlPart);
        Object castedValue = switch (sqlType){
            case VARCHAR_TYPE -> paramStrValue;
            case INT_TYPE -> Integer.valueOf(paramStrValue);
            case DOUBLE_TYPE -> Double.valueOf(paramStrValue);
        };
        values.add(castedValue);
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
