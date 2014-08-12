package org.xbib.elasticsearch.plugin.sru;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.xbib.elasticsearch.module.sru.SRUModule;
import org.xbib.elasticsearch.rest.sru.SRURestSearchAction;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.newArrayList;

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


    @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        modules.add(SRUModule.class);
        return modules;
    }
}
