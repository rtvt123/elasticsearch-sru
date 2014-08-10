package org.xbib.elasticsearch.rest.sru;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestStatusToXContentListener;
import org.xbib.query.sru.SearchRetrieveRequest;
import org.xbib.query.sru.SearchRetrieveConstants;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class SRURestSearchAction extends BaseRestHandler {

    @Inject
    public SRURestSearchAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(GET, "/_sru", this);
        controller.registerHandler(POST, "/_sru", this);
        controller.registerHandler(GET, "/{index}/_sru", this);
        controller.registerHandler(POST, "/{index}/_sru", this);
        controller.registerHandler(GET, "/{index}/{type}/_sru", this);
        controller.registerHandler(POST, "/{index}/{type}/_sru", this);
        controller.registerHandler(GET, "/_sru/template", this);
        controller.registerHandler(POST, "/_sru/template", this);
        controller.registerHandler(GET, "/{index}/_sru/template", this);
        controller.registerHandler(POST, "/{index}/_sru/template", this);
        controller.registerHandler(GET, "/{index}/{type}/_sru/template", this);
        controller.registerHandler(POST, "/{index}/{type}/_sru/template", this);
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
            SearchRetrieveRequest sruRequest = new SearchRetrieveRequest(new SearchRequestBuilder(client))
                    .index(Strings.splitStringByCommaToArray(request.param("index")))
                    .type(Strings.splitStringByCommaToArray(request.param("type")))
                    .setQuery(cql)
                    .setStartRecord(request.paramAsInt(SearchRetrieveConstants.START_RECORD_PARAMETER, 1))
                    .setMaximumRecords(request.paramAsInt(SearchRetrieveConstants.MAXIMUM_RECORDS_PARAMETER, 10))
                    .setFilter(request.param(SearchRetrieveConstants.FILTER_PARAMETER))
                    .setFacetLimit(request.param(SearchRetrieveConstants.FACET_LIMIT_PARAMETER))
                    .setFacetCount(request.param(SearchRetrieveConstants.FACET_COUNT_PARAMETER))
                    .setResultSetTTL(request.paramAsInt(SearchRetrieveConstants.RESULT_SET_TTL_PARAMETER, 0));
            sruRequest.execute(new RestStatusToXContentListener<SearchResponse>(channel));
        } catch (IOException e) {
            throw new ElasticsearchException(e.getMessage(), e);
        }
    }

}
