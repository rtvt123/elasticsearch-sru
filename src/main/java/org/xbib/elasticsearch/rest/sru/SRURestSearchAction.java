package org.xbib.elasticsearch.rest.sru;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestStatusToXContentListener;
import org.elasticsearch.search.Scroll;
import org.xbib.elasticsearch.module.sru.HandlebarsService;
import org.xbib.query.sru.SearchRetrieveRequest;
import org.xbib.query.sru.SearchRetrieveConstants;

import java.io.IOException;

import static org.elasticsearch.common.unit.TimeValue.parseTimeValue;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class SRURestSearchAction extends BaseRestHandler {

    private final HandlebarsService handlebars;

    @Inject
    public SRURestSearchAction(Settings settings, Client client, RestController controller, HandlebarsService handlebars) {
        super(settings, client);
        this.handlebars = handlebars;
        controller.registerHandler(GET, "/_sru", this);
        controller.registerHandler(POST, "/_sru", this);
        controller.registerHandler(GET, "/{index}/_sru", this);
        controller.registerHandler(POST, "/{index}/_sru", this);
        controller.registerHandler(GET, "/{index}/{type}/_sru", this);
        controller.registerHandler(POST, "/{index}/{type}/_sru", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, Client client) {
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
        try {
            SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client);
            searchRequestBuilder.setSearchType(request.param("search_type"));
            String scroll = request.param("scroll");
            if (scroll != null) {
                searchRequestBuilder.setScroll(new Scroll(parseTimeValue(scroll, null)));
            }
            searchRequestBuilder.setRouting(request.param("routing"));
            searchRequestBuilder.setPreference(request.param("preference"));
            searchRequestBuilder.setIndicesOptions(IndicesOptions.fromRequest(request, IndicesOptions.strictExpandOpenAndForbidClosed()));
            SearchRetrieveRequest sruRequest = new SearchRetrieveRequest(searchRequestBuilder)
                    .index(Strings.splitStringByCommaToArray(request.param("index")))
                    .type(Strings.splitStringByCommaToArray(request.param("type")))
                    .setQuery(cql)
                    .setStartRecord(request.paramAsInt(SearchRetrieveConstants.START_RECORD_PARAMETER, 1))
                    .setMaximumRecords(request.paramAsInt(SearchRetrieveConstants.MAXIMUM_RECORDS_PARAMETER, 10))
                    .setFilter(request.param(SearchRetrieveConstants.FILTER_PARAMETER))
                    .setFacetLimit(request.param(SearchRetrieveConstants.FACET_LIMIT_PARAMETER))
                    .setFacetCount(request.param(SearchRetrieveConstants.FACET_COUNT_PARAMETER))
                    .setFacetStart(request.param(SearchRetrieveConstants.FACET_START_PARAMETER)) // not supported
                    .setFacetSort(request.param(SearchRetrieveConstants.FACET_SORT_PARAMETER)) // not supported
                    .setResultSetTTL(request.paramAsInt(SearchRetrieveConstants.RESULT_SET_TTL_PARAMETER, 0));
            sruRequest.execute(new SRUToXContentListener(channel, request, handlebars));
        } catch (IOException e) {
            throw new ElasticsearchException(e.getMessage(), e);
        }
    }

}
