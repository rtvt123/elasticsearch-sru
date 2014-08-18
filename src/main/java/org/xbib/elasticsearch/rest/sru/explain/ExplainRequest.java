package org.xbib.elasticsearch.rest.sru.explain;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.client.Client;

import java.io.IOException;

public class ExplainRequest {

    private Client client;

    private final GetMappingsRequest getMappingsRequest;

    public ExplainRequest() {
        this.getMappingsRequest = null;
    }

    public ExplainRequest(Client client, GetMappingsRequest getMappingsRequest) {
        this.client = client;
        this.getMappingsRequest = getMappingsRequest;
    }

    public void execute(ExplainToXContentListener listener) throws IOException {
        client.admin().indices().getMappings(getMappingsRequest, listener);
    }

}
