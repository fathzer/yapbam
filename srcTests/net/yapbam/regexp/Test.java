package net.yapbam.regexp;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	static String strPattern = Normalizer.normalize("^�t.*$", Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	static Pattern pattern = Pattern.compile(strPattern, Pattern.UNICODE_CASE+Pattern.CASE_INSENSITIVE);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test ("aetaaab");
		test ("�T�");
		test ("�t�");
		test ("�tait");
		test ("�tre");
		test ("e");
		test ("Etre");
		test ("�");
	}

	private static void test(String string) {
		String toTest = Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		Matcher matcher = pattern.matcher(toTest);
		System.out.println (strPattern+(matcher.matches()?"":" not ")+" matches "+string);
	}
}
