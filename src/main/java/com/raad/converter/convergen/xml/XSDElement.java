package com.raad.converter.convergen.xml;

import com.google.gson.Gson;
import org.apache.xerces.xs.XSElementDeclaration;
import java.util.ArrayList;
import java.util.List;

public class XSDElement {

    private String name;
    private XSElementDeclaration xsDeclaration;
    private XSDElement parent;
    private List<XSDAttribute> attributes = new ArrayList<XSDAttribute>();
    private List<XSDElement> children = new ArrayList<XSDElement>();
    private int minOcurrs;
    private boolean maxOcurrsUnbounded;
    private int maxOcurrs;
    private String type;
    private String defaultValue;

    public XSDElement() { }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public XSElementDeclaration getXsDeclaration() { return xsDeclaration; }
    public void setXsDeclaration(XSElementDeclaration xsDeclaration) { this.xsDeclaration = xsDeclaration; }

    public XSDElement getParent() { return parent; }
    public void setParent(XSDElement parent) { this.parent = parent; }

    public List<XSDAttribute> getAttributes() { return attributes; }
    public void setAttributes(List<XSDAttribute> attributes) { this.attributes = attributes; }
    public void addAttribute(XSDAttribute attribute) { this.attributes.add(attribute); }

    public List<XSDElement> getChildren() { return children; }
    public void setChildren(List<XSDElement> children) { this.children = children; }
    public void addChildren(XSDElement child) { this.children.add(child); }

    public int getMinOcurrs() { return minOcurrs; }
    public void setMinOcurrs(int minOcurrs) { this.minOcurrs = minOcurrs; }

    public boolean isMaxOcurrsUnbounded() { return maxOcurrsUnbounded; }
    public void setMaxOcurrsUnbounded(boolean maxOcurrsUnbounded) { this.maxOcurrsUnbounded = maxOcurrsUnbounded; }

    public int getMaxOcurrs() { return maxOcurrs; }
    public void setMaxOcurrs(int maxOcurrs) { this.maxOcurrs = maxOcurrs; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
