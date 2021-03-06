package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.InteractorFactory;
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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

class XmlConfiguredInteractorFactory<TInteractor> implements InteractorFactory<TInteractor> {
    private final InteractorFactory<TInteractor> decorated;

    XmlConfiguredInteractorFactory(InputStream resourceStream) {
        XmlConfiguration<InteractorFactory<TInteractor>> xmlConf
                = new XmlConfiguration<InteractorFactory<TInteractor>>(resourceStream);
        decorated = xmlConf.load();
    }

    private static class XmlConfiguration<T> {
        private final Document configDocument;
        private static final String FactoryClassSection = "factoryClass";
        private static final String FactoryConfigSection = "factoryConfig";

        public XmlConfiguration(InputStream resourceStream) {
            configDocument = xmlDocumentFromStream(resourceStream);
        }

        T load() {
            Class<? extends T> factoryClass = getFactoryType();
            Reader jaxbStream = getJaxbConfigPartOfXml(configDocument);
            return JAXB.unmarshal(jaxbStream, factoryClass);
        }

        private Class<? extends T> getFactoryType() {
            String factoryClassTypeName = configDocument.getElementsByTagName(FactoryClassSection).item(0).getTextContent();
            return classFromName(factoryClassTypeName);
        }

        private Reader getJaxbConfigPartOfXml(Document configDocument) {
            Node factoryConfig = configDocument.getElementsByTagName(FactoryConfigSection).item(0);
            Node configElement = getFirstElementOf(factoryConfig.getChildNodes());
            return new StringReader(nodeToString(configElement));
        }

        private Node getFirstElementOf(NodeList nodes) {
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node current = nodes.item(i);
                if (current.getNodeType() == Node.ELEMENT_NODE) {
                    return current;
                }
            }
            throw new IllegalArgumentException();
        }

        private String nodeToString(Node node) {
            Document temporaryDocument = createDocumentWithNode(node);
            return documentToString(temporaryDocument);
        }

        private Document createDocumentWithNode(Node node) {
            Document temporaryDocument = getDocumentBuilder().newDocument();
            node = temporaryDocument.importNode(node, true);
            temporaryDocument.appendChild(node);
            return temporaryDocument;
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
        private Class<? extends T> classFromName(String factoryClassName) {
            try {
                return (Class<? extends T>) Class.forName(factoryClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }


        private volatile DocumentBuilder documentBuilder;

        private DocumentBuilder getDocumentBuilder() {
            if (documentBuilder == null) {
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

    InteractorFactory<TInteractor> getDecorated() {
        return decorated;
    }

    @Override
    public TInteractor createInteractor() {
        return getDecorated().createInteractor();
    }
}
