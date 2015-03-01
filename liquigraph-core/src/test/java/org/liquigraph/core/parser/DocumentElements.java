package org.liquigraph.core.parser;

import com.google.common.base.Charsets;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

class DocumentElements {

    public static Element documentElement(String xml) throws Exception {
        return DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(Charsets.UTF_8)))
                .getDocumentElement();
    }
}
