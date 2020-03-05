package com.raad.converter.convergen.xml;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.raad.converter.domain.dto.TagInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.impl.xs.XSAttributeUseImpl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.xs.*;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.LSInput;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//http://www.herongyang.com/XSD/JAXP-XSD-Schema-XML-DOM-Validator.html
//https://github.com/jaarrechea/XSDParser
public class XSDOutTagInfo implements DOMErrorHandler, IXML {

    public Logger logger = LoggerFactory.getLogger(XSDOutTagInfo.class);

    private TransformerFactory transformerFactory;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

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

    public XSDElement parseXSD(File schema, String mainElement) throws IOException, ClassNotFoundException,
        InstantiationException, IllegalAccessException, ClassCastException {
        return parseXSD(new FileInputStream(schema), buildHelperList(mainElement), true);
    }

    public XSDElement parseXSD(File schema, List<String> elements) throws IOException, ClassNotFoundException,
        InstantiationException, IllegalAccessException, ClassCastException {
        return   parseXSD(new FileInputStream(schema), elements);
    }

    public XSDElement parseXSD(URL url, String mainElement) throws IOException, ClassNotFoundException,
        InstantiationException, IllegalAccessException, ClassCastException {
        return parseXSD(url.openStream(), buildHelperList(mainElement), true);
    }

    public XSDElement parseXSD(URL url, List<String> elements) throws IOException, ClassNotFoundException,
        InstantiationException, IllegalAccessException, ClassCastException {
        return parseXSD( url.openStream(), elements);
    }

    public XSDElement parseXSD(String schemaName, String mainElement) throws IOException, ClassNotFoundException,
        InstantiationException, IllegalAccessException, ClassCastException {
        return parseXSD(new File(schemaName), mainElement);
    }

    public XSDElement parseXSD(String schemaName, List<String> elements) throws IOException, ClassNotFoundException,
        InstantiationException, IllegalAccessException, ClassCastException {
        return parseXSD(new File(schemaName), elements);
    }

    public XSDElement parseXSD(InputStream inputStream, String mainElement) throws IOException, ClassNotFoundException,
        InstantiationException, IllegalAccessException, ClassCastException {
        return parseXSD(inputStream, buildHelperList(mainElement), true);
    }

    public List<String> buildHelperList(String mainElement){
        List<String> elements = new ArrayList<String>();
        elements.add(mainElement);
        return elements;
    }

    public XSDElement parseXSD(InputStream inputStream, List<String> elements) throws IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, ClassCastException {
        return parseXSD(inputStream, elements, false);
    }

    public void processElement(XSDElement xsdElement, List<String> elements, boolean allElements){
        elements(xsdElement, elements, allElements);
        attributes(xsdElement);
        List<XSDElement> children = xsdElement.getChildren();
        if (children!=null && children.size()>0){
            for (XSDElement child:children){
                processElement(child, elements, allElements);
            }
        }
    }

    public XSDElement parseXSD(InputStream inputStream, List<String> elements, boolean allElements) throws IOException,
        ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        XSDElement mainElement=null;
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        XSImplementation impl = (XSImplementation) registry.getDOMImplementation(XS_LOADER);
        XSLoader schemaLoader = impl.createXSLoader(null);
        DOMConfiguration config = schemaLoader.getConfig();
        // create Error Handler
        DOMErrorHandler errorHandler = new XSDOutTagInfo();
        // set error handler
        config.setParameter(ERROR_HANDLER, errorHandler);
        // set validation feature
        config.setParameter(VALIDATE, Boolean.TRUE);
        // parse document
        LSInput input = new DOMInputImpl( );
        input.setByteStream(inputStream);
        XSModel model = schemaLoader.load(input);
        if (model != null) {
            // Main element
            XSElementDeclaration element = model.getElementDeclaration(elements.get(0), null);
            mainElement = new XSDElement();
            mainElement.setName(elements.get(0));
            mainElement.setXsDeclaration(element);
            processElement(mainElement, elements, allElements);
        }
        return mainElement;
    }

    public void elements(XSDElement xsdElement, List<String> elements, boolean allElements){
        XSElementDeclaration element = xsdElement.getXsDeclaration();
        if(element.getTypeDefinition() instanceof XSSimpleTypeDefinition) {
            XSSimpleTypeDefinition simple =  (XSSimpleTypeDefinition) element.getTypeDefinition();
            xsdElement.setType(simple.getName());
            XSValue xsValue = element.getValueConstraintValue();
            if (xsValue !=null && !StringUtils.isBlank(xsValue.getNormalizedValue())){
                xsdElement.setDefaultValue(xsValue.getNormalizedValue());
            } else {
                xsdElement.setDefaultValue("");
            }
            return;
        } else if(element.getTypeDefinition() instanceof XSComplexTypeDecl) {
            XSComplexTypeDecl definition =  (XSComplexTypeDecl) element.getTypeDefinition();
            XSParticle particle = definition.getParticle();
            if(particle != null){
                XSTerm term = particle.getTerm();
                if(term instanceof XSModelGroup){
                    XSModelGroup xsModelGroup = (XSModelGroup)term;
                    XSObjectList xsol = xsModelGroup.getParticles();
                    for(Object p : xsol ){
                        XSParticle part = (XSParticle) p;
                        XSTerm pterm = part.getTerm();
                        if(pterm instanceof XSElementDeclaration){
                            //xs:element inside complex type
                            String name = pterm.getName();
                            if (allElements || elements.contains(name)) {
                                XSDElement child = new XSDElement();
                                child.setName(name);
                                child.setParent(xsdElement);
                                child.setXsDeclaration((XSElementDeclaration)pterm);
                                child.setMinOcurrs(part.getMinOccurs());
                                child.setMaxOcurrs(part.getMaxOccurs());
                                child.setMaxOcurrsUnbounded(part.getMaxOccursUnbounded());
                                xsdElement.addChildren(child);
                            }
                        }
                    }
                }
            }
        }
    }

    public void attributes(XSDElement xsdElement){
        XSElementDeclaration element = xsdElement.getXsDeclaration();
        if (element.getTypeDefinition() instanceof XSComplexTypeDecl) {
            XSComplexTypeDecl definition =  (XSComplexTypeDecl) element.getTypeDefinition();
            if (definition==null) return;
            XSObjectList xsol = definition.getAttributeUses();
            if (xsol!=null && xsol.getLength()>0){
                attributes:
                for (int j=0; j<xsol.getLength();j++){
                    XSAttributeUseImpl attr = (XSAttributeUseImpl)xsol.item(j);
                    XSValue xsValue = attr.getValueConstraintValue();
                    XSAttributeDecl decl = (XSAttributeDecl)attr.getAttrDeclaration();
                    if (decl==null) { continue attributes; }
                    XSDAttribute attribute = new XSDAttribute();
                    xsdElement.addAttribute(attribute);
                    attribute.setName(decl.getName());
                    attribute.setRequired(attr.getRequired());
                    if (xsValue !=null && !StringUtils.isBlank(xsValue.getNormalizedValue())){
                        attribute.setDefaultValue(xsValue.getNormalizedValue());
                    } else {
                        attribute.setDefaultValue("");
                    }
                    XSSimpleTypeDefinition type = decl.getTypeDefinition();
                    String typeName = type==null?"":type.getName();
                    attribute.setType(typeName);
                    if (type instanceof XSSimpleTypeDecl) {
                        ObjectList list = ((XSSimpleTypeDecl)type).getActualEnumeration();
                        if (list!=null && list.getLength()>0){
                            for (int k=0; k<list.getLength(); k++){
                                attribute.getOptions().add((String)list.get(k));
                            }
                            if (attribute.isRequired()){
                                attribute.setDefaultValue(attribute.getOptions().get(0));
                            }
                        }
                    }
                }
            }
        }
    }

    // if the file is schema store this file in some place ftp
    public Schema loadSchemaFromUrl(String url) throws Exception {
        logger.info("Schema From Url :- " + url);
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new URL(url));
    }

    public Schema loadSchemaFromFile(File file) throws Exception {
        logger.info("Schema From File :- " + file.getName());
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(file);
    }

    public Document parseXmlDOMByFile(File file) throws Exception {
        logger.info("DOM XML From File :- " + file.getName());
        return this.builder.parse(file);
    }

    public Document parseXmlDomByStream(InputStream xmlDomStream) throws Exception {
        logger.info("DOM XML From Stream.");
        return this.builder.parse(xmlDomStream);
    }

    // if schema with xml valid then return true else show exception
    public boolean validateXml(Schema schema, Document document) throws Exception {
        Validator validator = schema.newValidator();
        logger.info("Validator Class :- " + validator.getClass().getName());
        // validating the document against the schema
        validator.validate(new DOMSource(document));
        logger.info("Validation passed.");
        return true;
    }

    @Override
    public boolean handleError(DOMError error) {
        short severity = error.getSeverity();
        if (severity == DOMError.SEVERITY_ERROR) {
            logger.error("[xs-error]: " + error.getMessage());
        }
        if (severity == DOMError.SEVERITY_WARNING) {
            logger.error("[xs-warning]: " + error.getMessage());
        }
        return true;
    }

    public void getParentChild(XSDElement xsdElement, int level, List<TagInfo> tagInfos) {
        if (xsdElement.getChildren().size() > 0) {
            for (XSDElement child : xsdElement.getChildren()) {
                logger.info("Level :- " + level + " Parent :- " + xsdElement.getName() + " Child :- " + child.getName());
                TagInfo tagInfo = new TagInfo();
                tagInfo.setTag_name(child.getName());
                tagInfo.setParent_tag(xsdElement.getName());
                tagInfos.add(tagInfo);
                getParentChild(child, level + 2, tagInfos);
            }
        }
    }

    private void printData(XSDElement xsdElement, int level) {
        String margin = StringUtils.repeat(" ", level);
        logger.info(margin + "Element " + xsdElement.getName() + " ->"
            + " minOcurres=" + xsdElement.getMinOcurrs() + " maxOcurres=" + xsdElement.getMaxOcurrs()
            + " unbounded=" + xsdElement.isMaxOcurrsUnbounded() + " type=" + xsdElement.getType()
            + " default=" + xsdElement.getDefaultValue());
        for (XSDAttribute attribute : xsdElement.getAttributes()) {
            logger.info(margin + "-- " + attribute.getName() + " ->" +
                " type=" + attribute.getType() + " required=" + attribute.isRequired() +
                " default=" + attribute.getDefaultValue());
            for (String option : attribute.getOptions()) {
                logger.info(margin + "---- " + option);
            }
        }
        if (xsdElement.getChildren().size() > 0) {
            logger.info(margin + "Children of " + xsdElement.getName());
            for (XSDElement child : xsdElement.getChildren()) { printData(child, level + 2); }
        }
    }

    @Override
    public String toString() { return new Gson().toJson(this); }


}
