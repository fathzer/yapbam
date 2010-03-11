package net.yapbam.gui.graphics.balancehistory;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.JPanel;

import net.yapbam.data.BalanceData;
import net.yapbam.data.FilteredData;
import net.yapbam.data.event.DataEvent;
import net.yapbam.data.event.DataListener;
import net.yapbam.gui.AbstractPlugIn;
import net.yapbam.gui.IconManager;
import net.yapbam.gui.LocalizationData;

public class BalanceHistoryPlugIn extends AbstractPlugIn {
	private BalanceHistoryPane panel;
	private BalanceData data;

	public BalanceHistoryPlugIn(FilteredData filteredData, Object restartData) {
		this.data = filteredData.getBalanceData();
		this.panel = new BalanceHistoryPane(data.getBalanceHistory());
		this.setPanelTitle(LocalizationData.get("BalanceHistory.title"));
		this.setPanelToolTip(LocalizationData.get("BalanceHistory.toolTip"));
		testAlert();
		data.addListener(new DataListener() {
			@Override
			public void processEvent(DataEvent event) {
				panel.setBalanceHistory(data.getBalanceHistory());
				testAlert();
			}
		});
	}
	
	private void testAlert() {
		long firstAlertDate = data.getBalanceHistory().getFirstDateUnder(new Date(), null, 0.0);
		String tooltip;
		tooltip = LocalizationData.get("BalanceHistory.toolTip");
		if (firstAlertDate>=0) {
			Date date = new Date();
			if (firstAlertDate>0) date.setTime(firstAlertDate);
			String dateStr = DateFormat.getDateInstance(DateFormat.SHORT, LocalizationData.getLocale()).format(date);
			tooltip = tooltip.replace("'", "''");
			String pattern = "<html>"+tooltip+"<br>"+LocalizationData.get("BalanceHistory.alertTooltipAdd")+"</html>";
			tooltip = MessageFormat.format(pattern, "<b>"+dateStr+"</b>");
		}
		setPanelIcon((firstAlertDate>=0?IconManager.ALERT:null));
		setPanelToolTip(tooltip);
	}

	public JPanel getPanel() {
		return panel;
	}
}
