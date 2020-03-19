package org.liquigraph.core.io.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import io.sniffy.socket.DisableSockets;
import io.sniffy.test.junit.SniffyRule;

public class ExplicitSchemaValidatorTest {

    @Rule
    public SniffyRule sniffyRule = new SniffyRule();
    
    @Test
    @DisableSockets
    public void validates_document_with_no_internet_connection() throws Exception {
        //given
        String document =
                "<changelog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "           xsi:noNamespaceSchemaLocation=\"http://www.liquigraph.org/schema/1.0/liquigraph.xsd\">\n" +
                "</changelog>";
        ExplicitSchemaValidator toTest = new ExplicitSchemaValidator();
        
        //when
        Collection<String> result = toTest.validate(domSource(document));
        
        //then
        assertThat(result).isEmpty();
    }

    private static DOMSource domSource(String xml) throws Exception {
        return new DOMSource(root(xml));
    }

    private static Node root(String xml) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes())) {
            return DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(inputStream)
                    .getDocumentElement();
        }
    }
}
