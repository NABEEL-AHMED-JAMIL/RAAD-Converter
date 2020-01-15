package com.raad.converter.convergen;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public interface IRaadStreamConverter {

    public ByteArrayOutputStream doConvert(InputStream inputStream, String sourceFileName, String targetFileName) throws Exception;

}
