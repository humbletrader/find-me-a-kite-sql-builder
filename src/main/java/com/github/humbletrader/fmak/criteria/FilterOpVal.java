package com.github.humbletrader.fmak.criteria;

import com.github.humbletrader.fmak.query.SearchValAndOp;

import java.util.SequencedSet;

public record FilterOpVal(SupportedFilter filter, SequencedSet<SearchValAndOp> values) {
}
