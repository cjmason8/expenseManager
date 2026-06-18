package au.com.mason.expensemanager.pdf;

import java.io.IOException;

public class PdfReader {

	public static String extract(byte[] content) throws IOException {
		return PdfTextExtractor.extract(content);
	}

}
