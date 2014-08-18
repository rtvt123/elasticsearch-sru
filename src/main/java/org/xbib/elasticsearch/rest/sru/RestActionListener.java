package org.xbib.elasticsearch.rest.sru;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.rest.RestChannel;

public abstract class RestActionListener<Response> implements ActionListener<Response> {

    protected final RestChannel channel;

    protected RestActionListener(RestChannel channel) {
        this.channel = channel;
    }

    @Override
    public final void onResponse(Response response) {
        try {
            processResponse(response);
        } catch (Throwable t) {
            onFailure(t);
        }
    }

    @Override
    public abstract void onFailure(Throwable e);

    protected abstract void processResponse(Response response) throws Exception;

}
