package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractor;
import com.crudetech.sample.logcrunch.LogFileFilterInteractorFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

class XmlConfiguredLogFileFilterInteractorFactory implements LogFileFilterInteractorFactory{
    private static final String FactoryClassSection = "factoryClass";
    private static final String FactoryConfigSection = "factoryConfig";
    private final LogFileFilterInteractorFactory decorated;

    XmlConfiguredLogFileFilterInteractorFactory(InputStream resourceStream) {
        Document configDocument = xmlDocumentFromStream(resourceStream);
        Node factoryClassName = configDocument.getElementsByTagName(FactoryClassSection).item(0);
        Class<? extends LogFileFilterInteractorFactory> factoryClass = classFromName(factoryClassName.getTextContent());

        String jaxbXmlText = getConfigXml(configDocument);

        StringReader jaxbStream = new StringReader(jaxbXmlText);

        decorated = JAXB.unmarshal(jaxbStream, factoryClass);
    }

    private String getConfigXml(Document configDocument) {
        Node factoryConfig = configDocument.getElementsByTagName(FactoryConfigSection).item(0);
        NodeList childNodes = factoryConfig.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); ++i){
            Node current = childNodes.item(i);
            if(current.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            return nodeToString(current);
        }
        throw new IllegalArgumentException();
    }

    private String nodeToString(Node node) {
        Transformer transformer = newTransformer();
        StringWriter writer = new StringWriter();

        Document jaxbDoc = getDocumentBuilder().newDocument();
        node = jaxbDoc.importNode(node, true);
        node = jaxbDoc.adoptNode(node);
        jaxbDoc.appendChild(node);


        Source source = new DOMSource(jaxbDoc);
        Result result = new StreamResult(writer);
        transform(transformer, result, source);
        return writer.toString();
    }

    private void transform(Transformer transformer, Result result, Source source) {
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private Transformer newTransformer() {
        try {
            return TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends LogFileFilterInteractorFactory> classFromName(String factoryClassName) {
        try {
            return (Class<? extends LogFileFilterInteractorFactory>) Class.forName(factoryClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Document xmlDocumentFromStream(InputStream resourceStream) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resourceStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private volatile DocumentBuilder documentBuilder;
    private DocumentBuilder getDocumentBuilder(){
        if(documentBuilder == null){
            documentBuilder = newDocumentBuilder();
        }
        return documentBuilder;
    }

    private DocumentBuilder newDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public LogFileFilterInteractorFactory getDecorated() {
        return decorated;
    }

    @Override
    public LogFileFilterInteractor createInteractor() {
        return decorated.createInteractor();
    }
}
