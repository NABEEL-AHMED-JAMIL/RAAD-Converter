package com.raad.converter.convergen;

import com.sun.star.lang.XComponent;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jodconverter.filter.Filter;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.utils.Lo;
import org.jodconverter.office.utils.Write;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextReplacerFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.raad.converter.convergen.TextReplacerFilter.class);

    private final String[] searchList;
    private final String[] replacementList;

    public TextReplacerFilter(String[] searchList, String[] replacementList) {
        Validate.notEmpty(searchList, "Search list is empty", new Object[0]);
        Validate.notEmpty(replacementList, "Replacement list is empty", new Object[0]);
        int searchLength = searchList.length;
        int replacementLength = replacementList.length;
        Validate.isTrue(searchLength == replacementLength, "search array length [%d] and replacement array length [%d] don't match", new Object[]{searchLength, replacementLength});
        this.searchList = (String[]) ArrayUtils.clone(searchList);
        this.replacementList = (String[])ArrayUtils.clone(replacementList);
    }

    public void doFilter(OfficeContext context, XComponent document, FilterChain chain) throws OfficeException {
        LOGGER.debug("Applying the TextReplacerFilter");
        if (Write.isText(document)) {
            this.replaceText(document);
        }
        chain.doFilter(context, document);
    }

    private void replaceText(XComponent document) {
        XReplaceable replaceable = (XReplaceable) Lo.qi(XReplaceable.class, document);
        XReplaceDescriptor replaceDescr = replaceable.createReplaceDescriptor();
        LOGGER.debug("Changing all occurrences of ...");

        for(int i = 0; i < this.searchList.length; ++i) {
            LOGGER.debug("{} -> {}", this.searchList[i], this.replacementList[i]);
            replaceDescr.setSearchString(this.searchList[i]);
            replaceDescr.setReplaceString(this.replacementList[i]);
            replaceable.replaceAll(replaceDescr);
        }

    }
}

