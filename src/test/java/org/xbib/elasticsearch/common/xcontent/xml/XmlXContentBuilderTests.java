package org.xbib.elasticsearch.common.xcontent.xml;

import org.junit.Test;
import org.xbib.elasticsearch.common.xcontent.XmlXContentBuilder;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.xbib.elasticsearch.common.xcontent.XmlXContentFactory.xmlBuilder;

public class XmlXContentBuilderTests {

    /**
     * This test is just here to check XML escape and catch any nasty StaX implementations such as the one in the Java JDK.
     * It passes with Woodstox 4.2.0
     * @throws IOException
     */
    @Test
    public void testXmlEscape() throws IOException {
        XmlXContentBuilder builder = xmlBuilder();
        builder.startObject();
        builder.field("element", "greater > or less than <");
        builder.endObject();
        assertEquals(builder.string(),
                "<source xmlns=\"http://xbib.org/ns/sru/elasticsearch/source/1.0/\"><element>greater > or less than &lt;</element></source>");
    }
}
