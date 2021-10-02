package net.yapbam.gui.graphics.balancehistory;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.Printable;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable.PrintMode;
import javax.swing.SwingConstants;

import com.fathzer.soft.ajlib.swing.table.JTableListener;

import net.yapbam.data.FilteredData;
import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.YapbamState;
import net.yapbam.gui.actions.ConvertToPeriodicalTransactionAction;
import net.yapbam.gui.actions.DeleteTransactionAction;
import net.yapbam.gui.actions.DuplicateTransactionAction;
import net.yapbam.gui.actions.EditTransactionAction;
import net.yapbam.gui.dialogs.export.ExportComponent;
import net.yapbam.gui.util.FriendlyTable;

public class BalanceHistoryTablePane extends JPanel {
	// If you ask yourself why this pane can't be displayed by Eclipse Window Builder, it seems the problem is
	// with JSplitButton. If you replace the JSplitButton with a basic JButton, the component is displayed without any problem
	//TODO Investigate more on this.
	private static final long serialVersionUID = 1L;
	private static final String HIDE_INTERMEDIATE_BALANCE_KEY = BalanceHistoryTablePane.class.getPackage().getName()+".hideIntermediateBalance"; //$NON-NLS-1$

	private JLabel columnMenu;
	BalanceHistoryTable table;
	private FilteredData data;
	private JCheckBox hideIntermediateChkBx;

	public BalanceHistoryTablePane(FilteredData data) {
		this.data = data;
		setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(getTable());

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.add(getColumnMenu(), BorderLayout.EAST);
		northPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.add(getHideIntermediateChkBx(), BorderLayout.WEST);
		southPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
		southPanel.add(getBtnExport(), BorderLayout.EAST);

		add(northPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
	}
	
	private JButton getBtnExport() {
		return new ExportComponent(BalanceHistoryTablePane.this.table);
	}

	private JCheckBox getHideIntermediateChkBx() {
		if (hideIntermediateChkBx == null) {
			hideIntermediateChkBx = new JCheckBox(LocalizationData.get("BalanceHistory.transaction.hideIntermediate")); //$NON-NLS-1$
			hideIntermediateChkBx.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					((BalanceHistoryModel)getTable().getModel()).setHideIntermediateBalances(e.getStateChange()==ItemEvent.SELECTED);
				}
			});
		}
		return hideIntermediateChkBx;
	}

	private JLabel getColumnMenu() {
		if (columnMenu == null) {
			columnMenu = new FriendlyTable.ShowHideColumsMenu(getTable(), LocalizationData.get("MainFrame.showColumns")); //$NON-NLS-1$
			columnMenu.setToolTipText(LocalizationData.get("MainFrame.showColumns.ToolTip")); //$NON-NLS-1$
			columnMenu.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return columnMenu;
	}

	private BalanceHistoryTable getTable() {
		if (table == null) {
			table = new BalanceHistoryTable(data);
			if (data != null) {
				Action edit = new EditTransactionAction(table);
				Action delete = new DeleteTransactionAction(table);
				Action duplicate = new DuplicateTransactionAction(table);
				table.addMouseListener(new JTableListener(new Action[] { edit, duplicate,
						delete, null, new ConvertToPeriodicalTransactionAction(table) }, edit));
			}
		}
		return table;
	}

	public void saveState() {
		YapbamState.INSTANCE.saveState(getTable(), this.getClass().getCanonicalName());
		YapbamState.INSTANCE.put(HIDE_INTERMEDIATE_BALANCE_KEY, Boolean.toString(getHideIntermediateChkBx().isSelected()));
	}

	public void restoreState() {
		YapbamState.INSTANCE.restoreState(getTable(), this.getClass().getCanonicalName());
		getHideIntermediateChkBx().setSelected(Boolean.parseBoolean(YapbamState.INSTANCE.get(HIDE_INTERMEDIATE_BALANCE_KEY, "true"))); //$NON-NLS-1$
	}

	public Printable getPrintable() {
		return getTable().getPrintable(PrintMode.FIT_WIDTH, null, null);
	}
}
