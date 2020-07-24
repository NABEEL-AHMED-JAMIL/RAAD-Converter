package com.raad.converter.convergen.excel;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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
    public final String MAC_TODAY = "May 6, 2000";

    public InputStream getExcelStream(String sourceFileName, InputStream inputStream,
        ByteArrayOutputStream bos) throws Exception {
        // stream re-order to fit the all content
        Workbook workbook = WorkbookFactory.create(inputStream);
        Iterator iterator = workbook.sheetIterator();
        while (iterator.hasNext()) {
            Sheet sheet = (Sheet) iterator.next();
            disableComments(sheet);
            sheet.setFitToPage(true);
            sheet.setAutobreaks(true);
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setFitWidth((short) 1);
            printSetup.setFitHeight((short) 0);
        }
        workbook.write(bos);
        inputStream = new ByteArrayInputStream(bos.toByteArray());
        workbook.close();
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
                                cell.setCellValue(MAC_TODAY);
                            }

                        }
                    }
                }
            }
        }
    }

}
