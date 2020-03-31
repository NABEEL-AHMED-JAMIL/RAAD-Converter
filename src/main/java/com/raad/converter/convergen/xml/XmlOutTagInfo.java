package com.raad.converter.convergen.xml;

import com.github.kklisura.cdt.launch.ChromeArguments;
import com.github.kklisura.cdt.launch.ChromeLauncher;
import com.github.kklisura.cdt.protocol.commands.Page;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.ChromeService;
import com.github.kklisura.cdt.services.types.ChromeTab;
import com.google.gson.Gson;
import com.raad.converter.domain.TagInfo;
import com.raad.converter.domain.XmlMakerRequest;
import com.raad.converter.domain.XmlRequest;
import com.raad.converter.domain.XmlTagResponseDTO;
import com.raad.converter.util.ExceptionUtil;
import com.raad.converter.util.FullPageScreenshotExample;
import com.raad.converter.util.HtmlAsDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;


@Component
@Scope(value="prototype")
/**
 * Gen method for create the xml with out attribute
 * */
public class XmlOutTagInfo extends XSDOutTagInfo {

    public Logger logger = LoggerFactory.getLogger(XmlOutTagInfo.class);

    private String pdfFilePath = "C:\\Users\\Nabeel.Ahmed\\Downloads\\" + "%s" + ".pdf";
    private String pngFilePath = "C:\\Users\\Nabeel.Ahmed\\Downloads\\" + "%s" + ".png";
    private String htmlFilePath = "C:\\Users\\Nabeel.Ahmed\\Downloads\\" + "%s" + ".html";

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
            Document htmlDocument = htmlAsDocument.seleniumChromeDriver(xmlMakerRequest.getUrl(), BODY);
            if(htmlDocument != null) {
                org.w3c.dom.Document xmlDoc = this.getBuilder().newDocument();
                boolean isParent = true;
                for(TagInfo tagInfo: xmlMakerRequest.getTags()) {
                    String tag_name = tagInfo.getTag_name();
                    String parent_tag = tagInfo.getParent_tag();
                    String html_tag = tagInfo.getHtml_tag();
                    Boolean cdata_tag = tagInfo.getCdata();
                    org.w3c.dom.Element child;
                    // for first node
                    if(isParent) {
                        // first time it's consider as root
                        child = xmlDoc.createElementNS(BLANK, tag_name);
                        // element have value than add else not need
                        addTagDetail(htmlDocument, xmlDoc, child, html_tag, cdata_tag);
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
                                    addTagDetail(htmlDocument, xmlDoc, child, html_tag, cdata_tag);
                                    node.appendChild(child);
                                }
                            } else {
                                // main second level child
                                org.w3c.dom.Element parent = xmlDoc.createElement(parent_tag);
                                if(tag_name != null && !tag_name.equals(BLANK)) {
                                    child = xmlDoc.createElement(tag_name);
                                    addTagDetail(htmlDocument, xmlDoc, child, html_tag, cdata_tag);
                                    parent.appendChild(child);
                                    xmlDoc.getDocumentElement().appendChild(parent);
                                }
                            }
                        } else {
                            // main root level child
                            child = xmlDoc.createElement(tag_name);
                            addTagDetail(htmlDocument, xmlDoc, child, html_tag, cdata_tag);
                            xmlDoc.getDocumentElement().appendChild(child);
                        }
                    }
                }
                // page_url
                org.w3c.dom.Element child;
                if(xmlMakerRequest.getPage_url()) {
                    child = xmlDoc.createElement(PAGE_URL);
                    addTagValue(htmlDocument, xmlDoc, child, xmlMakerRequest.getUrl());
                    xmlDoc.getDocumentElement().appendChild(child);
                }
                if(xmlMakerRequest.getHtml_cdata()) {
                    child = xmlDoc.createElement(BODY);
                    addCdata(htmlDocument, xmlDoc, child, HTML, xmlMakerRequest.getHtml_cdata());
                    xmlDoc.getDocumentElement().appendChild(child);
                }
                if(xmlMakerRequest.getHtml()) {
                    // create an html file on given file path
                    Writer unicodeFileWriter = new OutputStreamWriter
                        (new FileOutputStream(String.format(htmlFilePath, UUID.randomUUID())), "UTF-8");
                    unicodeFileWriter.write(htmlDocument.toString());
                    unicodeFileWriter.close();
                }
                if(xmlMakerRequest.getPdf()) {
                    // pdf image
                    //htmlConvert(xmlMakerRequest.getUrl(), String.format(pdfFilePath, UUID.randomUUID()));

                    // set no-sandbox true to bypass OS security in docker image
                    Map<String, Object> additionalChromeArguments = new HashMap<>();
                    additionalChromeArguments.put("no-sandbox", true);

                    // create chrome argument with additional properties and headless true
                    ChromeArguments chromeArguments = ChromeArguments.builder().noFirstRun().noDefaultBrowserCheck()
                            .disableBackgroundNetworking().disableBackgroundTimerThrottling()
                            .disableClientSidePhishingDetection().disableDefaultApps().disableExtensions().disableHangMonitor()
                            .disablePopupBlocking().disablePromptOnRepost().disableSync().disableTranslate()
                            .metricsRecordingOnly().safebrowsingDisableAutoUpdate().headless(true).disableGpu(true)
                            .hideScrollbars(true).muteAudio(true).additionalArguments(additionalChromeArguments).build();

                    // Create chrome launcher.
                    final ChromeLauncher launcher = new ChromeLauncher();
                    // Launch chrome either as headless (true) or regular (false).
                    final ChromeService chromeService = launcher.launch(chromeArguments);
                    // Create empty tab ie about:blank.
                    final ChromeTab tab = chromeService.createTab();
                    // Get DevTools service to this tab
                    final ChromeDevToolsService devToolsService = chromeService.createDevToolsService(tab);
                    // Get individual commands
                    final Page page = devToolsService.getPage();
                    String fileName = String.format(pngFilePath, UUID.randomUUID());
                    page.onLoadEventFired(event -> {
                        System.out.println("Taking screenshot...");
                        FullPageScreenshotExample.captureFullPageScreenshot(devToolsService, page, fileName);
                        System.out.println("Done!");
                        devToolsService.close();
                    });
                    // Enable page events.
                    page.enable();
                    // Navigate to github.com.
                    page.navigate(xmlMakerRequest.getUrl());
                    devToolsService.waitUntilClosed();
                    try(PDDocument doc = new PDDocument()) {
                        FullPageScreenshotExample.addImageAsNewPage(doc,fileName);
                        doc.save( String.format(pdfFilePath, UUID.randomUUID()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
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

    private void addTagDetail(Document htmlDocument, org.w3c.dom.Document xmlDoc, org.w3c.dom.Element child, String html_tag, Boolean cdata_tag) throws Exception {
        addTagValue(htmlDocument, xmlDoc, child, html_tag);
        addCdata(htmlDocument, xmlDoc, child, html_tag, cdata_tag);
    }

    private void addTagValue(Document htmlDocument, org.w3c.dom.Document xmlDoc, org.w3c.dom.Element child, String html_tag) throws Exception {
        if(html_tag != null && !html_tag.equals(BLANK)) {
            child.appendChild(xmlDoc.createTextNode(getText(htmlDocument, html_tag)));
        }
    }

    private void addCdata(Document htmlDocument, org.w3c.dom.Document xmlDoc, org.w3c.dom.Element child, String html_tag, Boolean cdata_tag) throws Exception {
        if((cdata_tag != null && cdata_tag) && (html_tag != null && !html_tag.equals(BLANK))) {
            child.appendChild(xmlDoc.createCDATASection(getOuterHtml(htmlDocument, html_tag)));
        }
    }

    private String getText(Document htmlDocument, String html_tag) {
        try {
            String text = null;
            if(html_tag.contains(COMMA)) {
                StringBuilder builder = new StringBuilder();
                for(String tag: html_tag.split(COMMA)) {
                    builder.append(htmlDocument.select(tag.trim()).text() + " ");
                }
                text = builder.toString();
                return (text != null && !text.equals(BLANK)) ? text : html_tag;
            } else {
                text = htmlDocument.select(html_tag).text();
                return (text != null && !text.equals(BLANK)) ? text : html_tag;
            }
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
            return html_tag;
        }
    }

    private String getOuterHtml(Document htmlDocument, String html_tag) {
        try {
            String outerHtml = null;
            if(html_tag.contains(COMMA)) {
                StringBuilder builder = new StringBuilder();
                for(String tag: html_tag.split(COMMA)) {
                    builder.append(htmlDocument.select(tag.trim()).outerHtml() + " ");
                }
                outerHtml = builder.toString();
                return (outerHtml != null && !outerHtml.equals(BLANK)) ? outerHtml : html_tag;
            } else {
                outerHtml = htmlDocument.select(html_tag).outerHtml();
                return (outerHtml != null && !outerHtml.equals(BLANK)) ? outerHtml : html_tag;
            }
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
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

    public int htmlConvert(String url, String filePath) throws Exception {
        logger.info("Process Start");
        int returnValue = -1;
        Process p;
        if (System.getProperty("os.name").startsWith("Windows")) {
            p = Runtime.getRuntime().exec("cmd /C start chrome --headless --hide-scrollbars --disable-gpu --no-sandbox --print-to-pdf=" + filePath + " " + url);
        } else {
            p = Runtime.getRuntime().exec("google-chrome --headless --hide-scrollbars --disable-gpu --no-sandbox --print-to-pdf=" + filePath + " " + url);
        }
        p.waitFor();
        returnValue = p.exitValue();
        logger.info("Process Complete.");
        return returnValue;
    }

    public int imageConvert(String url, String filePath) throws Exception {
        logger.info("Process Start");
        int returnValue = -1;
        Process p;
        if (System.getProperty("os.name").startsWith("Windows")) {
            p = Runtime.getRuntime().exec("cmd /C start chrome --headless --hide-scrollbars --disable-gpu --window-size=1280,1696 --screenshot=" + filePath + " --default-background-color=0 " + url);
        } else {
            p = Runtime.getRuntime().exec("google-chrome --headless --hide-scrollbars --disable-gpu --window-size=1280,1696 --screenshot=" + filePath + " --default-background-color=0 " + url);
        }
        p.waitFor();
        returnValue = p.exitValue();
        logger.info("Process Complete.");
        return returnValue;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

}