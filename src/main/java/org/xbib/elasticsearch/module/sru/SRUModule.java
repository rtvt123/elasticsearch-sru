package org.xbib.elasticsearch.module.sru;

import org.elasticsearch.common.inject.AbstractModule;

public class SRUModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(HandlebarsService.class).asEagerSingleton();
    }
}
