package com.raad.converter.convergen;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.raad.converter.convergen.excel.ExcelStreamReader;
import com.raad.converter.convergen.hwp.HwpTextExtractor;
import org.apache.commons.io.FilenameUtils;
import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.*;


@Component
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
    private DocumentConverter converter;
    private String html = "<!DOCTYPE html>\n" + "<html>\n" + "\t<head>\n" + "\t</head>\n" +
            "<body>\n" + "<p>%s</p>\n" + "</body>\n" + "</html>\n";

    @Autowired
    private OfficeManager officeManager;
    @Autowired
    private ExcelStreamReader excelStreamReader;

    public RaadStreamConverter() { }


    @PostConstruct
    public void init() {
        System.out.println("Office Manager Init");
        this.converter = LocalConverter.builder().storeProperties(getStoreProperties())
           .officeManager(officeManager).build();
        System.out.println("Office Manager End");
    }

    /**
     * @param inputStream => stream
     * @param sourceFileName => xyz.xls
     * @param targetFileName => xyz.pdf
     * */
    public ByteArrayOutputStream doConvert(InputStream inputStream, String sourceFileName, String targetFileName) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if(inputStream == null || (sourceFileName == null || sourceFileName.equals("")) || (targetFileName == null || targetFileName.equals(""))) {
            throw new NullPointerException("File Process File Due To Null Value");
        } else {
            // xls file handling
            if(sourceFileName.contains(ScraperConstant.XLS_EXTENSION) || sourceFileName.contains(ScraperConstant.XLSX_EXTENSION)) {
                inputStream = this.excelStreamReader.getExcelStream(sourceFileName, inputStream, new ByteArrayOutputStream());
             // hwp file handling
            } else if(sourceFileName.contains(ScraperConstant.HWP_EXTENSION)) {
                sourceFileName = sourceFileName.replace(ScraperConstant.HWP_EXTENSION, ScraperConstant.TXT_EXTENSION);
                String text = convertInputStreamToString(HwpTextExtractor.extract(inputStream));
                try{
                    inputStream = new ByteArrayInputStream(text.getBytes());
                    return convert(inputStream, sourceFileName, targetFileName, outputStream);
                } catch (Exception ex) {
                    System.out.println("===========>>Hwp File Try Itext<<===========");
                    String tempHtml = String.format(html, text.replaceAll("\\s+", " "));
                    inputStream = new ByteArrayInputStream(tempHtml.getBytes());
                    return hwpTextToPdfV2(inputStream,  outputStream);
                }
            }
            return convert(inputStream, sourceFileName, targetFileName, outputStream);
        }
    }

    private HashMap<String, Object> getStoreProperties() {
        HashMap<String, Object> loadProperties = new HashMap<>();
        loadProperties.put(FilterData, getFilterData());
        return loadProperties;
    }

    private HashMap<String, Object> getFilterData() {
        HashMap<String, Object> filterDate = new HashMap<>();
//        filterDate.put(IsSkipEmptyPages, new Boolean(true));
//        filterDate.put(SelectPdfVersion, new Integer(1));
        filterDate.put(ExportBookmarks, false);
        filterDate.put(ExportNotes, false);
        return filterDate;
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString();
    }

    private ByteArrayOutputStream convert(InputStream inputStream, String sourceFileName, String targetFileName,
        ByteArrayOutputStream byteArrayOutputStream) throws OfficeException, IOException {
        final DocumentFormat sourceFormat = DefaultDocumentFormatRegistry.getFormatByExtension(FilenameUtils.getExtension(sourceFileName));
        final DocumentFormat targetFormat = DefaultDocumentFormatRegistry.getFormatByExtension(FilenameUtils.getExtension(targetFileName));
        this.converter.convert(inputStream).as(sourceFormat).to(byteArrayOutputStream).as(targetFormat).execute();
        if(inputStream != null) { inputStream.close(); }
        return byteArrayOutputStream;
    }

    public ByteArrayOutputStream hwpTextToPdf(InputStream inputStream, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        ConverterProperties properties = new ConverterProperties();
        properties.setCharset(ScraperConstant.UTF8);
        properties.setFontProvider(new DefaultFontProvider(false, false, true));
        HtmlConverter.convertToPdf(inputStream, byteArrayOutputStream, properties);
        if(inputStream != null) { inputStream.close(); }
        return byteArrayOutputStream;
        // ==>
    }

    public ByteArrayOutputStream hwpTextToPdfV2(InputStream inputStream, ByteArrayOutputStream byteArrayOutputStream) throws Exception {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<ByteArrayOutputStream> callable = () -> {
                ConverterProperties properties = new ConverterProperties();
                properties.setCharset(ScraperConstant.UTF8);
                properties.setFontProvider(new DefaultFontProvider(false, false, true));
                HtmlConverter.convertToPdf(inputStream, byteArrayOutputStream, properties);
                if(inputStream != null) { inputStream.close(); }
                return byteArrayOutputStream;
            };
            System.out.println("Submitting Callable");
            Future<ByteArrayOutputStream> future = executor.submit(callable);
            System.out.println("Do something else while callable is getting executed");
            return future.get(10, TimeUnit.MINUTES);
        } catch (Exception ex) {
            throw new Exception("File Not Convert In given time");
        }

    }

}
