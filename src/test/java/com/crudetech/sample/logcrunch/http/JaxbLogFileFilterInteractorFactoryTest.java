package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.TempDir;
import com.crudetech.sample.logcrunch.LogFileFilterInteractorFactory;
import com.crudetech.sample.logcrunch.logback.LogbackLogFileFilterInteractorFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class JaxbLogFileFilterInteractorFactoryTest {

    private static class ResourceStream extends ExternalResource {
        private InputStream resourceStream;
        private final String resourceName;

        ResourceStream(String resourceName) {
            this.resourceName = resourceName;
        }

        @Override
        protected void before() throws Throwable {
            resourceStream = getClass().getResourceAsStream(resourceName);
        }

        @Override
        protected void after() {
            try {
                resourceStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Rule
    public ResourceStream xmlConfig = new ResourceStream("/logfilter-logcrunch-test.xml");

    @Test
    public void constructsFromXml() {
        JaxbLogFileFilterInteractorFactory jaxbFactory =
                new JaxbLogFileFilterInteractorFactory(xmlConfig.resourceStream);

        LogFileFilterInteractorFactory innerFactory = jaxbFactory.getDecorated();


        LogFileFilterInteractorFactory expected = new LogbackLogFileFilterInteractorFactory(new File("/some/path"), Charset.forName("UTF-8"), "yyyyMMdd");

        assertThat(innerFactory, is(expected));
    }


    @Test
    public void xox() throws Exception {
        assertThat(xmlConfig.resourceStream, is(not(nullValue())));

        LogFileFilterInteractorFactory factory =
                new LogbackLogFileFilterInteractorFactory(new TempDir(), Charset.forName("UTF-8"), "yyyyMMdd");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXB.marshal(factory, out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
        Node item = d.getDocumentElement();
        Node nodeValue = item.cloneNode(true);
        Document d2 = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        d2.adoptNode(nodeValue);
        Element root = d2.createElement("filterInteractorFactory");
        d2.appendChild(root);

        Element factoryClass = d2.createElement("factoryClass");
        factoryClass.appendChild(d2.createTextNode(LogbackLogFileFilterInteractorFactory.class.getName()));
        root.appendChild(factoryClass);

        Element factoryConfig = d2.createElement("factoryConfig");
        factoryConfig.appendChild(nodeValue);
        root.appendChild(factoryConfig);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        Source source = new DOMSource(d2);
        transformer.transform(source, result);
        writer.close();
        String xml = writer.toString();

        System.out.println(xml);
    }
}
