package com.github.humbletrader.fmak.query;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FmakSqlBuilderTest {

    private FmakSqlBuilder underTest = new FmakSqlBuilder(20);

    @Test
    public void sqlBuildForTwoParametersNonProductAttributes(){
        Map<String, SequencedSet<SearchValAndOp>> filters = new HashMap<>();
        filters.put("category", new LinkedHashSet<>(List.of(new SearchValAndOp("KITES", "eq"))));
        filters.put("country", new LinkedHashSet<>(List.of(new SearchValAndOp("EU", "eq"))));
        filters.put("product_name", new LinkedHashSet<>(List.of(new SearchValAndOp("cabrinha", "eq"))));

        ParameterizedStatement result = underTest.buildDistinctValuesSql(filters,   "product_name");
        assertEquals(
                "select distinct p.product_name "+
                        "from products p "+
                        "inner join shops s on s.id = p.shop_id "+
                        "where p.category = ? "+
                        "and s.country = ? "+
                        "and p.product_name = ? "+
                        "order by p.product_name",
                result.getSqlWithoutParameters()
        );
        assertEquals(Arrays.asList("KITES", "EU", "cabrinha"), result.getParamValues());
    }

    @Test
    public void sqlShouldContainAJoinWhenSizeIsInFilter(){
        Map<String, SequencedSet<SearchValAndOp>> filters = new HashMap<>();
        filters.put("category", new LinkedHashSet<>(List.of(new SearchValAndOp("KITES", "eq"))));
        filters.put("country", new LinkedHashSet<>(List.of(new SearchValAndOp("EU", "eq"))));
        filters.put("product_name", new LinkedHashSet<>(List.of(new SearchValAndOp("cabrinha", "eq"))));
        filters.put("size", new LinkedHashSet<>(List.of(new SearchValAndOp("10", "eq"))));

        ParameterizedStatement result = underTest.buildDistinctValuesSql(filters, "product_name");
        assertEquals(
                "select distinct p.product_name " +
                        "from products p "+
                        "inner join shops s on s.id = p.shop_id "+
                        "inner join product_attributes a on p.id = a.product_id " +
                        "where p.category = ? "+
                        "and s.country = ? " +
                        "and p.product_name = ? "+
                        "and a.size = ? "+
                        "order by p.product_name",
                result.getSqlWithoutParameters()
        );
        assertEquals(List.of("KITES", "EU", "cabrinha", "10"), result.getParamValues());
    }

    @Test
    public void distinctSize(){
        Map<String, SequencedSet<SearchValAndOp>> filters = new HashMap<>();
        filters.put("category", new LinkedHashSet<>(List.of(new SearchValAndOp("KITES", "eq"))));
        filters.put("country", new LinkedHashSet<>(List.of(new SearchValAndOp("US", "eq"))));

        ParameterizedStatement result = underTest.buildDistinctValuesSql(filters, "size");
        assertEquals(
                "select distinct a.size from products p " +
                        "inner join shops s on s.id = p.shop_id " +
                        "inner join product_attributes a on p.id = a.product_id " +
                        "where p.category = ? "+
                        "and s.country = ? " +
                        "order by a.size",
                result.getSqlWithoutParameters()
        );
        assertEquals(List.of("KITES", "US"), result.getParamValues());
    }

    @Test
    public void distinctConstructionTech(){
        Map<String, SequencedSet<SearchValAndOp>> filters = new HashMap<>();
        filters.put("category", new LinkedHashSet<>(List.of(new SearchValAndOp("KITES", "eq"))));
        filters.put("country", new LinkedHashSet<>(List.of(new SearchValAndOp("US", "eq"))));

        ParameterizedStatement result = underTest.buildDistinctValuesSql(filters, "construction_tech");
        assertEquals(
                "select distinct p.construction_tech from products p " +
                        "inner join shops s on s.id = p.shop_id " +
                        "where p.category = ? "+
                        "and s.country = ? " +
                        "order by p.construction_tech",
                result.getSqlWithoutParameters()
        );
        assertEquals(List.of("KITES", "US"), result.getParamValues());
    }

    @Test
    public void searchSql(){
        Map<String, SequencedSet<SearchValAndOp>> filters = new HashMap<>();
        filters.put("category", new LinkedHashSet<>(List.of(new SearchValAndOp("KITES", "eq"))));
        filters.put("country", new LinkedHashSet<>(List.of(new SearchValAndOp("UK", "eq"))));

        ParameterizedStatement result = underTest.buildSearchSqlForWebFilters(filters, 2);
        assertEquals("select p.brand_name_version, p.link, a.price, a.size, p.condition, p.visible_to_public "+
                        "from products p " +
                        "inner join shops s on s.id = p.shop_id "+
                        "inner join product_attributes a on p.id = a.product_id " +
                        "where p.category = ? " +
                        "and s.country = ? "+
                        "order by a.price limit ? offset ?",
                result.getSqlWithoutParameters()
        );
        assertEquals(Arrays.asList("KITES", "UK", 21, 40), result.getParamValues());
    }

    @Test
    public void searchSqlWithIntegerParameters(){
        Map<String, SequencedSet<SearchValAndOp>> filters = new HashMap<>();
        filters.put("category", new LinkedHashSet<>(List.of(new SearchValAndOp("KITES", "eq"))));
        filters.put("country", new LinkedHashSet<>(List.of(new SearchValAndOp("USA", "eq"))));
        filters.put("size", new LinkedHashSet<>(List.of(new SearchValAndOp("15.0", "gt"), new SearchValAndOp("17.0", "lt"))));
        filters.put("year", new LinkedHashSet<>(List.of(new SearchValAndOp("2022", "eq"))));

        ParameterizedStatement result = underTest.buildSearchSqlForWebFilters(filters, 1);
        assertEquals("select p.brand_name_version, p.link, a.price, a.size, p.condition, p.visible_to_public "+
                        "from products p " +
                        "inner join shops s on s.id = p.shop_id "+
                        "inner join product_attributes a on p.id = a.product_id " +
                        "where p.category = ? " +
                        "and s.country = ? "+
                        "and p.year = ? "+
                        "and a.size > ? "+
                        "and a.size < ? "+
                        "order by a.price limit ? offset ?",
                result.getSqlWithoutParameters()
        );
        assertEquals(Arrays.asList("KITES", "USA", 2022, "15.0", "17.0",  21, 20), result.getParamValues());
    }

    @Test
    public void searchSqlWithGreaterThanOperator(){
        Map<String, SequencedSet<SearchValAndOp>> filters = new HashMap<>();
        filters.put("category", new LinkedHashSet<>(List.of(new SearchValAndOp("KITES", "eq"))));
        filters.put("country", new LinkedHashSet<>(List.of(new SearchValAndOp("USA", "eq"))));
        filters.put("size", new LinkedHashSet<>(List.of(new SearchValAndOp("17.0", "eq"))));
        filters.put("price", new LinkedHashSet<>(List.of(new SearchValAndOp("1000", "gt"))));

        ParameterizedStatement result = underTest.buildSearchSqlForWebFilters(filters, 1);
        assertEquals("select p.brand_name_version, p.link, a.price, a.size, p.condition, p.visible_to_public "+
                        "from products p " +
                        "inner join shops s on s.id = p.shop_id "+
                        "inner join product_attributes a on p.id = a.product_id " +
                        "where p.category = ? " +
                        "and s.country = ? "+
                        "and a.size = ? "+
                        "and a.price > ? "+
                        "order by a.price limit ? offset ?",
                result.getSqlWithoutParameters()
        );
        assertEquals(Arrays.asList("KITES", "USA", "17.0", 1000.0, 21, 20), result.getParamValues());
    }

}