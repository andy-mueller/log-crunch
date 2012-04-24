package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.LogFileFilterInteractorFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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

public class JaxbLogFileFilterInteractorFactory {
    private final LogFileFilterInteractorFactory decorated;

    public JaxbLogFileFilterInteractorFactory(InputStream resourceStream) {
        Document configDocument = xmlDocumentFromStream(resourceStream);
        Node factoryClassName = configDocument.getElementsByTagName("factoryClass").item(0);
        Class<? extends LogFileFilterInteractorFactory> factoryClass = classFromName(factoryClassName.getTextContent());

        Node jaxBNode = configDocument.getElementsByTagName("factoryConfig").item(0);
        String jaxbXmlText = nodeToString(jaxBNode.getFirstChild());

        StringReader jaxbStream = new StringReader(jaxbXmlText);

        decorated = JAXB.unmarshal(jaxbStream, factoryClass);
    }

    private String nodeToString(Node node) {
        Transformer transformer = newTransformer();
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);

        Document jaxbDoc = getDocumentBuilder().newDocument();
        Node jaxbNode = node.getChildNodes().item(0)  ;
        jaxbNode = jaxbDoc.adoptNode(jaxbNode);
        jaxbDoc.appendChild(jaxbNode);


        Source source = new DOMSource(jaxbDoc);
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
}
