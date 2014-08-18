package org.xbib.elasticsearch.rest.sru.searchretrieve;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.xbib.elasticsearch.common.util.ExceptionFormatter;
import org.xbib.elasticsearch.common.xcontent.XmlXContentBuilder;
import org.xbib.elasticsearch.common.xcontent.XmlXContentFactory;
import org.xbib.elasticsearch.common.xcontent.xml.XmlNamespaceContext;
import org.xbib.elasticsearch.common.xcontent.xml.XmlXParams;
import org.xbib.elasticsearch.module.sru.HandlebarsService;
import org.xbib.elasticsearch.rest.sru.RestResponseListener;

import javax.xml.namespace.QName;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.collect.Lists.newLinkedList;
import static org.elasticsearch.common.collect.Maps.newHashMap;

public class SearchRetrieveToXContentListener extends RestResponseListener<SearchResponse>
        implements SearchRetrieveConstants {

    private final static XmlXParams DEFAULT_XML_PARAMS =
            new XmlXParams(new QName("http://xbib.org/ns/sru/elasticsearch/source/1.0/", "source", "es"),
                    XmlNamespaceContext.newInstance());

    private final static ESLogger logger = ESLoggerFactory.getLogger("", "sru.searchretrieve");

    private final Settings settings;

    private final RestRequest restRequest;

    private final HandlebarsService handlebarsService;

    private final XmlXParams xmlParams;

    private String query = "";

    private String key;

    private String[] values;

    public SearchRetrieveToXContentListener(Settings settings, RestChannel channel,
                                            RestRequest restRequest, HandlebarsService handlebarsService) {
        super(channel);
        this.settings = settings;
        this.restRequest = restRequest;
        this.handlebarsService = handlebarsService;
        this.xmlParams = findXmlParams(settings);
    }

    @Override
    public RestResponse buildResponse(SearchResponse response) throws Exception {
        Map<String,Object> map = newHashMap();
        map.put("total", response.getHits().getTotalHits());
        if (response.getHits().getHits() != null) {
            List<Map<String, Object>> records = newLinkedList();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> m = newHashMap();
                m.put("index", hit.getIndex());
                m.put("type", hit.getType());
                m.put("id", hit.getId());
                m.put("score", hit.getScore());
                m.put("recordschema", restRequest.param("recordSchema",
                        settings.get("sru.searchretrieve.recordSchema", "http://xbib.org/ns/sru/elasticsearch/source/1.0/")));
                m.put("recordpacking", settings.get("sru.searchretrieve.recordPacking", "xml"));
                Map<String, Object> filteredSourceMap = filter(hit.getSource());
                m.put("source", filteredSourceMap);
                try {
                    XmlXContentBuilder builder = XmlXContentFactory.xmlBuilder(xmlParams);
                    builder.map(filteredSourceMap);
                    m.put("sourcexml", builder.string());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                records.add(m);
            }
            if (!records.isEmpty()) {
                map.put("records", records);
            }
        }
        if (response.getAggregations() != null) {
            List<Map<String, Object>> facets = newLinkedList();
            for (Aggregation aggregation : response.getAggregations()) {
                if (aggregation instanceof StringTerms) {
                    StringTerms stringTerms = (StringTerms) aggregation;
                    Map<String, Object> m = newHashMap();
                    m.put("name", stringTerms.getName());
                    List<Map<String,Object>> buckets = newLinkedList();
                    for (Terms.Bucket bucket : stringTerms.getBuckets()) {
                        Map<String, Object> b = newHashMap();
                        b.put("term", bucket.getKey());
                        b.put("count", bucket.getDocCount());
                        String newFilter = stringTerms.getName() + "=\"" + URLEncoder.encode(bucket.getKey(), "UTF-8") + "\"";
                        String filter = restRequest.params().containsKey("filter") ?
                                restRequest.param("filter") + " and " + newFilter : newFilter;
                        b.put("requestUrl", new URL(new URL(settings.get("sru.searchretrieve.baseUrl", "http://localhost:9200")),
                                restRequest.path()
                                        + "?operation=searchRetrieve&version=" + settings.get("sru.version", "2.0")
                                        + "&query=" + query
                                        + "&filter=" + filter));
                        buckets.add(b);
                    }
                    m.put("buckets", buckets);
                    facets.add(m);
                } else if (aggregation instanceof LongTerms) {
                    LongTerms longTerms = (LongTerms) aggregation;
                    Map<String, Object> m = newHashMap();
                    m.put("name", longTerms.getName());
                    List<Map<String,Object>> buckets = newLinkedList();
                    for (Terms.Bucket bucket : longTerms.getBuckets()) {
                        Map<String, Object> b = newHashMap();
                        b.put("term", bucket.getKey());
                        b.put("count", bucket.getDocCount());
                        String newFilter = longTerms.getName() + "=\"" + URLEncoder.encode(bucket.getKey(), "UTF-8") + "\"";
                        String filter = restRequest.params().containsKey("filter") ?
                                restRequest.param("filter") + " and " + newFilter : newFilter;
                        b.put("requestUrl", new URL(new URL(settings.get("sru.searchretrieve.baseUrl", "http://localhost:9200")),
                                restRequest.path()
                                        + "?operation=searchRetrieve&version=" + settings.get("sru.version", "2.0")
                                        + "&query=" + query
                                        + "&filter=" + filter));
                        buckets.add(b);
                    }
                    m.put("buckets", buckets);
                    facets.add(m);
                }
            }
            if (!facets.isEmpty()) {
                map.put("facets", facets);
            }
        }
        String xml = handlebarsService
                .getTemplate(settings.get("sru.searchretrieve.template", "xml/sru/2.0/response"))
                .apply(handlebarsService.makeContext(restRequest, map));
        logger.info("params={} total={} took={}ms", restRequest.params(),
                response.getHits().getTotalHits(), response.getTookInMillis());
        return new BytesRestResponse(RestStatus.OK,
                restRequest.param(HTTP_ACCEPT_PARAMETER,
                        settings.get("sru.searchretrieve.contentType", "application/sru+xml;charset=UTF-8")),
                xml);
    }

    @Override
    public void onFailure(Throwable t) {
        try {
            logger.error(t.getMessage(), t);
            Map<String,Object> map = newHashMap();
            map.put("message", t.getMessage());
            map.put("exception", ExceptionFormatter.format(t));
            String xml = handlebarsService
                    .getTemplate(settings.get("sru.searchretrieve.errorTemplate", "xml/sru/2.0/diagnostics"))
                    .apply(handlebarsService.makeContext(restRequest, map));
            channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR,
                    settings.get("sru.searchretrieve.contentType", "text/xml;charset=UTF-8"),
                    xml));
        } catch (Throwable e1) {
            logger.error("failed to send failure response", e1);
        }
    }

    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Filter out elements from result doc
     * @param key the key to be filtered out
     * @param values the values to check (a list of ISIL)
     */
    public void setFilter(String key, String[] values) {
        this.key = key;
        this.values = values;
    }

    /**
     * Filter out unwanted elements from Elasticsearch document.
     * Remove all "xbib" keys, and remove ISILs if they are set in "identifier" and are not the index
     *
     * @param map Elasticsearch document
     * @return the filtered Elasticsearch document
     */
    protected Map<String,Object> filter(Map<String,Object> map) {
        Map<String,Object> newMap = newHashMap();
        for (Map.Entry<String,Object> me : map.entrySet()) {
            if ("xbib".equals(me.getKey())) {
                continue;
            }
            if (key == null) {
                newMap.put(me.getKey(), me.getValue());
                continue;
            }
            if (me.getValue() instanceof Map) {
                Map m = (Map)me.getValue();
                boolean b = false;
                if (m.containsKey(key)) {
                    for (String v : values) {
                        String s = m.get(key).toString();
                        // main ISIL or not?
                        if (s.indexOf("-") < s.lastIndexOf("-")) {
                            if (s.startsWith(v)) {
                                b = true;
                            }
                        } else if (v.equals(s)) {
                            b = true;
                        }
                    }
                } else {
                    b = true;
                }
                if (b) {
                    newMap.put(me.getKey(), m);
                }
            } else if (me.getValue() instanceof List) {
                List newList = newLinkedList();
                List list = (List)me.getValue();
                for (Object o : list) {
                    if (o instanceof Map) {
                        Map<String, Object> m = (Map<String, Object>)o;
                        boolean b = false;
                        if (m.containsKey(key)) {
                            for (String v : values) {
                                String s = m.get(key).toString();
                                // main ISIL or not?
                                if (s.indexOf("-") < s.lastIndexOf("-")) {
                                    if (s.startsWith(v)) {
                                        b = true;
                                    }
                                } else if (v.equals(s)) {
                                    b = true;
                                }
                            }
                        } else {
                            b = true;
                        }
                        if (b) {
                            newList.add(m);
                        }
                    } else {
                        newList.add(o);
                    }
                }
                if (!newList.isEmpty()) {
                    newMap.put(me.getKey(), newList);
                }
            } else {
                newMap.put(me.getKey(), me.getValue());
            }
        }
        return newMap;
    }

    protected XmlXParams findXmlParams(Settings settings) {
        XmlXParams xmlParams = DEFAULT_XML_PARAMS;
        String uri = settings.get("sru.searchretrieve.namespaceURI");
        String prefix = settings.get("sru.searchretrieve.namespacePrefix");
        String elementName = settings.get("sru.searchretrieve.elementName");
        if (uri != null && prefix != null && elementName != null) {
            xmlParams = new XmlXParams(new QName(uri, elementName, prefix), XmlNamespaceContext.newInstance());
        }
        return xmlParams;
    }
}
