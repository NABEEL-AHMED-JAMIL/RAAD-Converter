/*
   Copyright [2015] argonet.co.kr

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
/*
 * This software has been developed with reference to
 * the HWP file format open specification by Hancom, Inc.
 * http://www.hancom.co.kr/userofficedata.userofficedataList.do?menuFlag=3
 * 한글과컴퓨터의 한/글 문서 파일(.hwp) 공개 문서를 참고하여 개발하였습니다.
 * 
 * 본 제품은 다음의 소스를 참조하였습니다.
 * https://github.com/cogniti/ruby-hwp/
 */
package com.raad.converter.convergen.hwp;

import java.io.*;

import com.raad.converter.convergen.RaadStreamConverter;
import com.raad.converter.convergen.hwp.v3.HwpTextExtractorV3;
import com.raad.converter.convergen.hwp.v5.HwpTextExtractorV5;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HwpTextExtractor {

	protected static Logger log = LoggerFactory.getLogger(HwpTextExtractor.class);

	public static InputStream extract(InputStream inputStream) throws Exception {
		Writer writer = new StringWriter();
		boolean success = HwpTextExtractorV5.extractText(inputStream, writer);
		if (!success)
			success = HwpTextExtractorV3.extractText(inputStream, writer);

		if(!success) {
			throw new Exception("File not a hwp format");
		}

		return new ByteArrayInputStream(writer.toString().getBytes("UTF8"));
	}

	public static void main(String args[]) throws Exception {
		File file = new File("C:\\Users\\Nabeel.Ahmed\\Desktop\\RAAD\\RAAD-Converter\\src\\main\\resources\\hwp\\example.hwp");
		InputStream inputStream = HwpTextExtractor.extract(new FileInputStream(file));
		FileUtils.copyInputStreamToFile(inputStream, new File("C:\\Users\\Nabeel.Ahmed\\Downloads\\example.rtf"));
		System.out.println("Process Done");
	}
}