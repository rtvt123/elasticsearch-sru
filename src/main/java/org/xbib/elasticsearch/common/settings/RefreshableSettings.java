package org.xbib.elasticsearch.common.settings;

import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.loader.JsonSettingsLoader;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class RefreshableSettings {

    private final static ESLogger logger = ESLoggerFactory.getLogger("", "refresh.settings");

    private final static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private Settings settings;

    private final String index;

    private final String type;

    private final String id;

    public RefreshableSettings(Settings settings, Client client) {
        set(settings);
        this.index = settings.get("refresh.settings.index");
        this.type = settings.get("refresh.settings.type");
        this.id = settings.get("refresh.settings.id");
        if (index != null && type != null && id != null) {
            executor.scheduleAtFixedRate(new Thread() {
                public void run() {
                    logger.debug("refreshing");
                    boolean save = true;
                    try {
                        GetResponse getResponse = new GetRequestBuilder(client)
                                .setIndex(index)
                                .setType(type)
                                .setId(id)
                                .execute()
                                .actionGet();
                        save = !getResponse.isExists();
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                    if (save) {
                        try {
                            XContentBuilder builder = jsonBuilder()
                                    .map(settings.getAsStructuredMap());
                            new IndexRequestBuilder(client)
                                    .setIndex(index)
                                    .setType(type)
                                    .setId(id)
                                    .setSource(builder)
                                    .setRefresh(true)
                                    .execute()
                                    .actionGet();
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                        }
                    }
                    try {
                        GetResponse getResponse = new GetRequestBuilder(client)
                                .setIndex(index)
                                .setType(type)
                                .setId(id)
                                .execute()
                                .actionGet();
                        set(settingsBuilder().put(new JsonSettingsLoader()
                                .load(jsonBuilder().map(getResponse.getSourceAsMap()).string()))
                                .build());
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }, 1, 1, TimeUnit.MINUTES);
        }
    }

    public void set(Settings settings) {
        this.settings = settings;
    }

    public Settings get() {
        return settings;
    }

    public void close() {
        executor.shutdownNow();
    }

}
