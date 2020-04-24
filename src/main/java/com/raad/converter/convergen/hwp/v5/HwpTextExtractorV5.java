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
package com.raad.converter.convergen.hwp.v5;


import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.raad.converter.convergen.hwp.utils.HwpStreamReader;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.util.LittleEndian;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class HwpTextExtractorV5 {

	protected static Logger log = LogManager.getLogger(HwpTextExtractorV5.class);

	private static final byte[] HWP_V5_SIGNATURE = "HWP Document File".getBytes();

	private static final int[] HWP_CONTROL_CHARS = new int[] { 0, 10, 13, 24, 25, 26, 27, 28, 29, 30, 31 };
	private static final int[] HWP_INLINE_CHARS = new int[] { 4, 5, 6, 7, 8, 9, 19, 20 };
	private static final int[] HWP_EXTENDED_CHARS = new int[] { 1, 2, 3, 11, 12, 14, 15, 16, 17, 18, 21, 22, 23 };
	private static final int HWPTAG_BEGIN = 0x010;

	// extract text first check the file type if the file type is 5 then this file process
	public static boolean extractText(InputStream inputStream, Writer writer) throws IOException {
		POIFSFileSystem fs = null;
		try {
			FileHeader header;
			try {
				// read the stream
				fs = new POIFSFileSystem(inputStream);
				// check the header
				header = getHeader(fs);
			} catch (IOException ex) {
				log.warn("HWP Exception :- ", ex);
				return false;
			}
			// header 5.0 then prcess else it's not a hwp 5.0 file
			if (header == null) { return false; }

			// If you're here, then you think the HWP document is correct
			// IOException from now on is an error while reading the HWP.

			// distribution document .. Section in ViewText, not BodyText
			if (header.viewtext) {
				extractViewText(header, fs, writer);
			} else {
				extractBodyText(header, fs, writer);
			}

			return true;
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					log.warn("Exception", e);
				}
			}
		}
	}

	// check the type of file with the help of file header
	// oky understand
	private static FileHeader getHeader(POIFSFileSystem fs) throws IOException {
		DirectoryNode root = fs.getRoot();
		Entry headerEntry = root.getEntry("FileHeader");
		if (!headerEntry.isDocumentEntry()) { return null; }
		byte[] header = new byte[256];
		DocumentInputStream headerStream = new DocumentInputStream((DocumentEntry) headerEntry);
		try {
			int read = headerStream.read(header);
			if (read != 256 || !Arrays.equals(HWP_V5_SIGNATURE, Arrays.copyOfRange(header, 0, HWP_V5_SIGNATURE.length))) {
				return null;
			}
		} finally {
			headerStream.close();
		}

		FileHeader fileHeader = new FileHeader();
		fileHeader.version = HwpVersion.parseVersion(LittleEndian.getUInt(header, 32)); //5.0.0.
		long flags = LittleEndian.getUInt(header, 36);
		log.debug("Flags={}", Long.toBinaryString(flags).replace(' ', '0'));
		fileHeader.compressed = (flags & 0x01) == 0x01;
		fileHeader.encrypted = (flags & 0x02) == 0x02;
		fileHeader.viewtext = (flags & 0x04) == 0x04;

		return fileHeader;
	}

	private static void extractBodyText(FileHeader header, POIFSFileSystem fs, Writer writer) throws IOException {
		DirectoryNode root = fs.getRoot();
		Entry bodyText = root.getEntry("BodyText");
		if (bodyText == null || !bodyText.isDirectoryEntry())
			throw new IOException("Invalid BodyText");

		Iterator<Entry> iterator = ((DirectoryEntry) bodyText).getEntries();
		while (iterator.hasNext()) {
			Entry entry = iterator.next();
			if (entry.getName().startsWith("Section") && entry instanceof DocumentEntry) {
				log.debug("extract {}", entry.getName());
				InputStream input = new DocumentInputStream((DocumentEntry) entry);
				try {
					if (header.compressed) {
						input = new InflaterInputStream(input, new Inflater(true));
					}
					HwpStreamReader sectionStream = new HwpStreamReader(input);
					extractText(sectionStream, writer);
				} finally {
					try {
						input.close();
					} catch (IOException e) {
						log.error("있을 수 없는 일?", e);
					}
				}
			} else {
				log.warn("알수없는 Entry '{}'({})", entry.getName(), entry);
			}
		}
	}

	/**
	 * @param writer
	 *
	 * @return
	 * @throws IOException
	 */
	private static void extractViewText(FileHeader header, POIFSFileSystem fs, Writer writer) throws IOException {
		DirectoryNode root = fs.getRoot();
		Entry bodyText = root.getEntry("ViewText");
		if (bodyText == null || !bodyText.isDirectoryEntry()) {
			throw new IOException("Invalid ViewText");
		}
		Iterator<Entry> iterator = ((DirectoryEntry) bodyText).getEntries();
		while (iterator.hasNext()) {
			Entry entry = iterator.next();
			if (entry.getName().startsWith("Section") && entry instanceof DocumentEntry) {
				log.debug("extract {}", entry.getName());
				InputStream input = new DocumentInputStream((DocumentEntry) entry);

				Key key = readKey(input);
				try {
					input = createDecryptStream(input, key);
					if (header.compressed) {
						input = new InflaterInputStream(input, new Inflater(true));
					}
					HwpStreamReader sectionStream = new HwpStreamReader(input);
					extractText(sectionStream, writer);
				} catch (InvalidKeyException e) {
					throw new IOException(e);
				} catch (NoSuchAlgorithmException e) {
					throw new IOException(e);
				} catch (NoSuchPaddingException e) {
					throw new IOException(e);
				} finally {
					try {
						input.close();
					} catch (IOException e) {
						log.error("있을 수 없는 일?", e);
					}
				}
			} else {
				log.warn("알수없는 Entry '{}'({})", entry.getName(), entry);
			}
		}
	}

	// https://groups.google.com/forum/#!msg/hwp-foss/d2KL2ypR89Q/lCTkebPcIYYJ
	private static class SRand {
		private int random_seed;

		private SRand(int seed) {
			random_seed = seed;
		}

		private int rand() {
			random_seed = (random_seed * 214013 + 2531011) & 0xFFFFFFFF;
			return (random_seed >> 16) & 0x7FFF;
		}
	}

	private static Key readKey(InputStream input) throws IOException {
		byte[] data = new byte[260];
		input.read(data, 0, 4);
		input.read(data, 0, 256);
		SRand srand = new SRand(LittleEndian.getInt(data));
		byte xor = 0;
		for (int i = 0, n = 0; i < 256; i++, n--) {
			if (n == 0) {
				xor = (byte) (srand.rand() & 0xFF);
				n = (int) ((srand.rand() & 0xF) + 1);
			}
			if (i >= 4) {
				data[i] = (byte) ((data[i]) ^ (xor));
			}
		}

		int offset = 4 + (data[0] & 0xF);
		byte[] key = Arrays.copyOfRange(data, offset, offset + 16);

		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		return secretKey;
	}

	public static InputStream createDecryptStream(InputStream input, Key key) throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = null;
		cipher = Cipher.getInstance("AES/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new CipherInputStream(input, cipher);
	}

	private static void extractText(HwpStreamReader sectionStream, Writer writer) throws IOException {
		StringBuffer buf = new StringBuffer(1024);
		TagInfo tag = new TagInfo();
		while (true) {
			if (!readTag(sectionStream, tag)) { break; }
			buf.setLength(0);
			if (HWPTAG_BEGIN + 50 == tag.id) {
				writeParaHeader(sectionStream, tag.length, buf);
			} else if (HWPTAG_BEGIN + 51 == tag.id) {
				if (tag.length % 2 != 0) {
					throw new IOException("Invalid block size");
				}
				writeParaText(sectionStream, tag.length, buf);
				if (buf.length() > 0) {
					writer.append(buf.toString()).append('\n');
				}
			} else {
				sectionStream.ensureSkip(tag.length);
			}

			if (buf.length() > 0) {
				log.debug("TAG[{}]({}):{} [{}]", new Object[] { tag.id, tag.level, tag.length, buf });
			}
		}
	}

	private static void writeParaHeader(HwpStreamReader sectionStream,
			long length, StringBuffer buf) throws IOException {
		// log.debug("text={}", sectionStream.uint32());
		// log.debug("control mask={}", sectionStream.uint32());
		// log.debug("문단모양아이디참조값={}", sectionStream.uint16());
		// log.debug("문단스타일아이디참조값={}", sectionStream.uint8());
		// log.debug("단나누기종류={}", sectionStream.uint8());
		// log.debug("글자모양정보수={}", sectionStream.uint16());
		// log.debug("range tag정보수={}", sectionStream.uint16());
		// log.debug("각줄에 대한 align정보수={}", sectionStream.uint16());
		// log.debug("문단 Instance ID={}", sectionStream.uint32());
		// sectionStream.ensureSkip(2);
		sectionStream.ensureSkip(length);
	}

	/**
	 * HWPTAG_PARA_TEXT 의 문자스트림을 문자열로 변환
	 *
	 * @param sectionStream
	 * @param datasize
	 * @param buf
	 * @throws IOException
	 */
	private static void writeParaText(HwpStreamReader sectionStream, long datasize, StringBuffer buf) throws IOException {
		int[] chars = sectionStream.uint16((int) (datasize / 2));
		for (int index = 0; index < chars.length; index++) {
			int ch = chars[index];
			if (Arrays.binarySearch(HWP_INLINE_CHARS, ch) >= 0) {
				if (ch == 9) {
					buf.append('\t');
				}
				index += 7;
			} else if (Arrays.binarySearch(HWP_EXTENDED_CHARS, ch) >= 0) {
				index += 7;
			} else if (Arrays.binarySearch(HWP_CONTROL_CHARS, ch) >= 0) {
				buf.append(' ');
			} else {
				buf.append((char) ch);
			}
		}
	}

	private static boolean readTag(HwpStreamReader sectionStream, TagInfo tag) throws IOException {
		// p.24
		long recordHeader = sectionStream.uint32();
		if (recordHeader == -1) { return false; }
		// log.debug("Record Header={} [{}]", recordHeader,
		// Long.toHexString(recordHeader));
		tag.id = recordHeader & 0x3FF;
		tag.level = (recordHeader >> 10) & 0x3FF;
		tag.length = (recordHeader >> 20) & 0xFFF;
		// 확장 데이터 레코드 p.24
		if (tag.length == 0xFFF) { tag.length = sectionStream.uint32(); }
		return true;
	}

	public static class FileHeader {
		HwpVersion version;
		boolean compressed; // bit 0
		boolean encrypted; // bit 1
		boolean viewtext; // bit 2
	}

	public static class TagInfo {
		long id;
		long level;
		long length;
	}

	public static class HwpVersion {
		int m;
		int n;
		int p;
		int r;

		public static HwpVersion parseVersion(long longVersion) {
			HwpVersion version = new HwpVersion();
			version.m = (int) ((longVersion & 0xFF000000L) >> 24);
			version.n = (int) ((longVersion & 0x00FF0000L) >> 16);
			version.p = (int) ((longVersion & 0x0000FF00L) >> 8);
			version.r = (int) ((longVersion & 0x000000FFL));
			return version;
		}

		public String toString() {
			return String.format("%d.%d.%d.%d", m, n, p, r);
		}
	}

}
