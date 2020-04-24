package com.raad.converter.convergen.parser;

import com.raad.converter.domain.FormParser;
import com.raad.converter.domain.FormResponse;
import com.raad.converter.domain.ResponseDTO;
import com.raad.converter.util.HtmlAsDocument;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value="prototype")
//https://www.w3.org/TR/html4/interact/forms.html#h-17.2.1
//https://stackoverflow.com/questions/19860392/how-to-get-previous-element-using-jsoup-from-my-current-element-instance-using-j
public class FormParserProcess {

    private String FORM = "FORM";
    private String LABEL = "LABEL";
    private String SELECT = "SELECT";
    private String BUTTON = "BUTTON";
    private String INPUT = "INPUT";
    private String TEXTAREA = "TEXTAREA";
    private String IMAGE_BUTTON = "IMG";

    private String TEXT = "TEXT";
    private String PASSWORD = "PASSWORD";
    private String CHECKBOX = "CHECKBOX";
    private String RADIO = "RADIO";
    private String SUBMIT = "SUBMIT";
    private String RESET = "RESET";
    private String FILE = "FILE";
    private String HIDDEN = "HIDDEN";
    private String IMAGE = "IMAGE";
    private String DROPDOWN = "DROP DOWN";
    private String TYPE = "type";
    private String TITLE = "title";
    private String VALUE = "value";
    private String NAME = "name";
    private String BR = "br";
    private String ID = "id";


    @Autowired
    private HtmlAsDocument htmlAsDocument;

    private List<FormResponse> formResponse;
    private Set<String> controlTypes;

    // 1% chance of wrong data due to checkbox and radio
    // 1% chance of wrong data due to select
    public ResponseDTO parseForm(FormParser formParser) {
        Document htmlDocument = this.htmlAsDocument.getHtml(formParser.getUrl(), formParser.getTag());
        if(htmlDocument != null) {
            this.formResponse = new ArrayList<>();
            this.controlTypes = new HashSet<>();
            // select all of the forms from the document
            //.first().getElementsByTag(FORM)
            Elements formElements = htmlDocument.select(formParser.getTag());
            for(Element formElem : formElements) {
                // select all child tags of the form
                this.processV1(formElem.select("*"));
            }
            if(this.formResponse != null && this.formResponse.size() > 0) {
                HashMap<String, Object> object = new HashMap<>();
                object.put("controlTypes", this.controlTypes);
                object.put("formResponse", this.formResponse);
                return new ResponseDTO("Success Process", object);
            } else {
                return new ResponseDTO("Incorrect Search Form Tag defined", null);
            }
        } else {
            return new ResponseDTO("Incorrect Search Form Tag defined", null);
        }
    }

    public void processV1(Elements formChildren) {
        for(Element formChild : formChildren) {
            // if child have more child the its deep process
            FormResponse formResponse = new FormResponse();
            // || formChild.tagName().equalsIgnoreCase(TEXTAREA)
            if(formChild.tagName().equalsIgnoreCase(INPUT)) {
                Attributes attributes = formChild.attributes();
                //formResponse.setExt(formChild.attributes());
                if(attributes.hasKey(TYPE)) {
                    if(attributes.get(TYPE).equalsIgnoreCase(HIDDEN)) { // HIDDEN
                        continue;
                    } else if(attributes.get(TYPE).equalsIgnoreCase(CHECKBOX)) {
                        formResponse.setControlType(CHECKBOX); // CHECK BOX
                    } else if (attributes.get(TYPE).equalsIgnoreCase(RADIO)) {
                        formResponse.setControlType(RADIO); // RADIO
                    } else if (attributes.get(TYPE).equalsIgnoreCase(SUBMIT)) {
                        formResponse.setControlType(SUBMIT); // SUBMIT
                    }  else if (attributes.get(TYPE).equalsIgnoreCase(BUTTON)){
                        formResponse.setControlType(BUTTON); // TEXT BUTTON
                    } else {
                        formResponse.setControlType(TEXT); // TEXT BOX pass,text
                    }
                } else {
                    // TEXT BOX
                    formResponse.setControlType(TEXT);
                }
                if(!StringUtils.isEmpty(formChild.cssSelector())) {
                    formResponse.setHtmlTag(formChild.cssSelector());
                }
                if(formResponse.getControlType().equals(RADIO) || formResponse.getControlType().equals(CHECKBOX)) {
                    if(formChildren.get(formChildren.indexOf(formChild) + 1) != null &&
                            formChildren.get(formChildren.indexOf(formChild) + 1).tagName().equalsIgnoreCase(LABEL)) {
                        formResponse.setControlName(formChildren.get(formChildren.indexOf(formChild) + 1).text().toUpperCase());
                    } else if(formChildren.get(formChildren.indexOf(formChild) - 1) != null &&
                            formChildren.get(formChildren.indexOf(formChild) - 1).tagName().equalsIgnoreCase(LABEL)) {
                        formResponse.setControlName(formChildren.get(formChildren.indexOf(formChild) - 1).text().toUpperCase());
                    } else if(formChild.text() != null && !formChild.text().equals("")) {
                        formResponse.setControlName(formChild.text().toUpperCase());
                    }
                } else {
                    if(formChildren.get(formChildren.indexOf(formChild) - 1) != null &&
                            formChildren.get(formChildren.indexOf(formChild) - 1).tagName().equalsIgnoreCase(LABEL)) {
                        formResponse.setControlName(formChildren.get(formChildren.indexOf(formChild) - 1).text().toUpperCase());
                    } else if(formChildren.get(formChildren.indexOf(formChild) - 1) != null &&
                            formChildren.get(formChildren.indexOf(formChild) - 1).tagName().equalsIgnoreCase(BR)) {
                        if(formChildren.get(formChildren.indexOf(formChild) - 2) != null &&
                                formChildren.get(formChildren.indexOf(formChild) - 2).tagName().equalsIgnoreCase(LABEL)) {
                            formResponse.setControlName(formChildren.get(formChildren.indexOf(formChild) - 2).text().toUpperCase());
                        }
                    } else if(!StringUtils.isEmpty(formChild.text())) {
                        formResponse.setControlName(formChild.text().toUpperCase());
                    }
                }
                if(StringUtils.isEmpty(formResponse.getControlName()) && attributes.hasKey(ID)) {
                    formResponse.setControlName(attributes.get(ID).toUpperCase());
                } else if(StringUtils.isEmpty(formResponse.getControlName()) && attributes.hasKey(NAME)) {
                    formResponse.setControlName(attributes.get(NAME).toUpperCase());
                } else if(StringUtils.isEmpty(formResponse.getControlName()) && attributes.hasKey(VALUE)) {
                    formResponse.setControlName(attributes.get(VALUE).toUpperCase());
                }
                this.formResponse.add(formResponse);
                this.controlTypes.add(formResponse.getControlType());
            } else if(formChild.tagName().equalsIgnoreCase(BUTTON)) {
                if(!StringUtils.isEmpty(formChild.text())) {
                    formResponse.setControlName(formChild.text().toUpperCase());
                }
                if(!StringUtils.isEmpty(formChild.cssSelector())) {
                    formResponse.setHtmlTag(formChild.cssSelector());
                }
                Attributes attributes = formChild.attributes();
                //formResponse.setExt(formChild.attributes());
                if(attributes.hasKey(TYPE)) {
                    if(attributes.get(TYPE).equalsIgnoreCase(SUBMIT)) { // button as submit
                        formResponse.setControlType(SUBMIT);
                    } else {
                        formResponse.setControlType(BUTTON); // button simple button
                    }
                }
                if(StringUtils.isEmpty(formResponse.getControlName()) && attributes.hasKey(TITLE)) {
                    formResponse.setControlName(attributes.get(TITLE).toUpperCase());
                } else if(StringUtils.isEmpty(formResponse.getControlName()) && attributes.hasKey(VALUE)) {
                    formResponse.setControlName(attributes.get(VALUE).toUpperCase());
                } else if(StringUtils.isEmpty(formResponse.getControlName()) && attributes.hasKey(NAME)) {
                    formResponse.setControlName(attributes.get(NAME).toUpperCase());
                }
                if(StringUtils.isEmpty(formResponse.getControlType())) {
                    formResponse.setControlType(BUTTON); // button simple button
                }
                this.formResponse.add(formResponse);
                this.controlTypes.add(formResponse.getControlType());
            } else if(formChild.tagName().equalsIgnoreCase(SELECT)) {
                // change control name select to dropdown
                //formResponse.setControlType(formChild.tagName().toUpperCase());
                formResponse.setControlType(DROPDOWN);
                Attributes attributes = formChild.attributes();
                //formResponse.setExt(formChild.attributes());
                if( StringUtils.isEmpty(formResponse.getControlName()) && attributes.hasKey(NAME)) {
                    formResponse.setControlName(attributes.get(NAME).toUpperCase());
                } else if( StringUtils.isEmpty(formResponse.getControlName()) && attributes.hasKey(ID)) {
                    formResponse.setControlName(attributes.get(ID).toUpperCase());
                } else if( StringUtils.isEmpty(formResponse.getControlName()) && attributes.hasKey(VALUE)) {
                    formResponse.setControlName(attributes.get(VALUE).toUpperCase());
                } else if(formChildren.get(formChildren.indexOf(formChild) - 1) != null &&
                        formChildren.get(formChildren.indexOf(formChild) - 1).tagName().equalsIgnoreCase(LABEL)) {
                    formResponse.setControlName(formChildren.get(formChildren.indexOf(formChild) - 1).text().toUpperCase());
                } else if(formChildren.get(formChildren.indexOf(formChild) - 1) != null &&
                        formChildren.get(formChildren.indexOf(formChild) - 1).tagName().equalsIgnoreCase(BR)) {
                    if(formChildren.get(formChildren.indexOf(formChild) - 2) != null &&
                            formChildren.get(formChildren.indexOf(formChild) - 2).tagName().equalsIgnoreCase(LABEL)) {
                        formResponse.setControlName(formChildren.get(formChildren.indexOf(formChild) - 2).text().toUpperCase());
                    }
                }
                formResponse.setHtmlTag(formChild.cssSelector());
                HashMap<String, String> option = new HashMap<>();
                for(Element optionElement: formChild.children()) {
                    option.put(optionElement.text(), optionElement.cssSelector());
                }
                //formResponse.setValues(option);
                this.formResponse.add(formResponse);
                this.controlTypes.add(formResponse.getControlType());
            }
        }
    }
}
