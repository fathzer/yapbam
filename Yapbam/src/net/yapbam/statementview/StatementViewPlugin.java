package net.yapbam.statementview;

import javax.swing.JPanel;

import net.yapbam.data.FilteredData;
import net.yapbam.gui.AbstractPlugIn;

public class StatementViewPlugin extends AbstractPlugIn {
	
	private StatementViewPanel panel;

	public StatementViewPlugin(FilteredData data, Object state) {
		this.panel = new StatementViewPanel(data.getGlobalData());
		this.setPanelTitle("Relev�s de comptes"); //LOCAL
		this.setPanelToolTip("Vue relev�s de comptes"); //TODO
	}

	@Override
	public JPanel getPanel() {
		return this.panel;
	}

}
