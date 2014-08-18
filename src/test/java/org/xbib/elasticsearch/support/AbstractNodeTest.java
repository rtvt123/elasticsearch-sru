package org.xbib.elasticsearch.support;

import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.elasticsearch.common.collect.Maps.newHashMap;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public abstract class AbstractNodeTest {

    private final static AtomicInteger clusterCount = new AtomicInteger();

    protected String cluster;

    private Map<String, Node> nodes = newHashMap();

    private Map<String, Client> clients = newHashMap();

    private Map<String, InetSocketTransportAddress> addresses = newHashMap();

    private Map<String, InetSocketTransportAddress> httpAddresses = newHashMap();

    @Before
    public void startNodes() throws Exception {
        cluster = "test-sru-cluster-" + NetworkUtils.getLocalAddress().getHostName() + "-" + clusterCount.incrementAndGet();
        startNode("1");
    }

    @After
    public void closeNodes() {
        for (Client client : clients.values()) {
            client.close();
        }
        clients.clear();
        for (Node node : nodes.values()) {
            node.close();
        }
        nodes.clear();
    }

    public Node startNode(String id) {
        Node node = buildNode(id).start();
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest().transport(true);
        NodesInfoResponse response = clients.get(id).admin().cluster().nodesInfo(nodesInfoRequest).actionGet();
        NodeInfo nodeInfo = response.iterator().next();
        Object obj = nodeInfo.getTransport().getAddress().publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            addresses.put(id, (InetSocketTransportAddress) obj);
        }
        obj = nodeInfo.getHttp().getAddress().publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            httpAddresses.put(id, (InetSocketTransportAddress) obj);
        }
        return node;
    }

    public Node buildNode(String id) {
        Node node = nodeBuilder().settings(settings()).build();
        nodes.put(id, node);
        clients.put(id, node.client());
        return node;
    }

    protected URI getHttpAddressOfNode(String id) {
        InetSocketTransportAddress address = httpAddresses.get(id);
        return URI.create("http://" + address.address().getHostName() + ":" + (address.address().getPort()));
    }

    public Settings settings() {
        return settingsBuilder()
                .put("cluster.name", cluster)
                .put("index.number_of_shards", 1)
                .put("index.number_of_replica", 0)
                .put("cluster.routing.schedule", "50ms")
                .put("gateway.type", "none")
                .put("index.store.type", "ram")
                .put("http.enabled", true)
                .put("discovery.zen.multicast.enabled", false)
                .build();
    }

    public Node node(String id) {
        return nodes.get(id);
    }

    public Client client(String id) {
        return clients.get(id);
    }


}