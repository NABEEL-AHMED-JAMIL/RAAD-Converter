package com.raad.converter.convergen.xml;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

public class XSDOutTagInfo implements IXML {

    public Logger logger = LogManager.getLogger(XSDOutTagInfo.class);

    private TransformerFactory transformerFactory;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

    // local schema info
    private String schemaType;
    private String elementFormDefault;
    private String attributeFormDefault;
    private String name;
    private String type;
    private String indicator;

    @PostConstruct
    public void init() throws Exception {
        logger.info("============Xml Factory InIt============");
        this.factory = DocumentBuilderFactory.newInstance();
        this.factory.setNamespaceAware(true);
        this.builder = this.factory.newDocumentBuilder();
        this.transformerFactory = TransformerFactory.newInstance();
        logger.info("============Xml Factory End============");
    }

    public XSDOutTagInfo() { }

    public TransformerFactory getTransformerFactory() { return transformerFactory; }
    public void setTransformerFactory(TransformerFactory transformerFactory) { this.transformerFactory = transformerFactory; }

    public DocumentBuilderFactory getFactory() { return factory; }
    public void setFactory(DocumentBuilderFactory factory) { this.factory = factory; }

    public DocumentBuilder getBuilder() { return builder; }
    public void setBuilder(DocumentBuilder builder) { this.builder = builder; }

    public String getSchemaType() { return schemaType; }
    public void setSchemaType(String schemaType) { this.schemaType = schemaType; }

    public String getElementFormDefault() { return elementFormDefault; }
    public void setElementFormDefault(String elementFormDefault) { this.elementFormDefault = elementFormDefault; }

    public String getAttributeFormDefault() { return attributeFormDefault; }
    public void setAttributeFormDefault(String attributeFormDefault) { this.attributeFormDefault = attributeFormDefault; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getIndicator() { return indicator; }
    public void setIndicator(String indicator) { this.indicator = indicator; }

    public void parseSchema() {

    }

    public void validateSchemaToXml() {

    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
