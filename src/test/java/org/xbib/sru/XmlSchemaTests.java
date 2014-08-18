package org.xbib.sru;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class XmlSchemaTests {

    @Test
    public void validateSearchResponse() throws IOException, SAXException, ParserConfigurationException {
        InputStream expected = getClass().getResource("test.xml").openStream();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder parser = builderFactory.newDocumentBuilder();
        Document document = parser.parse(expected);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // I found the sru:recordIdentifier missing in OASIS SearchRetrieve, and reported to LoC.
        // LoC fixed the XSD. But, since then, OASIS did not fix the standard.
        Schema schema = factory.newSchema(new URL("http://www.loc.gov/standards/sru/sru-2-0/schemas/sruResponse.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(document));
        expected.close();
    }
}
