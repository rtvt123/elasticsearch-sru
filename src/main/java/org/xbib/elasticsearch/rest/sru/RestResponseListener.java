package org.xbib.elasticsearch.rest.sru;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestResponse;

public abstract class RestResponseListener<Response> extends RestActionListener<Response> {

    protected RestResponseListener(RestChannel channel) {
        super(channel);
    }

    @Override
    protected final void processResponse(Response response) throws Exception {
        channel.sendResponse(buildResponse(response));
    }

    public abstract RestResponse buildResponse(Response response) throws Exception;
}
