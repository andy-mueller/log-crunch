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
        XmlConfiguration xmlConf = new XmlConfiguration(resourceStream);
        decorated = xmlConf.load();
    }
    private static class XmlConfiguration{
        private final Document configDocument;

        public XmlConfiguration(InputStream resourceStream) {
            configDocument = xmlDocumentFromStream(resourceStream);
        }
        LogFileFilterInteractorFactory load() {
            Class<? extends LogFileFilterInteractorFactory> factoryClass = classFromName(getFactoryClassNode(configDocument));

            String jaxbXmlText = getJaxbConfigPartOfXml(configDocument);
            StringReader jaxbStream = new StringReader(jaxbXmlText);

            return JAXB.unmarshal(jaxbStream, factoryClass);
        }

        private String getFactoryClassNode(Document configDocument) {
            return configDocument.getElementsByTagName(FactoryClassSection).item(0).getTextContent();
        }

        private String getJaxbConfigPartOfXml(Document configDocument) {
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
            Document jaxbDoc = getDocumentBuilder().newDocument();
            node = jaxbDoc.importNode(node, true);
            jaxbDoc.appendChild(node);

            return documentToString(jaxbDoc);
        }

        private String documentToString(Document source) {
            Transformer transformer = newTransformer();
            StringWriter writer = new StringWriter();
            try {
                transformer.transform(new DOMSource(source), new StreamResult(writer));
                return writer.toString();
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


        private Document xmlDocumentFromStream(InputStream resourceStream) {
            try {
                return getDocumentBuilder().parse(resourceStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
