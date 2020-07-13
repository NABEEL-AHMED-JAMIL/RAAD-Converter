package com.raad.converter.convergen.excel;

import com.raad.converter.convergen.ScraperConstant;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;


@Component
@Scope(value="prototype")
public class ExcelStreamReader implements Excel {

    private String TODAY = "TODAY()";

    public InputStream getExcelStream(String sourceFileName, InputStream inputStream,
        ByteArrayOutputStream bos) throws Exception {
        // stream re-order to fit the all content
        if(sourceFileName.contains(ScraperConstant.XLSX_EXTENSION)) {
            XSSFWorkbook xssfWorkbook = (XSSFWorkbook) WorkbookFactory.create(inputStream);
            FormulaEvaluator evaluator = xssfWorkbook.getCreationHelper().createFormulaEvaluator();
            Iterator iterator = xssfWorkbook.sheetIterator();
            while (iterator.hasNext()) {
                XSSFSheet xssfSheet = (XSSFSheet) iterator.next();
                disableComments(xssfSheet);
                xssfSheet.setFitToPage(true);
                xssfSheet.setAutobreaks(true);
                XSSFPrintSetup printSetup = xssfSheet.getPrintSetup();
                printSetup.setFitWidth((short) 1);
                printSetup.setFitHeight((short) 0);
                //printSetup.setPaperSize(A4_EXTRA_PAPERSIZE);
            }
            xssfWorkbook.write(bos);
            inputStream = new ByteArrayInputStream(bos.toByteArray());
            xssfWorkbook.close();
        } else {
            HSSFWorkbook xlsWorkbook = (HSSFWorkbook) WorkbookFactory.create(inputStream);
            FormulaEvaluator evaluator = xlsWorkbook.getCreationHelper().createFormulaEvaluator();
            Iterator iterator = xlsWorkbook.sheetIterator();
            while (iterator.hasNext()) {
                HSSFSheet hssfSheet =  (HSSFSheet) iterator.next();
                disableComments(hssfSheet);
                hssfSheet.setFitToPage(true);
                hssfSheet.setAutobreaks(true);
                HSSFPrintSetup hssfPrintSetup = hssfSheet.getPrintSetup();
                hssfPrintSetup.setFitWidth((short) 1);
                hssfPrintSetup.setFitHeight((short) 0);
                //hssfPrintSetup.setPaperSize(A4_EXTRA_PAPERSIZE);
            }
            xlsWorkbook.write(bos);
            inputStream = new ByteArrayInputStream(bos.toByteArray());
            xlsWorkbook.close();
        }
        return inputStream;
    }

    private void disableComments(Sheet sheet) {
        if (sheet instanceof XSSFSheet || sheet instanceof HSSFSheet) {
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.iterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    Comment comment = cell.getCellComment();
                    if (comment != null) {
                        System.out.println("Comment :- " + comment.getString());
                        cell.removeCellComment();
                    }
                    if (cell != null) {
                        if (cell.getCellType() == CellType.FORMULA) {
                            if(cell.getCellFormula().equalsIgnoreCase(TODAY)) {
                                cell.setCellType(CellType.STRING);
                                cell.setCellValue("Mac-Date");
                            }

                        }
                    }
                }
            }
        }
    }

}
