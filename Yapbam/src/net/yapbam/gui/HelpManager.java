package net.yapbam.gui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import net.yapbam.gui.util.AbstractDialog;
import net.yapbam.util.NullUtils;
import net.yapbam.util.Portable;

/** This class is responsible for displaying Yapbam helpMessages. 
 */
public class HelpManager {
	/** Content class identifies a part of Yapbam help content, for instance help on regular expressions.*/ 
	public static final class Content {
		String key;
		Content (String key) {
			this.key=key;
		}
	};
	/** Help about regular expressions. */
	public static final Content REGULAR_EXPRESSIONS = new Content("regular_expressions"); //$NON-NLS-1$
	/** Help about import. */
	public static final Content IMPORT = new Content("import"); //$NON-NLS-1$
	
	private static final String BUNDLE_NAME = "net.yapbam.gui.helpUrls"; //$NON-NLS-1$
	private static ResourceBundle RESOURCE_BUNDLE;
	private static Locale currentLocale = null;
		
	private static void loadLanguagesLocations() {
		if (!NullUtils.areEquals(currentLocale, Locale.getDefault())) {
			currentLocale = Locale.getDefault();
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		}
	}
	
	/** Shows help.
	 * @param parent Determines the frame in which the help is displayed. Please note, that this is useful if the help is displayed in a dialog box, but
	 * Yapbam is free to use another way to display help. For instance, in a web browser. Even in such a case, this parameter is used if there's an error
	 * to display the alert dialog. 
	 * @param content The content to display.
	 */
	public static final void show(Component parent, Content content) {
		loadLanguagesLocations();
		URI url = new File(Portable.getHelpDirectory(), RESOURCE_BUNDLE.getString(content.key)).toURI();
		show(parent, url);
	}

	/** Shows help located at a specific URL.
	 * This method could be used by Plugins to display their specific help.
	 * @param parent Determines the frame in which the help is displayed. Please note, that this is useful if the help is displayed in a dialog box, but
	 * Yapbam is free to use another way to display help. For instance, in a web browser. Even in such a case, this parameter is used if there's an error
	 * to display the alert dialog. 
	 * @param uri The URI of the document to display.
	 */
	public static void show(Component parent, URI uri) {
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException exception) {
			String message = MessageFormat.format(LocalizationData.get("HelpManager.errorDialog.message"), exception.toString()); //$NON-NLS-1$
			JOptionPane.showMessageDialog(AbstractDialog.getOwnerWindow(parent), message, LocalizationData.get("HelpManager.errorDialog.title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
		} catch (UnsupportedOperationException e) {
			String url = uri.toString();
			String message = MessageFormat.format(LocalizationData.get("HelpManager.unsupported.message"), url); //$NON-NLS-1$
			StringSelection stringSelection = new StringSelection(url);
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents( stringSelection, null);
		    JOptionPane.showMessageDialog(AbstractDialog.getOwnerWindow(parent), message, LocalizationData.get("HelpManager.errorDialog.title"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
		}
	}
}
