package com.github.humbletrader.fmak.query;

import java.util.List;

public record SearchValAndOp(List<String> values, String op) {

    public SearchValAndOp(String value, String op){
        this(List.of(value), op);
    }

}
