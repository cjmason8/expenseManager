package au.com.mason.expensemanager.util;

import java.util.Arrays;
import java.util.List;

public class CollectionUtil {

	public static List<String> splitAndConvert(String line, String delimiter) {
		return Arrays.asList(line.split(delimiter));
	}
}
