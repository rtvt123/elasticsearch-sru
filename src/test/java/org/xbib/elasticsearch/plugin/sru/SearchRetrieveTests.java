package org.xbib.elasticsearch.plugin.sru;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;
import org.xbib.elasticsearch.support.AbstractNodeTest;
import org.xbib.elasticsearch.support.TextMatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchRetrieveTests extends AbstractNodeTest {

    @Test
    public void testSearchRetrieve() throws IOException {
        XContentBuilder builder = jsonBuilder()
                .startObject()
                .startObject("dc")
                .field("title", "Hello world")
                .endObject()
                .endObject();
        client("1").index(new IndexRequest().index("test").type("test").id("1").source(builder).refresh(true)).actionGet();

        URI uri = getHttpAddressOfNode("1");
        URL url = new URL(uri.toURL(), "_sru/test");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
        out.write("dc.title=Hello");
        out.close();

        InputStream expected = getClass().getResource("/org/xbib/sru/test.xml").openStream();
        InputStream in = connection.getInputStream();
        assertThat(new BufferedReader(new InputStreamReader(in)),
                TextMatcher.matchesTextLines(new BufferedReader(new InputStreamReader(expected))));
        expected.close();
        in.close();
    }

}
