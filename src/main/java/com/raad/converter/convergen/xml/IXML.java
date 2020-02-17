package com.raad.converter.convergen.xml;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public interface IXML {

    static final String[] elements = new String[] { "database", "author", "namespace", "entity", "column" };

    // xml input type
    final String STRING = "string";
    final String DECIMAL = "decimal";
    final String INTEGER = "integer";
    final String BOOLEAN = "boolean";
    final String DATE = "date";
    final String TIME = "time";
    //============other detail =======
    final String BLANK = "";
    final String START = "*";
    final String XS_ELEMENT = "xs:element";
    final String NAME_ATTRIBUTE = "name";
    final String TYPE_ATTRIBUTE = "type";
    final String HTML = "html";
    final String BODY = "body";
    final String TABLE = "table";
    final String TR = "tr";
    final String TD = "td";
    final String ROOT_REMOVES = "#root > html > ";
    final String STYLE = "style";
    final String SCRIPT = "script";
    final String HASH = "#";
    final String DOT = ".";
    final String GREATER_SIGN = " > ";
    final String NTH_CHILD = ":nth-child(%d)";
    final String UTF8 ="UTF-8";
    final String YES = "yes";
    final String NAME = "{http://xml.apache.org/xslt}indent-amount";
    final String VALUE = "2";
    final String XS_LOADER = "XS-Loader";
    final String ERROR_HANDLER = "error-handler";
    final String VALIDATE = "validate";

    // method use to convert the file
    public default File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convertFile);
        fos.write(file.getBytes());
        fos.close();
        return convertFile;
    }
}
