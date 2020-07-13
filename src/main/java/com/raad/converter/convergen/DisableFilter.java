package com.raad.converter.convergen;

import com.sun.star.lang.XComponent;
import com.sun.star.sheet.XCalculatable;
import org.jodconverter.filter.Filter;
import org.jodconverter.filter.FilterChain;
import org.jodconverter.office.OfficeContext;
import org.jodconverter.office.utils.Lo;


public class DisableFilter implements Filter {

    @Override
    public void doFilter(OfficeContext officeContext, XComponent xComponent, FilterChain filterChain)
            throws Exception {
        Lo.qiOptional(XCalculatable.class, xComponent).ifPresent((x) -> {
            System.out.println("Pass");
            x.enableAutomaticCalculation(false);
        });
        filterChain.doFilter(officeContext, xComponent);
    }
}