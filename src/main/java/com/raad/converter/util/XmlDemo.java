package com.raad.converter.util;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;


//<parent>
//    <dependency>
//        <groupId>org.jdom</groupId>
//        <artifactId>jdom2</artifactId>
//        <version>2.0.6</version>
//    </dependency>
//    <dependency>
//        <groupId>org.jdom-1</groupId>
//        <artifactId>jdom1</artifactId>
//        <version>2.0.0</version>
//    </dependency>
//</parent>


public class XmlDemo {

    public static void main(String args[]) {
        try {

            Document document = new Document();
            Element root = new Element("parent");
            // Creating a child for the root element. Here we can see how to
            // set the text of an xml element.
            Element child = new Element("dependency");
            child.addContent(new Element("groupId").setText("org.jdom"));
            child.addContent(new Element("artifactId").setText("jdom2"));
            child.addContent(new Element("version").setText("2.0.6"));
            root.addContent(child);

            child = new Element("dependency");
            child.addContent(new Element("groupId").setText("org.jdom-1"));
            child.addContent(new Element("artifactId").setText("jdom1"));
            child.addContent(new Element("version").setText("2.0.6"));
            root.addContent(child);

            // set the root
            document.setContent(root);

            FileWriter writer = new FileWriter("userinfo.xml");
            XMLOutputter outputter = new XMLOutputter();
            // Set the XLMOutputter to pretty formatter. This formatter
            // use the TextMode.TRIM, which mean it will remove the
            // trailing white-spaces of both side (left and right)
            outputter.setFormat(Format.getPrettyFormat());
            // Write the document to a file and also display it on the
            // screen through System.out.
            outputter.output(document, writer);
            outputter.output(document, System.out);

        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
    }

}
