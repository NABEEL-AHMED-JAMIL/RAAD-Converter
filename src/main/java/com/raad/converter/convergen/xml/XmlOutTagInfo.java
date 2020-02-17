package com.raad.converter.convergen.xml;

import com.google.gson.Gson;
import com.raad.converter.domain.dto.*;
import com.raad.converter.util.HtmlAsDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Component
@Scope(value="prototype")
//http://zetcode.com/java/dom/
public class XmlOutTagInfo extends XSDOutTagInfo {

    public Logger logger = LogManager.getLogger(XmlOutTagInfo.class);

    @Autowired
    private HtmlAsDocument htmlAsDocument;

    public XmlTagResponseDTO xmlTagResponseDot(XmlRequest xmlRequest) throws Exception {
        logger.info("Process For Xml Tag Start");
        XmlTagResponseDTO xmlTagResponseDto = new XmlTagResponseDTO();
        //xmlTagResponseDto.setTagDetails(getTableTagDetail(xmlRequest.getUrl()));
        // if file then process of parse else skip that part
        if(xmlRequest.getFile() != null) {
            xmlTagResponseDto.setTagInfos(getTagInfo(convertMultiPartToFile(xmlRequest.getFile())));
        }
        logger.info("Process For Xml Tag Endss");
        return xmlTagResponseDto;
    }

    // method use to make the xml from to given try
    public String makeXml(XmlMakerRequest xmlMakerRequest) throws Exception {
        logger.info("Process For Xml Create Start");
        String xml = null;
        if(xmlMakerRequest.getTags() != null && xmlMakerRequest.getUrl() != null) {
            Document htmlDocument = this.htmlAsDocument.getHtml(xmlMakerRequest.getUrl(), BODY);
            if(htmlDocument != null) {
                org.w3c.dom.Document xmlDoc = this.getBuilder().newDocument();
                boolean isParent = true;
                for(TagInfo tagInfo: xmlMakerRequest.getTags()) {
                    // these are value
                    String tag_name = tagInfo.getTag_name();
                    String parent_tag = tagInfo.getParent_tag();
                    String html_tag = tagInfo.getHtml_tag();
                    org.w3c.dom.Element child = null;
                    // for first node
                    if(isParent) {
                        // first time it's consider as root
                        child = xmlDoc.createElementNS(BLANK, tag_name);
                        // element have value than add else not need
                        if(html_tag != null && !html_tag.equals(BLANK)) {
                            child.appendChild(xmlDoc.createTextNode(getText(htmlDocument, html_tag)));
                        }
                        xmlDoc.appendChild(child);
                        isParent = false;
                    } else {
                        // if parent not define then skip this one
                        if(parent_tag != null && !parent_tag.equals(BLANK)) {
                            // first check if parent exist get the old parent else create the new once
                            NodeList nodeList = xmlDoc.getElementsByTagName(parent_tag);
                            if(nodeList != null && nodeList.getLength() > 0) {
                                // old tag which append value
                                Node node = nodeList.item(nodeList.getLength()-1);
                                if(node != null && (tag_name != null && !tag_name.equals(BLANK))) {
                                    child = xmlDoc.createElement(tag_name);
                                    if(html_tag != null && !html_tag.equals(BLANK)) {
                                        child.appendChild(xmlDoc.createTextNode(getText(htmlDocument, html_tag)));
                                    }
                                    node.appendChild(child);
                                }
                            } else {
                                // main second level child
                                org.w3c.dom.Element parent = xmlDoc.createElement(parent_tag);
                                if(tag_name != null && !tag_name.equals(BLANK)) {
                                    child = xmlDoc.createElement(tag_name);
                                    if(html_tag != null && !html_tag.equals(BLANK)) {
                                        child.appendChild(xmlDoc.createTextNode(getText(htmlDocument, html_tag)));
                                    }
                                    parent.appendChild(child);
                                    xmlDoc.getDocumentElement().appendChild(parent);
                                }
                            }
                        } else {
                            // main root level child
                            child = xmlDoc.createElement(tag_name);
                            if(html_tag != null && !html_tag.equals(BLANK)) {
                                child.appendChild(xmlDoc.createTextNode(getText(htmlDocument, html_tag)));
                            }
                            xmlDoc.getDocumentElement().appendChild(child);
                        }
                    }
                }
                // below line use after all root
                Transformer transformer = this.getTransformerFactory().newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, UTF8);
                transformer.setOutputProperty(OutputKeys.INDENT, YES);
                transformer.setOutputProperty(NAME, VALUE);
                DOMSource source = new DOMSource(xmlDoc);
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                transformer.transform(source,result);
                xml = result.getWriter().toString();
            }
        }
        logger.info("Process For Xml Create End");
        return xml;
    }

    public String getText(Document htmlDocument, String html_tag) {
        try {
            String text = htmlDocument.select(html_tag).text();
            return text != null && !text.equals("") ? text : html_tag;
        } catch (Exception ex) {
            return html_tag;
        }
    }

    // method use to parse the xml and make the tag for whole xml
    private List<TagInfo> getTagInfo(File file) throws Exception {
        List<TagInfo> tagInfos = new ArrayList<>();
        try {
            org.w3c.dom.Document document = this.getBuilder().parse(file);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName(XS_ELEMENT);
            if(nodeList != null && nodeList.getLength() > 1) {
                org.w3c.dom.Element firt_element = (org.w3c.dom.Element)nodeList.item(0);
                if(firt_element.hasAttributes()) {
                    XSDElement mainElement = parseXSD(file, firt_element.getAttribute(NAME_ATTRIBUTE));
                    if(mainElement != null) {
                        TagInfo tagInfo = new TagInfo();
                        tagInfo.setTag_name(firt_element.getAttribute(NAME_ATTRIBUTE));
                        tagInfos.add(tagInfo); // parent add here
                        getParentChild(mainElement, 0, tagInfos);
                    }
                } else {
                    throw new Exception("Schema Not Valid");
                }
            } else {
                nodeList = document.getElementsByTagName(START);
                for (int i=0; i<nodeList.getLength(); i++) {
                    Node element = nodeList.item(i);
                    TagInfo tagInfo = new TagInfo();
                    if(i==0) {
                        // first node so his parent is document so no need to add
                        tagInfo.setTag_name(element.getNodeName());
                    } else {
                        tagInfo.setTag_name(element.getNodeName().trim());
                        tagInfo.setParent_tag(element.getParentNode().getNodeName().trim());
                        // content value of xml tag if any else left empty
                        if(element.getChildNodes().item(0) != null) {
                            tagInfo.setHtml_tag(element.getChildNodes().item(0).getTextContent().trim());
                        }
                    }
                    tagInfos.add(tagInfo);
                }
            }

        } finally {
            file.delete();
        }
        return tagInfos;
    }

    // method use to get all node key from xml file now
    private Set<String> getXMlNode(File file) throws Exception {
        Set<String> xmlNode = new LinkedHashSet<>();
        try {
            org.w3c.dom.Document document = this.getBuilder().parse(file);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName(START);
            for(int i=0; i<nodeList.getLength(); i++) {
                Node element = nodeList.item(i);
                xmlNode.add(element.getNodeName());
            }
        } finally {
            file.delete();
        }
        return xmlNode;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

}