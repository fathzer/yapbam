package net.yapbam.gui;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.ResourceBundle;

import net.astesana.ajlib.swing.framework.Application;
import net.yapbam.data.GlobalData;

/** This class is the main entry point for localization concerns.
 */
public abstract class LocalizationData {
	public static final Locale SYS_LOCALE;
	private static net.astesana.ajlib.utilities.LocalizationData locData;
	
	static {
		SYS_LOCALE = new Locale(System.getProperty("user.language"), System.getProperty("user.country"));  //$NON-NLS-1$//$NON-NLS-2$
		reset();
	}
	
	public static void reset() {
		Locale locale = Preferences.safeGetLocale(); // Be aware that Preferences.INSTANCE may not be initialized (if its instantiation failed)
		GlobalData.setDefaultCurrency(Currency.getInstance(locale));
		Locale.setDefault(locale);
		ResourceBundle bundle = ResourceBundle.getBundle("Resources", locale); //$NON-NLS-1$
		locData = new net.astesana.ajlib.utilities.LocalizationData(bundle);
		locData.setTranslatorMode(Preferences.safeIsTranslatorMode());
		Application.LOCALIZATION = locData;
	}
	
	public static String get(String key) {
		return locData.getString(key);
	}
	
	public static char getChar(String key) {
		return locData.getChar(key);
	}

	public static Locale getLocale() {
		return Locale.getDefault();
	}

	public static DecimalFormat getCurrencyInstance() {
		return (DecimalFormat) NumberFormat.getCurrencyInstance(getLocale());
	}
	
	public static URL getURL(String document) {
//		System.out.println(document);
//		URL[] urls = ((URLClassLoader)LocalizationData.class.getClassLoader()).getURLs();
//		System.out.println ("urls :"+Arrays.asList(urls));

		URL url = LocalizationData.class.getResource("/localization/"+getLocale().getLanguage()+"/"+document);  //$NON-NLS-1$//$NON-NLS-2$
//		if (url!=null) {System.out.println("ok 1"); return url;}
		if (url==null) url = LocalizationData.class.getResource("/localization/"+document); //$NON-NLS-1$
//		if (url!=null) {System.out.println("ok 2"); return url;}
//		url = LocalizationData.class.getResource("../../../localization/"+getLocale().getLanguage()+"/"+document);
//		if (url!=null) {System.out.println("ok 3"); return url;}
//		if (url==null) url = LocalizationData.class.getResource("../../../localization/"+document);
//		if (url!=null) System.out.println("ok 4");
		return url;
	}
}
