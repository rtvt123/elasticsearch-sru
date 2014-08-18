package org.xbib.elasticsearch.rest.sru;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.search.Scroll;
import org.xbib.elasticsearch.common.settings.RefreshableSettings;
import org.xbib.elasticsearch.module.sru.HandlebarsService;
import org.xbib.elasticsearch.rest.sru.explain.ExplainRequest;
import org.xbib.elasticsearch.rest.sru.explain.ExplainToXContentListener;
import org.xbib.elasticsearch.rest.sru.searchretrieve.SearchRetrieveRequest;
import org.xbib.elasticsearch.rest.sru.searchretrieve.SearchRetrieveConstants;
import org.xbib.elasticsearch.rest.sru.searchretrieve.SearchRetrieveToXContentListener;

import java.util.Arrays;

import static org.elasticsearch.common.unit.TimeValue.parseTimeValue;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class SRURestAction extends BaseRestHandler {

    private final static ESLogger logger = ESLoggerFactory.getLogger("", "sru.rest");

    private final HandlebarsService handlebars;

    private final RefreshableSettings refreshableSettings;

    @Inject
    public SRURestAction(Settings settings, Client client, RestController controller, HandlebarsService handlebars) {
        super(settings, client);
        this.handlebars = handlebars;
        this.refreshableSettings = new RefreshableSettings(settings, client);

        controller.registerHandler(GET, "/_sru", this);
        controller.registerHandler(POST, "/_sru", this);
        controller.registerHandler(GET, "/_sru/{index}/", this);
        controller.registerHandler(POST, "/_sru/{index}/", this);
        controller.registerHandler(GET, "/_sru/{index}/{type}/", this);
        controller.registerHandler(POST, "/_sru/{index}/{type}/", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        if (!request.params().containsKey("index")
                && !request.params().containsKey(SearchRetrieveConstants.OPERATION_PARAMETER)
                && !request.params().containsKey(SearchRetrieveConstants.QUERY_PARAMETER)) {
            explain(request, channel, client);
        }
        String operation = request.param(SearchRetrieveConstants.OPERATION_PARAMETER);
        if (SearchRetrieveConstants.EXPLAIN_COMMAND.equals(operation)) {
            explain(request, channel, client);
        } else {
            searchRetrieve(request, channel, client);
        }
    }

    private void searchRetrieve(final RestRequest request, final RestChannel channel, final Client client) {
        SearchRetrieveToXContentListener listener =
                new SearchRetrieveToXContentListener(refreshableSettings.get(), channel, request, handlebars);
        try {
            String cql = request.param("q");
            if (cql == null) {
                cql = request.param("cql");
            }
            if (cql == null) {
                cql = request.param(SearchRetrieveConstants.QUERY_PARAMETER);
            }
            if (cql == null) {
                cql = request.content().toUtf8();
            }
            if (request.params().containsKey("recordSchema")) {
                handleRecordSchemaRequest(request, client, listener, cql);
                return;
            }
            String[] indices = Strings.splitStringByCommaToArray(request.param("index"));
            String[] types = Strings.splitStringByCommaToArray(request.param("type"));
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            searchRequest.types(types);
            searchRequest.searchType(request.param("search_type"));
            String scroll = request.param("scroll");
            if (scroll != null) {
                searchRequest.scroll(new Scroll(parseTimeValue(scroll, null)));
            }
            searchRequest.routing(request.param("routing"));
            searchRequest.preference(request.param("preference"));
            searchRequest.indicesOptions(IndicesOptions.fromRequest(request, IndicesOptions.strictExpandOpenAndForbidClosed()));
            SearchRetrieveRequest sruRequest = new SearchRetrieveRequest(client, searchRequest)
                    .setVersion(request.param(SearchRetrieveConstants.VERSION_PARAMETER, settings.get("sru.version", "2.0")))
                    .setQuery(cql)
                    .setStartRecord(request.paramAsInt(SearchRetrieveConstants.START_RECORD_PARAMETER, 1))
                    .setMaximumRecords(request.paramAsInt(SearchRetrieveConstants.MAXIMUM_RECORDS_PARAMETER, 10))
                    .setFilter(request.param(SearchRetrieveConstants.FILTER_PARAMETER))
                    .setFacetLimit(request.param(SearchRetrieveConstants.FACET_LIMIT_PARAMETER))
                    .setFacetCount(request.param(SearchRetrieveConstants.FACET_COUNT_PARAMETER))
                    .setFacetStart(request.param(SearchRetrieveConstants.FACET_START_PARAMETER)) // not supported
                    .setFacetSort(request.param(SearchRetrieveConstants.FACET_SORT_PARAMETER)) // not supported
                    .setResultSetTTL(request.paramAsInt(SearchRetrieveConstants.RESULT_SET_TTL_PARAMETER, 0));
            logger.info("searchRetrieve: cql query {} --> elasticsearch query {} index={} types={}",
                    sruRequest.getQuery(), sruRequest.getElasticsearchQuery(), Arrays.asList(indices), Arrays.asList(types));
            listener.setQuery(cql);
            sruRequest.execute(listener);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            listener.onFailure(e);
        }
    }

    private void handleRecordSchemaRequest(final RestRequest request,
                                          Client client, SearchRetrieveToXContentListener listener, String cql) throws Exception {
        String[] indices = Strings.splitStringByCommaToArray(request.param("index") + "," + request.param("recordSchema"));
        String [] types = Strings.splitStringByCommaToArray(request.param("type"));
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indices);
        searchRequest.types(types);
        searchRequest.searchType(request.param("search_type"));
        String scroll = request.param("scroll");
        if (scroll != null) {
            searchRequest.scroll(new Scroll(parseTimeValue(scroll, null)));
        }
        searchRequest.routing(request.param("routing"));
        searchRequest.preference(request.param("preference"));
        searchRequest.indicesOptions(IndicesOptions.fromRequest(request, IndicesOptions.strictExpandOpenAndForbidClosed()));
        SearchRetrieveRequest searchRetrieveRequest = new SearchRetrieveRequest(client, searchRequest)
                .setVersion(request.param(SearchRetrieveConstants.VERSION_PARAMETER, settings.get("sru.version", "2.0")))
                .setQuery(cql)
                .setStartRecord(request.paramAsInt(SearchRetrieveConstants.START_RECORD_PARAMETER, 1))
                .setMaximumRecords(request.paramAsInt(SearchRetrieveConstants.MAXIMUM_RECORDS_PARAMETER, 10));
        logger.info("recordSchema request: cql query {} --> elasticsearch query {} index={} types={}",
                searchRetrieveRequest.getQuery(), searchRetrieveRequest.getElasticsearchQuery(), Arrays.asList(indices), Arrays.asList(types));
        listener.setFilter("identifier", indices);
        searchRetrieveRequest.execute(listener);
    }

    private void explain(final RestRequest request, final RestChannel channel, final Client client) {
        String[] indices = Strings.splitStringByCommaToArray(request.param("index"));
        String [] types = Strings.splitStringByCommaToArray(request.param("type"));
        GetMappingsRequest getMappingsRequest = new GetMappingsRequest();
        getMappingsRequest.indices(indices).types(types);
        getMappingsRequest.indicesOptions(IndicesOptions.fromRequest(request, getMappingsRequest.indicesOptions()));
        getMappingsRequest.local(request.paramAsBoolean("local", getMappingsRequest.local()));
        ExplainToXContentListener listener =
                new ExplainToXContentListener(refreshableSettings.get(), channel, request, handlebars);
        try {
            ExplainRequest explainRequest = new ExplainRequest(client, getMappingsRequest);
            logger.info("explain request");
            explainRequest.execute(listener);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            listener.onFailure(e);
        }
    }

}
