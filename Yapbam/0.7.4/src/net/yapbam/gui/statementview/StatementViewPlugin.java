package net.yapbam.gui.statementview;

import javax.swing.JPanel;

import net.yapbam.data.FilteredData;
import net.yapbam.gui.AbstractPlugIn;
import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.YapbamState;

public class StatementViewPlugin extends AbstractPlugIn {
	private static final String STATE_PREFIX = "net.yapbam.statementView."; //$NON-NLS-1$
	private StatementViewPanel panel;

	public StatementViewPlugin(FilteredData data, Object state) {
		this.panel = new StatementViewPanel(data.getGlobalData());
		this.setPanelTitle(LocalizationData.get("StatementView.title")); //$NON-NLS-1$
		this.setPanelToolTip(LocalizationData.get("StatementView.tooltip")); //$NON-NLS-1$
	}

	@Override
	public JPanel getPanel() {
		return this.panel;
	}

	@Override
	public void restoreState() {
		YapbamState.restoreState(panel.getTransactionsTable(), STATE_PREFIX);
	}

	@Override
	public void saveState() {
		YapbamState.saveState(panel.getTransactionsTable(), STATE_PREFIX);
	}

	@Override
	public boolean allowMenu(int menuId) {
		if (menuId==FILTER_MENU) return false;
		return super.allowMenu(menuId);
	}
}
