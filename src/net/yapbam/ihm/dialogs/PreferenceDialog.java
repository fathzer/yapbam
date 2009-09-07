package net.yapbam.ihm.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.yapbam.ihm.Preferences;

import java.lang.Object;
import java.lang.String;

@SuppressWarnings("serial")
public class PreferenceDialog extends AbstractDialog {

	private LocalizationPanel localizationPanel;

	public PreferenceDialog(Window owner) {
		super(owner, "Pr�f�rences", null); //LOCAL
	}

	@Override
	protected Object buildResult() {
		long result = 0;
		if (localizationPanel.isChanged()) {
			Preferences.INSTANCE.setLocale(localizationPanel.getBuiltLocale(), localizationPanel.isDefaultCountry(), localizationPanel.isDefaultLanguage());
			result += LOCALIZATION_CHANGED;
		}
		//TODO Other panels
		return result;
	}

	@Override
	protected JPanel createCenterPane(Object data) {
		JPanel panel = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		localizationPanel = new LocalizationPanel();
		tabbedPane.add("Localisation", localizationPanel);
		tabbedPane.add("Pr�sentation", new JPanel()); //TODO
		panel.add(tabbedPane, BorderLayout.CENTER);
		return panel;
	}

	@Override
	protected String getOkDisabledCause() {
		return null;
	}
	
	public static final long LOCALIZATION_CHANGED = 1;
	public static final long LOOK_AND_FEEL_CHANGED = 2;

	public Long getChanges() {
		return (Long) buildResult();
	}
}
