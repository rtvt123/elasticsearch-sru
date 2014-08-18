package org.xbib.elasticsearch.rest.sru.explain;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectCursor;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.xbib.elasticsearch.common.util.ExceptionFormatter;
import org.xbib.elasticsearch.module.sru.HandlebarsService;
import org.xbib.elasticsearch.rest.sru.RestResponseListener;

import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.collect.Lists.newLinkedList;
import static org.elasticsearch.common.collect.Maps.newHashMap;

public class ExplainToXContentListener extends RestResponseListener<GetMappingsResponse> {

    private final static ESLogger logger = ESLoggerFactory.getLogger("", "sru.explain");

    private final Settings settings;

    private final RestRequest restRequest;

    private final HandlebarsService handlebarsService;

    public ExplainToXContentListener(Settings settings, RestChannel channel, RestRequest restRequest, HandlebarsService handlebarsService) {
        super(channel);
        this.settings = settings;
        this.restRequest = restRequest;
        this.handlebarsService = handlebarsService;
    }

    @Override
    public RestResponse buildResponse(GetMappingsResponse response) throws Exception {
        Map<String,Object> map = newHashMap();
        map.putAll(settings.getAsStructuredMap());
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> m =
                response.getMappings();
        for (ObjectCursor<String> k : m.keys()) {
            ImmutableOpenMap<String, MappingMetaData> mappings = m.get(k.value);
            for (ObjectCursor<String> o : mappings.keys()) {
                MappingMetaData md = mappings.get(o.value);
                List<String> l = newLinkedList();
                fields("", l, md.getSourceAsMap());
                map.put("list", l);
            }
        }
        logger.info("{}", map);

        String xml = handlebarsService
                .getTemplate(settings.get("sru.explain.template", "xml/sru/2.0/explain"))
                .apply(handlebarsService.makeContext(restRequest, map));
        return new BytesRestResponse(RestStatus.OK,
                settings.get("sru.explain.contenttype", "text/xml;charset=UTF-8"), xml);
    }

    @Override
    public void onFailure(Throwable t) {
        try {
            logger.error(t.getMessage(), t);
            Map<String,Object> map = newHashMap();
            map.put("message", t.getMessage());
            map.put("exception", ExceptionFormatter.format(t));
            String xml = handlebarsService
                    .getTemplate(settings.get("sru.explain.errortemplate", "xml/sru/2.0/diagnostics"))
                    .apply(handlebarsService.makeContext(restRequest, map));
            channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR,
                    settings.get("sru.explain.contenttype", "text/xml;charset=UTF-8"),
                    xml));
        } catch (Throwable e1) {
            logger.error("failed to send failure response", e1);
        }
    }

    private void fields(String key, List<String> list, Map<String,Object> map) {
        if (map == null) {
            return;
        }
        for (String s : map.keySet()) {
            if ("properties".equals(s)) {
                fields(key, list, (Map<String,Object>)map.get(s));
            } else if (!"field".equals(s) && !"type".equals(s)) {
                Object o = map.get(s);
                if (o instanceof Map) {
                    Map<String, Object> m = (Map<String, Object>) map.get(s);
                    String k = key.length() == 0 ? s : key + "." + s;
                    if (m.containsKey("properties")) {
                        fields(k, list, m);
                    } else {
                        list.add(k);
                    }
                }
            }
        }
    }

}
