package net.yapbam.gui.graphics.balancehistory;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.yapbam.data.BalanceHistory;
import net.yapbam.gui.LocalizationData;

public class BalanceHistoryPane extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private BalanceHistory balanceHistory;
	
	private BalanceGraphic graph;
	private JLabel report;
	private BalanceRule rule;
	private JCheckBox isGridVisible;

	private JScrollPane scrollPane;
	
	public BalanceHistoryPane(BalanceHistory history) {
		super(new BorderLayout());
		this.balanceHistory = history;
		rule = new BalanceRule(this.balanceHistory);
		
		createGraphic();
		
		JPanel southPane = new JPanel(new BorderLayout());
		this.isGridVisible = new JCheckBox(LocalizationData.get("BalanceHistory.showGrid")); //$NON-NLS-1$
		this.isGridVisible.setToolTipText(LocalizationData.get("BalanceHistory.showGrid.toolTip")); //$NON-NLS-1$
		this.isGridVisible.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				graph.setGridVisible(e.getStateChange() == ItemEvent.SELECTED);
			}});
		southPane.add(this.isGridVisible,BorderLayout.EAST);
		this.report = new JLabel(getBalanceReportText(),SwingConstants.CENTER);
		this.report.setToolTipText(LocalizationData.get("BalanceHistory.report.toolTip")); //$NON-NLS-1$
		southPane.add(this.report,BorderLayout.CENTER);
		
		this.add(southPane, BorderLayout.SOUTH);
	}

	private String getBalanceReportText() {
		Date date = graph.getSelectedDate();
		String dateStr = DateFormat.getDateInstance(DateFormat.SHORT, LocalizationData.getLocale()).format(date);
		String balance = LocalizationData.getCurrencyInstance().format(this.balanceHistory.getBalance(date));
		String text = MessageFormat.format(LocalizationData.get("BalanceHistory.balance"), dateStr, balance); //$NON-NLS-1$
		return text;
	}

	public void setBalanceHistory(BalanceHistory history) {
		this.balanceHistory = history;
		this.rule.setBalanceHistory(history);
		this.remove(scrollPane);
		createGraphic();
		graph.setGridVisible(isGridVisible.isSelected());
		this.report.setText(getBalanceReportText());
		this.validate();
	}
	
	private void createGraphic() {
		graph = new BalanceGraphic(this.balanceHistory, rule.getYAxis());
		graph.setToolTipText(LocalizationData.get("BalanceHistory.chart.toolTip")); //$NON-NLS-1$
		graph.addPropertyChangeListener(BalanceGraphic.SELECTED_DATE_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				report.setText(getBalanceReportText());
			}
		});
		scrollPane = new JScrollPane(graph, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setRowHeaderView(rule);
	}

	/** Scrolls the graphic in order to have the currently selected date visible.
	 */
	void scrollToSelectedDate() {
		JViewport viewport = scrollPane.getViewport();
		int viewWidth = viewport.getWidth();
		int selected = graph.getX(graph.getSelectedDate());
		int graphWidth = graph.getPreferredSize().width;
		if ((viewport.getViewPosition().x > selected) || (viewport.getViewPosition().x+viewWidth<selected)) {
			//Do nothing if selected date is already visible.
			int position = selected-viewWidth/2;
			if (position < 0) position = 0;
			else if (position + viewWidth > graphWidth) position = graphWidth-viewWidth;
			viewport.setViewPosition(new Point(position, 0));
		}
	}
}
