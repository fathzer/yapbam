package net.yapbam.gui.transfer;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;

import net.astesana.ajlib.swing.widget.CurrencyWidget;
import net.astesana.ajlib.swing.widget.date.DateWidget;
import net.astesana.ajlib.utilities.NullUtils;
import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.dialogs.CategoryWidget;
import java.awt.GridLayout;
import net.yapbam.data.GlobalData;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;

import net.yapbam.gui.dialogs.SubtransactionListPanel;
import net.yapbam.gui.widget.AutoSelectFocusListener;

public class TransferPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public static final String OK_DISABLED_CAUSE_PROPERTY = "okDisabledCause"; //$NON-NLS-1$

	private JPanel upperPane;
	private FromOrToPane fromPane;
	private FromOrToPane toPane;
	private JLabel dateLabel;
	private DateWidget dateField;
	private JLabel amountLabel;
	private CurrencyWidget amountField;
	private JPanel panel;
	private CategoryWidget categoryWidget;

	private GlobalData data;
	private String okDisabledCause;
	private PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			updateOkDisabledCause();
		}
	};
	private SubtransactionListPanel subTransactionsPanel;
	
	/**
	 * Create the panel.
	 */
	public TransferPanel(GlobalData data) {
		this.data = data;
		initialize();
		updateOkDisabledCause();
	}

	private void initialize() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);
		GridBagConstraints gbc_upperPane = new GridBagConstraints();
		gbc_upperPane.insets = new Insets(0, 5, 0, 5);
		gbc_upperPane.weightx = 1.0;
		gbc_upperPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_upperPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_upperPane.gridx = 0;
		gbc_upperPane.gridy = 0;
		add(getUpperPane(), gbc_upperPane);
		GridBagConstraints gbc_subTransactionsPanel = new GridBagConstraints();
		gbc_subTransactionsPanel.fill = GridBagConstraints.BOTH;
		gbc_subTransactionsPanel.gridx = 0;
		gbc_subTransactionsPanel.gridy = 2;
		add(getSubTransactionsPanel(), gbc_subTransactionsPanel);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridwidth = 0;
		gbc_panel.weighty = 1.0;
		gbc_panel.weightx = 1.0;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(getPanel(), gbc_panel);
	}

	private JPanel getUpperPane() {
		if (upperPane == null) {
			upperPane = new JPanel();
			GridBagLayout gbl_upperPane = new GridBagLayout();
			upperPane.setLayout(gbl_upperPane);
			GridBagConstraints gbc_dateLabel = new GridBagConstraints();
			gbc_dateLabel.insets = new Insets(0, 0, 0, 5);
			gbc_dateLabel.anchor = GridBagConstraints.WEST;
			gbc_dateLabel.gridx = 0;
			gbc_dateLabel.gridy = 0;
			upperPane.add(getDateLabel(), gbc_dateLabel);
			GridBagConstraints gbc_dateField = new GridBagConstraints();
			gbc_dateField.insets = new Insets(0, 0, 0, 5);
			gbc_dateField.gridx = 1;
			gbc_dateField.gridy = 0;
			upperPane.add(getDateField(), gbc_dateField);
			GridBagConstraints gbc_amountLabel = new GridBagConstraints();
			gbc_amountLabel.insets = new Insets(0, 5, 0, 5);
			gbc_amountLabel.anchor = GridBagConstraints.EAST;
			gbc_amountLabel.gridx = 2;
			gbc_amountLabel.gridy = 0;
			upperPane.add(getAmountLabel(), gbc_amountLabel);
			GridBagConstraints gbc_amountField = new GridBagConstraints();
			gbc_amountField.anchor = GridBagConstraints.WEST;
			gbc_amountField.insets = new Insets(0, 0, 0, 5);
			gbc_amountField.gridx = 3;
			gbc_amountField.gridy = 0;
			upperPane.add(getAmountField(), gbc_amountField);
			GridBagConstraints gbc_categoryWidget = new GridBagConstraints();
			gbc_categoryWidget.weightx = 1.0;
			gbc_categoryWidget.fill = GridBagConstraints.HORIZONTAL;
			gbc_categoryWidget.gridwidth = 0;
			gbc_categoryWidget.gridx = 4;
			gbc_categoryWidget.gridy = 0;
			upperPane.add(getCategoryWidget(), gbc_categoryWidget);
		}
		return upperPane;
	}
	private FromOrToPane getFromPane() {
		if (fromPane == null) {
			fromPane = new FromOrToPane(data, true);
			fromPane.getAccountWidget().setToolTipText("S�lectionnez le compte de d�part dans ce menu");
			fromPane.getValueDateField().setToolTipText("Entrez ici la date de valeur pour le compte de d�part");
			fromPane.setBorder(new TitledBorder(null, "From", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, null));
			fromPane.addPropertyChangeListener(FromOrToPane.ACCOUNT_PROPERTY, listener);
			fromPane.addPropertyChangeListener(FromOrToPane.VALUE_DATE_PROPERTY, listener);
			fromPane.setDate(getDateField().getDate());
		}
		return fromPane;
	}
	private FromOrToPane getToPane() {
		if (toPane == null) {
			toPane = new FromOrToPane(data, false);
			if ((data!=null) && (data.getAccountsNumber()>1)) toPane.setAccount(data.getAccount(1));
			toPane.getAccountWidget().setToolTipText("S�lectionnez le compte de d'arriv�e dans ce menu");
			toPane.getValueDateField().setToolTipText("Entrez ici la date de valeur pour le compte d'arriv�e");
			toPane.setBorder(new TitledBorder(null, "to", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, null));
			toPane.addPropertyChangeListener(FromOrToPane.ACCOUNT_PROPERTY, listener);
			toPane.addPropertyChangeListener(FromOrToPane.VALUE_DATE_PROPERTY, listener);
			toPane.setDate(getDateField().getDate());
		}
		return toPane;
	}
	private JLabel getDateLabel() {
		if (dateLabel == null) {
			dateLabel = new JLabel(LocalizationData.get("TransactionDialog.date")); //$NON-NLS-1$
		}
		return dateLabel;
	}
	private DateWidget getDateField() {
		if (dateField == null) {
			dateField = new DateWidget();
			dateField.setToolTipText(LocalizationData.get("TransactionDialog.date.tooltip")); //$NON-NLS-1$
			dateField.setColumns(10);
			dateField.addPropertyChangeListener(DateWidget.DATE_PROPERTY, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					getFromPane().setDate(dateField.getDate());
					getToPane().setDate(dateField.getDate());
					updateOkDisabledCause();
				}
			});
			dateField.getDateField().addFocusListener(AutoSelectFocusListener.INSTANCE);
		}
		return dateField;
	}
	private JLabel getAmountLabel() {
		if (amountLabel == null) {
			amountLabel = new JLabel(LocalizationData.get("TransactionDialog.amount")); //$NON-NLS-1$
		}
		return amountLabel;
	}
	private CurrencyWidget getAmountField() {
		if (amountField == null) {
			amountField = new CurrencyWidget();
			amountField.setToolTipText("Enter the transfer amount here");
			amountField.setColumns(10);
			amountField.setValue(0.0);
			amountField.addPropertyChangeListener(CurrencyWidget.VALUE_PROPERTY, listener);
			amountField.addFocusListener(AutoSelectFocusListener.INSTANCE);
		}
		return amountField;
	}
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new GridLayout(0, 2, 0, 0));
			panel.add(getFromPane());
			panel.add(getToPane());
		}
		return panel;
	}
	private CategoryWidget getCategoryWidget() {
		if (categoryWidget == null) {
			categoryWidget = new CategoryWidget(data);
		}
		return categoryWidget;
	}
	
	public String getOkDisabledCause() {
		return okDisabledCause;
	}
	
	private void updateOkDisabledCause() {
		String old = okDisabledCause;
		okDisabledCause = null;
		if (getDateField().getDate()==null) {
			okDisabledCause = LocalizationData.get("TransactionDialog.bad.date"); //$NON-NLS-1$
		} else if (getAmountField().getValue() == null) {
			okDisabledCause = LocalizationData.get("TransactionDialog.bad.amount"); //$NON-NLS-1$
		} else if (GlobalData.AMOUNT_COMPARATOR.compare(getAmountField().getValue(),0.0)==0) {
			okDisabledCause = "The transfer's amount can't be null";
		} else if (getFromPane().getAccountWidget().get().equals(getToPane().getAccountWidget().get())) {
			okDisabledCause = "Both accounts are the same";
		} else if (getFromPane().getValueDateField().getDate()==null) {
			okDisabledCause = MessageFormat.format("The value date in the \"{0}\" panel is wrong", "from");
		} else if (getFromPane().getValueDateField().getDate()==null) {
			okDisabledCause = MessageFormat.format("The value date in the \"{0}\" panel is wrong", "to");
		}
		if (!NullUtils.areEquals(old, okDisabledCause)) {
			firePropertyChange(OK_DISABLED_CAUSE_PROPERTY, old, okDisabledCause);
		}
	}

	private SubtransactionListPanel getSubTransactionsPanel() {
		if (subTransactionsPanel == null) {
			subTransactionsPanel = new SubtransactionListPanel(data);
			subTransactionsPanel.addPropertyChangeListener(SubtransactionListPanel.SUM_PROPERTY, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if ((getAmountField().getValue() != null) && subTransactionsPanel.isAddToTransactionSelected()) {
						double diff = (Double) evt.getNewValue() - (Double) evt.getOldValue();
						double newValue = getAmountField().getValue() + diff;
						if (newValue < 0) {
							newValue = -newValue;
						}
						getAmountField().setValue(newValue);
					}
				}
			});
		}
		return subTransactionsPanel;
	}
}
