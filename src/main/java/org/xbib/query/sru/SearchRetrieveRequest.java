package org.xbib.query.sru;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.query.cql.CQLParser;
import org.xbib.query.cql.elasticsearch.ElasticsearchQueryGenerator;

import java.io.IOException;

/**
 * Contextual Query Language request for Elasticsearch
 */
public class SearchRetrieveRequest {

    private final static ESLogger logger = ESLoggerFactory.getLogger(SearchRetrieveRequest.class.getName());

    private final SearchRequestBuilder searchRequestBuilder;

    private ElasticsearchQueryGenerator generator = new ElasticsearchQueryGenerator();

    private String elasticseachQuery;

    private XContentBuilder sort;

    private String version;

    private String recordPacking;

    private String recordSchema;

    private String cqlQuery;

    private String filter;

    private Integer startRecord;

    private Integer maxRecords;

    private Integer ttl;

    private String sortKeys;

    private String facetLimit;

    private String facetStart;

    private String facetSort;

    private String facetCount;

    private String data;

    private String encoding;

    public SearchRetrieveRequest() {
        this.searchRequestBuilder = null;
    }

    public SearchRetrieveRequest(SearchRequestBuilder searchRequestBuilder) {
        this.searchRequestBuilder = searchRequestBuilder;
    }
    /**
     * The version of the request
     * @param version the version string
     * @return this request
     */
    public SearchRetrieveRequest setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getVersion() {
        return version;
    }


    public SearchRetrieveRequest setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public String getFilter() {
        return filter;
    }

    /**
     * The position within the sequence of matched records (see result set model) of the first record to be
     * returned. The first position in the sequence is 1. The value supplied must be greater than 0.
     * Default value if not supplied is 1.
     *
     * @param startRecord the start record
     * @return this request
     */
    public SearchRetrieveRequest setStartRecord(Integer startRecord) {
        this.startRecord = startRecord;
        if (startRecord != null) {
            generator.setFrom(startRecord - 1);
        }
        return this;
    }

    public int getStartRecord() {
        return startRecord;
    }

    /**
     * The number of records requested to be returned. The value must be 0 or greater. Default value
     * if not supplied is determined by the server (see explain). The server may return less than this
     * number of records, for example if there are fewer matching records than requested.
     * @param maxRecords the maximum record count
     * @return this request
     */
    public SearchRetrieveRequest setMaximumRecords(Integer maxRecords) {
        this.maxRecords = maxRecords;
        if (maxRecords != null) {
            generator.setSize(maxRecords);
        }
        return this;
    }

    public int getMaximumRecords() {
        return maxRecords;
    }

    /**
     * In order that records which are not well formed do not break the entire message, it is possible to
     * request that they be transferred as a single string with the < > and & escaped to their entity forms.
     * Moreover some toolkits may not be able to distinguish record XML from the XML which forms the response.
     * However, some clients may prefer that the records be transferred as XML in order to manipulate them
     * directly with a stylesheet which renders the records and potentially also the user interface.
     * This distinction is made via the recordPacking parameter in the request. If the value of the
     * parameter is 'string', then the server should escape the record before transfering it.
     * If the value is 'xml', then it should embed the XML directly into the response.
     * Either way, the data is transfered within the 'recordData' field.
     * If the server cannot comply with this packing request, then it must return a diagnostic.
     * @param recordPacking the record packing
     * @return this request
     */
    public SearchRetrieveRequest setRecordPacking(String recordPacking) {
        this.recordPacking = recordPacking;
        return this;
    }

    public String getRecordPacking() {
        return recordPacking;
    }

    /**
     * The schema requested for the records to be returned.
     * If the recordXPath parameter (next) is included, it is the abstract schema for purposes of
     * evaluation by the XPath expression.
     * If the recordXPath parameter is not included, it is the schema that response records should assume.
     * The value is the URI identifer for the schema or the short name for it published by the server.
     * The default value if not supplied is determined by the server (see explain).
     * @param recordSchema the record schema
     * @return this request
     */
    public SearchRetrieveRequest setRecordSchema(String recordSchema) {
        this.recordSchema = recordSchema;
        return this;
    }

    public String getRecordSchema() {
        return recordSchema;
    }

    /**
     * The number of seconds for which the client requests that the result set created should be maintained.
     * The server may choose not to fulfil this request, and may respond with a different number of seconds
     * (parameter resultSetIdleTime in response). If resultSetTTL is not supplied then the server will
     * determine the value.
     * @param ttl the time to live in seconds
     * @return this request
     */
    public SearchRetrieveRequest setResultSetTTL(Integer ttl) {
        this.ttl = ttl;
        if (searchRequestBuilder != null) {
            searchRequestBuilder.setTimeout(TimeValue.timeValueSeconds(ttl));
        }
        return this;
    }

    public Integer getResultSetTTL() {
        return ttl;
    }

    /**
     * Sorting
     * A request may include a sort specification, indicating the desired ordering of the results.
     * This is a request for the server to apply a sorting algorithm to the result set before
     * returning any records. It may be supplied with a new search or applied to an existing result set.
     * The sort parameter is included in the searchRetrieve operation, rather than defined as a separate
     * operation, for two reasons: (1) if the server knows the desired sort order in advance,
     * query processing can be optimized; (2) a server may be able to sort a result set at creation,
     * but not maintain it across multiple requests.
     * In order to specify result set(s) to sort, the query should include:
     * cql.resultSetId = "resultSetId"
     * where 'resultSetId' is the identifier supplied by the server for the result set.
     * If multiple result set identifiers are supplied, linked by boolean OR, AND or NOT, then
     * the request will combine and sort all of the given sets together. This is documented in the CQL context set.
     * The sort parameter includes one or more keys, each of which includes the following information:
     * path xsd:string Mandatory
     * An XPath expression describing a tagpath to be used in the sort. See additional description.
     * schema
     * xsd:string
     * Optional
     * The URI identifier for a supported schema. This schema is the one to which the XPath expression applies.
     * If it is not supplied then the default value from Explain will be used. See additional description.
     * ascending
     * xsd:boolean
     * Optional
     * Should the results be sorted ascending (true, and the default if not supplied) or descending (false).
     * caseSensitive
     * xsd:boolean
     * Optional
     * Should case be considered as important during the sort. The default value is false if not supplied.
     * missingValue
     * xsd:string
     * Optional
     * One of 'abort', 'highValue', 'lowValue', 'omit' or a supplied value.
     * The semantics of each are described below and the default is 'highValue'.
     * XPath and Schema
     * XPath is a W3C specification which allows the description of an element path. So to sort by title,
     * one might specify the xpath of "/record/title" within the Dublin Core schema. The records need not
     * be stored in this particular schema to be able to sort by it. The records do not even necessarily
     * need to be able to be returned in the schema.
     * SRU has the concept of utility schemas which are designed not to return records in, but into which
     * records can be transformed in order to sort them in a particular way. For example, if the record
     * has a geographical location in it, then it may be desirable to sort the locations in the records
     * from north to south and east to west. This would obviously require transformation into a schema
     * that allows sorting by a convenient coordinate system, rather than lexically on the place name,
     * and this schema may not be available for retrieving the records.
     * Missing Value Action
     * This parameter of a sort key instructs the server what to do when the supplied XPath is not present
     * within the record. For example if the server is instructed to sort by author, and a record has no author,
     * it will behave in accordance with this value.
     * Its value may be:
     * abort: The server should immediately cease trying to sort and return a diagnostic that the sort could not be performed.
     * omit: The server should remove this record from the results.
     * lowValue: The server should sort this as if it were the lowest possible value.
     * highValue: The server should sort this as if it were the highest possible value.
     * "constant": The server should sort the record as if this value ("constant") were supplied.
     *
     * sortKeys
     * The textual representation of a sort key is achieved by the following rules.
     * The path must be included as the first parameter.
     * Subsequent parameters are separated by the use of a comma (,) character in the order given above.
     * The path and schema must be quoted if they contain quotes, commas or spaces.
     * Internal quotes must be escaped with a backslash.
     * Parameters beyond the first may be supplied with no value, in which case the server will use the default.
     * The last parameter supplied must be present. (In other words, the key may not end in a comma.)
     * Boolean parameters are expressed as 1 (true) or 0 (false).
     * Multiple keys are separated by whitespace.
     * An example of the sortKeys parameter in an SRU URL might be:
     *
     * @param sortKeys the sort keys
     * @return this request
     */
    public SearchRetrieveRequest setSortKeys(String sortKeys) {
        this.sortKeys = sortKeys;
        return this;
    }

    public String getSortKeys() {
        return sortKeys;
    }

    public SearchRetrieveRequest setFacetLimit(String limitSpec) {
        this.facetLimit = limitSpec;
        return this;
    }

    public String getFacetLimit() {
        return facetLimit;
    }

    public SearchRetrieveRequest setFacetStart(String startSpec) {
        this.facetStart = startSpec;
        return this;
    }

    public String getFacetStart() {
        return facetStart;
    }

    public SearchRetrieveRequest setFacetCount(String countSpec) {
        this.facetCount = countSpec;
        return this;
    }

    public String getFacetCount() {
        return facetCount;
    }

    public SearchRetrieveRequest setFacetSort(String sortSpec) {
        this.facetSort = sortSpec;
        return this;
    }

    public String getFacetSort() {
        return facetSort;
    }

    public SearchRetrieveRequest setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String getEncoding() {
        return encoding;
    }

    public SearchRetrieveRequest setExtraRequestData(String data) {
        this.data = data;
        return this;
    }

    public String getExtraRequestData() {
        return data;
    }


    public SearchRetrieveRequest index(String index) {
        this.index(new String[]{index});
        return this;
    }

    public SearchRetrieveRequest index(String... index) {
        if (searchRequestBuilder != null) {
            searchRequestBuilder.setIndices(index);
        }
        return this;
    }

    public SearchRetrieveRequest type(String type) {
        this.type(new String[]{type});
        return this;
    }

    public SearchRetrieveRequest type(String... type) {
        if (searchRequestBuilder != null) {
            searchRequestBuilder.setTypes(type);
        }
        return this;
    }

    public SearchRetrieveRequest aggregations(byte[] aggregations) {
        if (searchRequestBuilder != null) {
            searchRequestBuilder.setAggregations(aggregations);
        }
        return this;
    }

    public SearchRetrieveRequest from(int from) {
        generator.setFrom(from);
        return this;
    }

    public SearchRetrieveRequest size(int size) {
        generator.setSize(size);
        return this;
    }

    public SearchRetrieveRequest timeout(TimeValue timeout) {
        if (searchRequestBuilder != null) {
            searchRequestBuilder.setTimeout(timeout);
        }
        return this;
    }

    public SearchRetrieveRequest elasticsearchQuery(String query) {
        this.elasticseachQuery = query == null || query.trim().length() == 0 ? "{\"query\":{\"match_all\":{}}}" : query;
        return this;
    }

    public SearchRetrieveRequest sort(XContentBuilder sort) {
        if (sort == null) {
            return this;
        }
        this.sort = sort;
        return this;
    }

    public SearchRetrieveRequest setQuery(String cql) {
        if (cql == null || cql.trim().length() == 0) {
            from(0).size(10).elasticsearchQuery(null);
            return this;
        }
        this.cqlQuery = cql;
        return this;
    }

    public String getQuery() {
        return cqlQuery;
    }

    public void execute(ActionListener<SearchResponse> listener) throws IOException {
        if (elasticseachQuery == null) {
            this.elasticseachQuery = createElasticsearchQuery();
        }
        if (elasticseachQuery == null) {
            return;
        }
        if (searchRequestBuilder == null) {
            return;
        }
        logger.debug("executing CQL query {} --> Elasticsearch query {}", cqlQuery, elasticseachQuery);
        searchRequestBuilder.setExtraSource(elasticseachQuery).execute(listener);
    }

    public String getQuerySource() {
        if (elasticseachQuery == null) {
            this.elasticseachQuery = createElasticsearchQuery();
        }
        return elasticseachQuery;
    }

    private String createElasticsearchQuery()  {
        if (sort != null) {
            generator.setSort(sort);
        }
        if (filter != null) {
            generator.filter(filter);
        }
        if (facetLimit != null) {
            generator.facet(facetLimit, facetSort);
        }
        if (cqlQuery != null) {
            CQLParser parser = new CQLParser(cqlQuery);
            parser.parse();
            parser.getCQLQuery().accept(generator);
            return generator.getSourceResult();
        }
        return null;
    }



}