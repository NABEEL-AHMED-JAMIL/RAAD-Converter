package com.raad.converter.convergen;

import com.raad.converter.convergen.excel.ExcelStreamReader;
import com.raad.converter.convergen.hwp.HwpPdfExtractor;
import com.raad.converter.convergen.hwp.HwpTextExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;


@Slf4j
@Service
@Scope(value="prototype")
public class RaadStreamConverter implements IRaadStreamConverter {

//    0: PDF 1.4 (default selection)
//    1: PDF/A-1 (ISO 19005-1:2005)
//    Example: new Integer(1)
    private String FilterData = "FilterData";
    private String IsSkipEmptyPages = "IsSkipEmptyPages";
    private String SelectPdfVersion = "SelectPdfVersion";
    private String ExportBookmarks = "ExportBookmarks";
    private String ExportNotes = "ExportNotes";

    @Autowired
    private HwpPdfExtractor hwpPdfExtractor;
    @Autowired
    private OfficeManager officeManager;
    @Autowired
    private ExcelStreamReader excelStreamReader;

    public RaadStreamConverter() { }

    /**
     * @param inputStream => stream
     * @param sourceFileName => xyz.xls
     * @param targetFileName => xyz.pdf
     * */
    public ByteArrayOutputStream doConvert(InputStream inputStream, String sourceFileName, String targetFileName) throws Exception {
        if(inputStream == null || (sourceFileName == null || sourceFileName.equals("")) || (targetFileName == null || targetFileName.equals(""))) {
            throw new NullPointerException("File Process File Due To Null Value");
        } else {
            // xls file handling
            if(sourceFileName.contains(ScraperConstant.XLS_EXTENSION) || sourceFileName.contains(ScraperConstant.XLSX_EXTENSION)) {
                inputStream = this.excelStreamReader.getExcelStream(sourceFileName, inputStream, new ByteArrayOutputStream());
             // hwp file handling
            } else if(sourceFileName.contains(ScraperConstant.HWP_EXTENSION)) {
				// looking for hwp file handling
                //String url = this.hwpPdfExtractor.startProcessForUrl(sourceFileName);
                //if(url != null) {
                    //download file and return out-byte-stream
                //    return this.hwpPdfExtractor.downloadFile(url, new ByteArrayOutputStream());
                //} else {
                    // output stream size zero it's mean file not convert through endpoint so we do with own customs scrapper
                    sourceFileName = sourceFileName.replace(ScraperConstant.HWP_EXTENSION, ScraperConstant.TXT_EXTENSION);
                    inputStream = HwpTextExtractor.extract(inputStream);
                //}
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final DocumentConverter converter = LocalConverter.builder().officeManager(officeManager).storeProperties(getStoreProperties()).build();
            final DocumentFormat sourceFormat = DefaultDocumentFormatRegistry.getFormatByExtension(FilenameUtils.getExtension(sourceFileName));
            final DocumentFormat targetFormat = DefaultDocumentFormatRegistry.getFormatByExtension(FilenameUtils.getExtension(targetFileName));
            converter.convert(inputStream).as(sourceFormat).to(outputStream).as(targetFormat).execute();
            return outputStream;
        }
    }

    private HashMap<String, Object> getStoreProperties() {
        HashMap<String, Object> loadProperties = new HashMap<>();
        loadProperties.put(FilterData, getFilterData());
        return loadProperties;
    }

    private HashMap<String, Object> getFilterData() {
        HashMap<String, Object> filterDate = new HashMap<>();
        filterDate.put(IsSkipEmptyPages, new Boolean(true));
        filterDate.put(SelectPdfVersion, new Integer(1));
        filterDate.put(ExportBookmarks, false);
        filterDate.put(ExportNotes, false);

        return filterDate;
    }
}