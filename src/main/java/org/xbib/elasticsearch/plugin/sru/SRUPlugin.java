package org.xbib.elasticsearch.plugin.sru;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.xbib.elasticsearch.rest.sru.SRURestSearchAction;

/**
 * SRU plugin
 */
public class SRUPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "sru-"
                + Build.getInstance().getVersion() + "-"
                + Build.getInstance().getShortHash();
    }

    @Override
    public String description() {
        return "SRU plugin";
    }

    public void onModule(RestModule module) {
        module.addRestAction(SRURestSearchAction.class);
    }


}
