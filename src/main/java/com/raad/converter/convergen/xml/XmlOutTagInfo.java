package com.raad.converter.convergen.xml;

import com.raad.converter.domain.dto.*;
import com.raad.converter.util.HtmlAsDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Component
@Scope(value="prototype")
//http://zetcode.com/java/dom/
public class XmlOutTagInfo {

    public Logger logger = LogManager.getLogger(XmlOutTagInfo.class);

    private final String HTML = "html";
    private final String BODY = "body";
    private final String TABLE = "table";
    private final String TR = "tr";
    private final String TD = "td";
    private final String ROOT_REMOVES = "#root > html > ";
    private final String STYLE = "style";
    private final String SCRIPT = "script";
    private final String HASH = "#";
    private final String DOT = ".";
    private final String GREATER_SIGN = " > ";
    private final String NTH_CHILD = ":nth-child(%d)";

    private final String UTF8 ="UTF-8";
    private final String YES = "yes";
    private final String NAME = "{http://xml.apache.org/xslt}indent-amount";
    private final String VALUE = "2";

    private TransformerFactory transformerFactory;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

    @Autowired
    private HtmlAsDocument htmlAsDocument;

    @PostConstruct
    public void init() throws Exception {
        logger.info("============Xml Factory InIt============");
        this.factory = DocumentBuilderFactory.newInstance();
        this.factory.setNamespaceAware(true);
        this.builder = this.factory.newDocumentBuilder();
        this.transformerFactory = TransformerFactory.newInstance();
    }

    public XmlTagResponseDTO xmlTagResponseDot(XmlRequest xmlRequest) throws Exception {
        // xml-tag-resposne-dot
        XmlTagResponseDTO xmlTagResponseDto = new XmlTagResponseDTO();
        // url to html tag get here
        xmlTagResponseDto.setTagDetails(getTableTagDetail(xmlRequest.getUrl()));
        // xml node get here
        if(xmlRequest.getFile() != null) {
            xmlTagResponseDto.setTagInfos(getTagInfo(convertMultiPartToFile(xmlRequest.getFile())));
        }
        return xmlTagResponseDto;
    }

    public String makeXml(XmlMakerRequest xmlMakerRequest) throws Exception {
        String xml = null;
        if(xmlMakerRequest.getTags() != null && xmlMakerRequest.getUrl() != null) {
            Document htmlDocument = this.htmlAsDocument.getHtml(xmlMakerRequest.getUrl(), BODY);
            if(htmlDocument != null) {
                org.w3c.dom.Document xmlDoc = this.builder.newDocument();
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
                        child = xmlDoc.createElementNS("",tag_name);
                        // element have value than add else not need
                        if(html_tag != null && !html_tag.equals("")) {
                            child.appendChild(xmlDoc.createTextNode(htmlDocument.select(html_tag).text()));
                        }
                        xmlDoc.appendChild(child);
                        isParent = false;
                    } else {
                        // if parent not define then skip this one
                        if(parent_tag != null && !parent_tag.equals("")) {
                            // first check if parent exist get the old parent else create the new once
                            NodeList nodeList = xmlDoc.getElementsByTagName(parent_tag);
                            if(nodeList != null && nodeList.getLength() > 0) {
                                // old tag which append value
                                Node node = nodeList.item(nodeList.getLength()-1);
                                if(node != null && (tag_name != null && !tag_name.equals(""))) {
                                    child = xmlDoc.createElement(tag_name);
                                    if(html_tag != null && !html_tag.equals("")) {
                                        String text = htmlDocument.select(html_tag).text();
                                        child.appendChild(xmlDoc.createTextNode(text != null ? text: ""));
                                    }
                                    node.appendChild(child);
                                }
                            } else {
                                // main second level child
                                org.w3c.dom.Element parent = xmlDoc.createElement(parent_tag);
                                if(tag_name != null && !tag_name.equals("")) {
                                    child = xmlDoc.createElement(tag_name);
                                    if(html_tag != null && !html_tag.equals("")) {
                                        child.appendChild(xmlDoc.createTextNode(htmlDocument.select(html_tag).text()));
                                    }
                                    parent.appendChild(child);
                                    xmlDoc.getDocumentElement().appendChild(parent);
                                }
                            }
                        } else {
                            // main root level child
                            child = xmlDoc.createElement(tag_name);
                            if(html_tag != null && !html_tag.equals("")) {
                                child.appendChild(xmlDoc.createTextNode(htmlDocument.select(html_tag).text()));
                            }
                            xmlDoc.getDocumentElement().appendChild(child);
                        }
                    }
                }
                // below line use after all root
                Transformer transformer = this.transformerFactory.newTransformer();
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
        return xml;
    }

    private Set<TableTagDetail> getTableTagDetail(String url) throws Exception {
        Set<TableTagDetail> tags = new LinkedHashSet<>();
        Document document = this.htmlAsDocument.getHtml(url, BODY);
        if(document != null) {
            logger.info("Page Found And Table Tag Iteration Start");
            Elements table_element = document.getElementsByTag(TD);
            for (Element td: table_element) {
                TableTagDetail tableTagDetail = new TableTagDetail();
                tableTagDetail.setTagKey(td.text());
                tableTagDetail.setTagValue(getCssPath(td).substring(ROOT_REMOVES.length()));
                tags.add(tableTagDetail);
            }
        }
        return tags;
    }

    private String getCssPath(Element el) {
        if(el == null) { return ""; }
        if(!el.id().isEmpty()) { return HASH + el.id(); }
        StringBuilder selector = new StringBuilder(el.tagName());
        String classes = StringUtil.join(el.classNames(), DOT);
        if(!classes.isEmpty()) { selector.append(DOT).append(classes); }
        if(el.parent() == null) { return selector.toString(); }
        selector.insert(0, GREATER_SIGN);
        if(el.parent().select(selector.toString()).size() > 1) {
            selector.append(String.format(NTH_CHILD, el.elementSiblingIndex() + 1));
        }
        return getCssPath(el.parent()) + selector.toString();
    }

    // deprecate
    private Set<String> getXMlNode(File file) throws Exception {
        Set<String> xmlNode = new LinkedHashSet<>();
        try {
            org.w3c.dom.Document document = this.builder.parse(file);
            document.getDocumentElement().normalize();
            logger.info("Root element " + document.getDocumentElement().getNodeName());
            NodeList nodeList= document.getElementsByTagName("*");
            for (int i=0; i<nodeList.getLength(); i++) {
                Node element = nodeList.item(i);
                xmlNode.add(element.getNodeName());
            }
        }catch (Exception ex) {
            throw ex;
        } finally {
            // delete this on time bz
            // it's take space in the project
            file.deleteOnExit();
        }
        return xmlNode;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private List<TagInfo> getTagInfo(File file) throws Exception {
        List<TagInfo> tagInfos = new ArrayList<>();
        try {
            org.w3c.dom.Document document = this.builder.parse(file);
            document.getDocumentElement().normalize();
            NodeList nodeList= document.getElementsByTagName("*");
            for (int i=0; i<nodeList.getLength(); i++) {
                Node element = nodeList.item(i);
                TagInfo tagInfo = new TagInfo();
                if(i==0) {
                    tagInfo.setTag_name(element.getNodeName());
                } else {
                    tagInfo.setTag_name(element.getNodeName().trim());
                    tagInfo.setParent_tag(element.getParentNode().getNodeName().trim());
                    if(element.getChildNodes().item(0) != null) {
                        tagInfo.setHtml_tag(element.getChildNodes().item(0).getTextContent().trim());
                    }
                }
                tagInfos.add(tagInfo);
            }
        }catch (Exception ex) {
            throw ex;
        } finally {
            file.deleteOnExit();
        }
        return tagInfos;
    }

//    public static void main(String args[]) throws Exception {
//        XmlOutTagInfo outTagInfo = new XmlOutTagInfo();
//        System.out.println(outTagInfo.getTagInfo(new File("C:\\Users\\Nabeel.Ahmed\\Desktop\\XML FILE\\output.xml")));
//    }

}