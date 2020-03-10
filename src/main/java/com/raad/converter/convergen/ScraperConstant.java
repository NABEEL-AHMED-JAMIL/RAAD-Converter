package com.raad.converter.convergen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ScraperConstant {

	public static final String UTF8 = "UTF-8";
	public static final String BLANK = " ";
	public static final String ZIP = "zip";

	public static final String PDF = "pdf";
	public static final String PDF_EXTENSION = ".pdf";

	public static final String DOC = "doc";
	public static final String DOC_EXTENSION = ".doc";

	public static final String DOCX = "docx";
	public static final String DOCX_EXTENSION = ".docx";

	public static final String ODT = "odt";
	public static final String ODT_EXTENSION = ".odt";

	public static final String RTF = "rtf";
	public static final String RTF_EXTENSION = ".rtf";

	public static final String TXT = "txt";
	public static final String TXT_EXTENSION = ".txt";

	public static final String PPT = "ppt";
	public static final String PPT_EXTENSION = ".ppt";

	public static final String PPTX = "pptx";
	public static final String PPTX_EXTENSION = ".pptx";

	public static final String XLS = "xls";
	public static final String XLS_EXTENSION = ".xls";

	public static final String XLSX = "xlsx";
	public static final String XLSX_EXTENSION = ".xlsx";

	public static final String DOT = "dot";
	public static final String DOT_EXTENSION = ".dot";
	
	public static final String DOTX = "dotx";
	public static final String DOTX_EXTENSION = ".dotx";

	public static final String HWP = "hwp";
	public static final String HWP_EXTENSION = ".hwp";

	public static final String CSV = "csv";
	public static final String CSV_EXTENSION = ".csv";

	public static final String XML = "xml";
	public static final String XML_EXTENSION = ".xml";

	public static final String ALL = "all";
	public static final String HTML = "html";

	public static  final String CONTENT_TYPE = "Content-Type";

	public static final List<String> headerTypeForPPT = new ArrayList<String>(Arrays.asList("application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation"));
	public static final List<String> headerTypeForTXT = new ArrayList<String>(Arrays.asList("text/plain", "application/msword", "application/vnd.oasis.opendocument.text"));
	public static final List<String> headerValuesForPdf = new ArrayList<String>(Arrays.asList("application/pdf", "application/octet-stream", "application/octet-stream;charset=UTF-8", "application/octet-stream;charset=utf-8","application/pdf;charset=UTF-8"));
	public static final List<String> headerValuesForDoc = new ArrayList<String>(Arrays.asList("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/octet-stream", "application/octet-stream;charset=UTF-8", "application/rtf;charset=UTF-8"));
	public static final List<String> headerValuesForHtml = new ArrayList<String>(Arrays.asList("text/html; charset=UTF-8", "text/html;charset=UTF-8", "text/html; charset=utf-8", "text/html;charset=utf-8", "text/html", "text/html; charset=ISO-8859-1"));// XLS, XLSX
	public static final List<String> headerValuesForXLS = new ArrayList<String>(Arrays.asList("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

	public static final int SCHEDULER_CRON_TIME_IN_MINUTES=2;

}
