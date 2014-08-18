package org.xbib.query.cql.elasticsearch;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.rest.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.query.cql.CQLParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class QueryTests extends Assert {

    private final static ESLogger logger = ESLoggerFactory.getLogger(QueryTests.class.getName());

    @Test
    public void testValidQueries() throws IOException {
        test("/org/xbib/query/cql/elasticsearch/queries.txt");
    }

    @Test
    public void testSimpleTermFilter() throws Exception {
        String cql = "Jörg";
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        ElasticsearchFilterGenerator generator = new ElasticsearchFilterGenerator();
        parser.getCQLQuery().accept(generator);
        String json = generator.getResult().string();
        logger.info("{} --> {}", cql, json);
        assertEquals(json, "{\"term\":{\"cql.allIndexes\":\"Jörg\"}}");
    }

    @Test
    public void testFieldTermFilter() throws Exception {
        String cql = "dc.type = electronic";
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        ElasticsearchFilterGenerator generator = new ElasticsearchFilterGenerator();
        parser.getCQLQuery().accept(generator);
        String json = generator.getResult().string();
        logger.info("{} --> {}", cql, json);
        assertEquals(json, "{\"query\":{\"term\":{\"dc.type\":\"electronic\"}}}");
    }

    @Test
    public void testDoubleFieldTermFilter() throws Exception {
        String cql = "dc.type = electronic and dc.date = 2013";
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        ElasticsearchFilterGenerator generator = new ElasticsearchFilterGenerator();
        parser.getCQLQuery().accept(generator);
        String json = generator.getResult().string();
        logger.info("{} --> {}", cql, json);
        assertEquals(json,
                "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"dc.type\":\"electronic\"}},{\"term\":{\"dc.date\":\"2013\"}}]}}}");
    }

    @Test
    public void testTripleFieldTermFilter() throws Exception {
        String cql = "dc.format = online and dc.type = electronic and dc.date = 2013";
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        ElasticsearchFilterGenerator generator = new ElasticsearchFilterGenerator();
        parser.getCQLQuery().accept(generator);
        String json = generator.getResult().string();
        logger.info("{} --> {}", cql, json);
        assertEquals(json,
                "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"dc.format\":\"online\"}},{\"term\":{\"dc.type\":\"electronic\"}}]}},{\"term\":{\"dc.date\":\"2013\"}}]}}}");
    }

    @Test
    public void testField() throws Exception {
        SearchRetrieveRequest cqlRequest = new SearchRetrieveRequest()
                .setQuery("dc.title = Köln");
        assertEquals(cqlRequest.getQuerySource(),
                "{\"from\":0,\"size\":10,\"query\":{\"simple_query_string\":{\"query\":\"Köln\",\"fields\":[\"dc.title\"],\"default_operator\":\"and\"}}}");
    }

    @Test
    public void testFacetAndFilter() throws Exception {
        SearchRetrieveRequest cqlRequest = new SearchRetrieveRequest()
                .from(0)
                .size(10)
                .setQuery("Köln")
                .setFilter("dc.format = online and dc.date = 2012")
                .setFacetLimit("10:dc.format");
        assertEquals(cqlRequest.getQuerySource(),
                "{\"from\":0,\"size\":10,\"query\":{\"filtered\":{\"query\":{\"simple_query_string\":{\"query\":\"Köln\",\"fields\":[\"cql.allIndexes\"],\"default_operator\":\"and\"}},\"filter\":{\"query\":{\"bool\":{\"must\":[{\"term\":{\"dc.format\":\"online\"}},{\"term\":{\"dc.date\":\"2012\"}}]}}}}},\"aggregations\":{\"dc.format\":{\"terms\":{\"field\":\"dc.format\",\"size\":10,\"order\":{\"_count\":\"desc\"}}}}}");
    }

    private void test(String path) throws IOException {
        int count = 0;
        int ok = 0;
        int errors = 0;
        LineNumberReader lr = new LineNumberReader(new InputStreamReader(getClass().getResourceAsStream(path), "UTF-8"));
        String line;
        while ((line = lr.readLine()) != null) {
            if (line.trim().length() > 0 && !line.startsWith("#")) {
                try {
                    int pos = line.indexOf('|');
                    if (pos >0) {
                        validate(line.substring(0, pos), line.substring(pos+1));
                        ok++;
                    }
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                    errors++;
                }
                count++;
            }
        }
        lr.close();
        assertEquals(errors, 0);
        assertEquals(ok, count);
    }

    private void validate(String cql, String expected) throws Exception {
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        ElasticsearchQueryGenerator generator = new ElasticsearchQueryGenerator();
        parser.getCQLQuery().accept(generator);
        String elasticsearchQuery = generator.getSourceResult();
        logger.info("{} --> {}", cql, elasticsearchQuery);
        //System.err.println(cql+"|"+elasticsearchQuery);
        assertEquals(elasticsearchQuery, expected);
    }

}
