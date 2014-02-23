package net.yapbam.gui.graphics.balancehistory;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.fathzer.soft.ajlib.swing.widget.RotatingLabel;
import com.fathzer.soft.ajlib.utilities.NullUtils;

import net.yapbam.data.Alert;
import net.yapbam.data.BalanceHistory;
import net.yapbam.data.FilteredData;
import net.yapbam.data.GlobalData;
import net.yapbam.data.event.AccountAddedEvent;
import net.yapbam.data.event.AccountPropertyChangedEvent;
import net.yapbam.data.event.AccountRemovedEvent;
import net.yapbam.data.event.DataEvent;
import net.yapbam.data.event.DataListener;
import net.yapbam.data.event.EverythingChangedEvent;
import net.yapbam.data.event.TransactionsAddedEvent;
import net.yapbam.data.event.TransactionsRemovedEvent;
import net.yapbam.gui.LocalizationData;
import net.yapbam.util.DateUtils;

import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.slf4j.LoggerFactory;

/** The whole balance history pane, with its controls.
 */
public class BalanceHistoryGraphPane extends JPanel {
	private static final long serialVersionUID = 1L;
		
	private JScrollPane scrollPane;
	private BalanceGraphic graph;
	private BalanceHistoryControlPane control;
	private AlertsPane alerts;
	private BalanceRule rule;
	private FilteredData data;
	private JSlider slider;
	private boolean ignoreEnd;
	
	@SuppressWarnings("unused")
	private BalanceHistoryGraphPane() {
		// This constructor is needed by the windows builder editor
		this(new FilteredData(new GlobalData()));
	}
	
	public BalanceHistoryGraphPane(FilteredData data) {
		super(new BorderLayout());
		this.data = data;
		
		graph = new BalanceGraphic(this.getBalanceHistory(), getBalanceRule().getYAxis());
		graph.setToolTipText(LocalizationData.get("BalanceHistory.chart.toolTip")); //$NON-NLS-1$
		graph.addPropertyChangeListener(BalanceGraphic.SELECTED_DATE_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				control.setReportText(getBalanceReportText());
			}
		});
		scrollPane = new JScrollPane(graph, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.setRowHeaderView(getBalanceRule());
		
		control = new BalanceHistoryControlPane();
		alerts = new AlertsPane();
		control.getIsGridVisible().addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				graph.setGridVisible(e.getStateChange() == ItemEvent.SELECTED);
			}});
		control.getToday().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Date today = DateUtils.getMidnight(new Date());
				setSelectedDate(today);
			}
		});
		control.getSlider().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				graph.setHorizontalScale(control.getSlider().getValue());
			}
		});
		control.setReportText(getBalanceReportText());
		alerts.getAlerts().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Alert alert = alerts.getSelectedAlert();
				BalanceHistoryGraphPane.this.data.getFilter().setValidAccounts(Collections.singletonList(alert.getAccount()));
				setSelectedDate(alert.getDate());
			}
		});
		this.add(alerts, BorderLayout.NORTH);
		this.add(control, BorderLayout.SOUTH);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
		add(leftPanel, BorderLayout.WEST);
		leftPanel.setLayout(new BorderLayout(0, 0));
		
		RotatingLabel lblZoom = new RotatingLabel();
		lblZoom.setHorizontalAlignment(SwingConstants.CENTER);
		lblZoom.setText("Zoom");
		lblZoom.setRotation(-90);
		leftPanel.add(lblZoom, BorderLayout.WEST);
		
		slider = new JSlider();
		slider.setValue(1);
		slider.setMinimum(1);
		slider.setOrientation(SwingConstants.VERTICAL);
		leftPanel.add(slider, BorderLayout.EAST);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				LoggerFactory.getLogger(BalanceHistoryGraphPane.class).debug ("set scale to {}",slider.getValue()); //TODO
			}
		});
		
		data.getGlobalData().addListener(new DataListener() {
			@Override
			public void processEvent(DataEvent event) {
				if ((event instanceof EverythingChangedEvent)
						|| (event instanceof AccountAddedEvent) || (event instanceof AccountRemovedEvent) || (event instanceof AccountPropertyChangedEvent)
						|| (event instanceof TransactionsAddedEvent) || (event instanceof TransactionsRemovedEvent)) {
						onDataChanged();
					}
			}
		});
	}
	
	private BalanceHistory getBalanceHistory() {
		return this.data.getBalanceData().getBalanceHistory();
	}

	private BalanceRule getBalanceRule() {
		if (rule==null) {
			rule = new BalanceRule();
		}
		return rule;
	}

	private String getBalanceReportText() {
		Date date = graph.getSelectedDate();
		String dateStr = DateFormat.getDateInstance(DateFormat.SHORT, LocalizationData.getLocale()).format(date);
		String balance = LocalizationData.getCurrencyInstance().format(this.getBalanceHistory().getBalance(date));
		return MessageFormat.format(LocalizationData.get("BalanceHistory.balance"), dateStr, balance); //$NON-NLS-1$
	}

	private void onDataChanged() {
		Date endDate = ignoreEnd ? null : data.getFilter().getValueDateTo();
		double min = Math.min(0, this.getBalanceHistory().getMinBalance(endDate));
		double max = Math.max(0, this.getBalanceHistory().getMaxBalance(endDate));
		getBalanceRule().getYAxis().setBounds(min, max);

		graph.setPreferredEndDate(endDate);
		graph.setHistory(getBalanceHistory(), getBalanceRule().getYAxis());
		scrollToSelectedDate();
	}
	
	private void setSelectedDate(Date date) {
		if (NullUtils.compareTo(date, BalanceHistoryGraphPane.this.data.getFilter().getValueDateFrom(), true)<0) {
			BalanceHistoryGraphPane.this.data.getFilter().setValueDateFilter(date, BalanceHistoryGraphPane.this.data.getFilter().getValueDateTo());
		} else if (NullUtils.compareTo(date, BalanceHistoryGraphPane.this.data.getFilter().getValueDateTo(), false)>0) {
			BalanceHistoryGraphPane.this.data.getFilter().setValueDateFilter(BalanceHistoryGraphPane.this.data.getFilter().getValueDateFrom(), date);
		}
		graph.setSelectedDate(date);
		scrollToSelectedDate();
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
			if (position < 0) {
				position = 0;
			} else if (position + viewWidth > graphWidth) {
				position = graphWidth-viewWidth;
			}
			viewport.setViewPosition(new Point(position, 0));
		}
	}

	public void setAlerts(Alert[] alerts) {
		this.alerts.setAlerts(alerts);
	}

	public void setIgnoreEnd(boolean ignoreEnd) {
		if (this.ignoreEnd!=ignoreEnd) {
			this.ignoreEnd = ignoreEnd;
			Date endDate = ignoreEnd ? null : data.getFilter().getValueDateTo();
			double min = Math.min(0, this.getBalanceHistory().getMinBalance(endDate));
			double max = Math.max(0, this.getBalanceHistory().getMaxBalance(endDate));
			getBalanceRule().getYAxis().setBounds(min, max);
	
			graph.setPreferredEndDate(endDate);
			scrollToSelectedDate();
		}
	}
}
