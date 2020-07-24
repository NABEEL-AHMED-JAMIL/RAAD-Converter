package com.raad.converter.convergen.excel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public interface Excel {

    public InputStream getExcelStream(String sourceFileName,
                      InputStream inputStream, ByteArrayOutputStream bos) throws Exception;
}
