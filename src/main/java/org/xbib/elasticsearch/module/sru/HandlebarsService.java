package org.xbib.elasticsearch.module.sru;

import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestRequest;
import org.xbib.template.handlebars.EscapingStrategy;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.HandlebarsContext;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.context.FieldValueResolver;
import org.xbib.template.handlebars.context.MapValueResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HandlebarsService extends AbstractComponent {

    private final Map<String,Template> templates;

    @Inject
    public HandlebarsService(Settings settings) {
        super(settings);
        Handlebars handlebars = new Handlebars()
                .with(EscapingStrategy.NONE);
        Map<String,Template> templates;
        try {
            templates = precompile(settings, handlebars);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            templates = null;
        }
        this.templates = templates;
    }

    public Template getTemplate(String name) {
        if (!templates.containsKey(name)) {
            throw new IllegalArgumentException("no template " + name + " precompiled");
        } else {
            return templates.get(name);
        }
    }

    public HandlebarsContext makeContext(RestRequest request, Map<String,Object> map) {
        return HandlebarsContext.newBuilder(makeParams(request, map))
                .resolver(MapValueResolver.INSTANCE, FieldValueResolver.INSTANCE)
                .build();
    }

    private Map<String,Object> makeParams(RestRequest request, Map<String,Object> map) {
        Map<String, Object> p = new HashMap<String, Object>();
        p.putAll(settings.getAsMap());
        for (Map.Entry<String,String> me : request.params().entrySet()) {
            p.put(me.getKey(), me.getValue());
        }
        p.put("map", map);
        return p;
    }

    private Map<String,Template> precompile(Settings settings, Handlebars handlebars) throws IOException {
        String[] names = settings.getAsArray("sru.handlebars.templates");
        if (names == null || names.length == 0) {
            names = new String[]{
                    "xml/sru/1.1/response",
                    "xml/sru/1.1/diagnostics",
                    "xml/sru/1.1/explain",
                    "xml/sru/1.2/response",
                    "xml/sru/1.2/diagnostics",
                    "xml/sru/1.2/explain",
                    "xml/sru/2.0/response",
                    "xml/sru/2.0/diagnostics",
                    "xml/sru/2.0/explain"};
        }
        Map<String,Template> templateMap = new HashMap<String,Template>();
        for (String name : names) {
            if (name != null) {
                Template template = handlebars.compile(name);
                if (template != null) {
                    logger.info("template {} precompiled", name);
                    templateMap.put(name, template);
                } else {
                    logger.warn("template {} not found", name);
                }
            }
        }
        return templateMap;
    }
}
