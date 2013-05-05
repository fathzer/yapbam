package net.yapbam.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.*;

import net.astesana.ajlib.swing.dialog.AbstractDialog;
import net.astesana.ajlib.swing.widget.ComboBox;
import net.astesana.ajlib.swing.widget.TextWidget;
import net.astesana.ajlib.utilities.NullUtils;

import net.yapbam.data.*;
import net.yapbam.date.helpers.DateStepper;
import net.yapbam.gui.IconManager;
import net.yapbam.gui.IconManager.Name;
import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.util.AutoUpdateOkButtonPropertyListener;
import net.yapbam.gui.widget.AutoSelectFocusListener;
import net.yapbam.gui.widget.CurrencyWidget;

/** This dialog allows to create or edit a transaction */
public abstract class AbstractTransactionDialog<V> extends AbstractDialog<FilteredData, V> {
	private static final long serialVersionUID = 1L;
	private static final boolean DEBUG = false;

	private AccountWidget accounts;
	protected TextWidget description;
	protected JTextField comment;
	protected CurrencyWidget amount;
	protected JCheckBox receipt;
	protected ComboBox modes;
	protected CategoryWidget categories;
	protected SubtransactionListPanel subtransactionsPanel;
	private String originalMode;
	private boolean originalIsExpense;
	private PredefinedDescriptionComputer pdc;
	
	protected AbstractTransactionDialog(Window owner, String title, FilteredData data, AbstractTransaction transaction) {
		super(owner, title, data); //$NON-NLS-1$
		pdc = null;
		if (transaction!=null) setContent(transaction);
	}
	
	protected void setPredefinedDescriptionComputer(PredefinedDescriptionComputer pdc) {
		this.pdc = pdc;
		if (pdc!=null) description.setPredefined(pdc.getPredefined(), pdc.getUnsortedSize());
	}

	protected void setContent(AbstractTransaction transaction) {
		Account account = transaction.getAccount();
		accounts.set(account);
		description.setText(transaction.getDescription());
		comment.setText(transaction.getComment());
		subtransactionsPanel.fill(transaction);
		// Danger, subtransaction.fill throws Property Change events that may alter the amount field content.
		// So, always set up the amountField after the subtransactions list.
		amount.setValue(Math.abs(transaction.getAmount()));
		receipt.setSelected(transaction.getAmount()>0);
		// Be aware, as its listener change the selectedMode, receipt must always be set before mode.
		originalMode = transaction.getMode().getName();
		originalIsExpense = transaction.getAmount()<0;
		if (!modes.contains(originalMode) && (account.getMode(originalMode)!=null)) {
			// It is possible that the mode of the transaction is no more available for this kind of transaction.
			// For instance, if this account had a payment mode (for example check), that was usable for expenses,
			// but that is, now, no more usable.
			// In order to allow the user to not change this original payment mode, will we add it to the available
			// payment modes.
			boolean old = modes.isActionEnabled();
			modes.setActionEnabled(false);
			modes.addItem(originalMode);
			modes.setActionEnabled(old);
		}
		modes.setSelectedItem(transaction.getMode().getName());
		categories.set(transaction.getCategory());
	}

	protected void setMode(Mode mode) {
		Account account = getAccount();
		int index = account.findMode(mode);
		if (index>=0) {
			modes.setSelectedItem(mode.getName()); // If the mode isn't available for this account, do nothing.
		}
	}
	
	/** Gets the currently selected account.
	 * @return an account.
	 */
	protected Account getAccount() {
		return accounts.get();
	}

	/** Gets the currently selected amount.
	 * @return a double, positive if the transaction is a receipt, negative if not.
	 */
	protected double getAmount() {
		double amount = this.amount.getValue()!=null?Math.abs(this.amount.getValue()):0.0;
		// Beware of zero value, a zero expense should be considered as a receipt,
		// because expenses and receipts have not the same modes available.
		// We will transform zero value into very, very, very small non zero values.
		if (amount==0) {
			amount=Double.MIN_VALUE;
		}
		if (!this.receipt.isSelected()) amount = -amount;
		return amount;
	}
	
	/** Returns whether the currently edited transaction is an expense. 
	 * @return true for an expense, false for a receipt
	 */
	protected boolean isExpense() {
		return getAmount()<0;
	}

/**/	private JPanel combine (JComboBox box, JButton button) {
        JPanel pane = new JPanel(new BorderLayout());
        Dimension dimension = box.getPreferredSize();
        button.setPreferredSize(new Dimension(dimension.height, dimension.height));
        pane.add(box, BorderLayout.CENTER);
        pane.add(button, BorderLayout.EAST);
        return pane;
	}/**/ //TODO remove
	
	@Override
	protected JPanel createCenterPane() {
		// Create the content pane.
		JPanel centerPane = new JPanel(new GridBagLayout());

		// Account
		Insets insets = new Insets(5,5,5,5);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = insets; c.gridx=0; c.gridy=0; c.anchor=GridBagConstraints.WEST;
		c.gridwidth=GridBagConstraints.REMAINDER; c.fill = GridBagConstraints.HORIZONTAL; c.weightx=1.0;
		accounts = new AccountWidget(data.getGlobalData());
		List<Account> filterAccounts = data.getFilter().getValidAccounts();
		if ((filterAccounts!=null) && (filterAccounts.size()==1)) { // If the filter defines only one account, select this account
			accounts.set(filterAccounts.get(0));
		}
		AccountsListener accountListener = new AccountsListener();
		accounts.addPropertyChangeListener(accountListener);
		accounts.setToolTipText(LocalizationData.get("TransactionDialog.account.tooltip")); //$NON-NLS-1$
		JButton newAccount = new JButton(IconManager.get(Name.NEW_ACCOUNT));
		newAccount.setFocusable(false);
		newAccount.setToolTipText(LocalizationData.get("TransactionDialog.account.new.tooltip")); //$NON-NLS-1$
		centerPane.add(accounts, c);

		// Description
		JLabel titleLibelle = new JLabel(LocalizationData.get("TransactionDialog.description")); //$NON-NLS-1$
		c = new GridBagConstraints();
		c.insets = insets; c.gridx=0; c.gridy=1; c.anchor = GridBagConstraints.WEST; c.weightx=1.0;
		centerPane.add(titleLibelle, c);
		JPanel panel = new JPanel(new GridBagLayout());
		c.gridx=1; c.gridwidth=GridBagConstraints.REMAINDER; c.fill = GridBagConstraints.HORIZONTAL;
		centerPane.add(panel, c);
		c = new GridBagConstraints();
		description = new TextWidget();
		description.setToolTipText(LocalizationData.get("TransactionDialog.description.tooltip")); //$NON-NLS-1$
		description.addPropertyChangeListener(TextWidget.PREDEFINED_VALUE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue()!=null) predefinedDescriptionSelected((String) evt.getNewValue());
			}
		});
		description.addFocusListener(AutoSelectFocusListener.INSTANCE);
		c = new GridBagConstraints(); c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1.0;
		panel.add(description, c);
		
		// Comment
		comment = new JTextField();
		comment.setToolTipText(LocalizationData.get("TransactionDialog.comment.tooltip")); //$NON-NLS-1$
		c.gridx=1;;
		panel.add(comment, c);

		// Next line
		c = new GridBagConstraints();
		c.insets = insets; c.gridx=0; c.gridy=2; c.anchor = GridBagConstraints.WEST;
		buildDateField(centerPane, AutoSelectFocusListener.INSTANCE, c); // Subclasses may insert a date field here

		c.fill=GridBagConstraints.NONE; c.anchor = GridBagConstraints.WEST; c.weightx = 0;
		centerPane.add(new JLabel(LocalizationData.get("TransactionDialog.amount")), c); //$NON-NLS-1$
		amount = new CurrencyWidget(LocalizationData.getLocale());
		amount.setColumns(10);
		amount.addFocusListener(AutoSelectFocusListener.INSTANCE);
		amount.addPropertyChangeListener(CurrencyWidget.VALUE_PROPERTY, new AutoUpdateOkButtonPropertyListener(this));
		amount.setToolTipText(LocalizationData.get("TransactionDialog.amount.tooltip")); //$NON-NLS-1$
		c.gridx++; c.weightx = 1.0;c.fill = GridBagConstraints.HORIZONTAL;
		centerPane.add(amount, c);
		
		receipt = new JCheckBox(LocalizationData.get("TransactionDialog.receipt")); //$NON-NLS-1$
		receipt.setToolTipText(LocalizationData.get("TransactionDialog.receipt.tooltip")); //$NON-NLS-1$
		receipt.addItemListener(new ReceiptListener());
		c.gridx++; c.weightx=1.0; c.anchor = GridBagConstraints.WEST; c.gridwidth = GridBagConstraints.REMAINDER;
		centerPane.add(receipt, c);

		// Next line
		c = new GridBagConstraints();
		c.insets = insets; c.gridx = 0; c.gridy = 3; c.anchor = GridBagConstraints.WEST;
		centerPane.add(new JLabel(LocalizationData.get("TransactionDialog.mode")), c); //$NON-NLS-1$
		modes = new ComboBox();
		buildModes(!receipt.isSelected());
		ModesListener modeListener = new ModesListener();
		modes.addActionListener(modeListener);
		modes.setToolTipText(LocalizationData.get("TransactionDialog.mode.tooltip")); //$NON-NLS-1$
		c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
		JButton newMode = new JButton(IconManager.get(Name.NEW_MODE));
		newMode.setFocusable(false);
		newMode.addActionListener(modeListener);
		newMode.setToolTipText(LocalizationData.get("TransactionDialog.mode.new.tooltip")); //$NON-NLS-1$
		centerPane.add(combine(modes, newMode), c);
		
		c.gridx = 2;
		buildNumberField(centerPane, AutoSelectFocusListener.INSTANCE, c); // Subclasses may insert a number field here

		categories = new CategoryWidget(this.data.getGlobalData());
		c.gridx++; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
		centerPane.add(categories, c);

		// Next line
		c = new GridBagConstraints();
		c.insets=insets; c.gridx=0; c.gridy=5; c.anchor = GridBagConstraints.WEST;
		buildStatementFields(centerPane, AutoSelectFocusListener.INSTANCE, c);

		// Next Line
		c.gridx=0; c.gridy++; c.gridwidth=GridBagConstraints.REMAINDER; c.fill = GridBagConstraints.HORIZONTAL;
		centerPane.add(new JSeparator(JSeparator.HORIZONTAL), c);

		c.insets = insets; c.gridx=0; c.gridy++; c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1; c.fill=GridBagConstraints.BOTH; c.weightx = 1.0; c.weighty = 1.0;
		subtransactionsPanel = new SubtransactionListPanel(this.data.getGlobalData());
		subtransactionsPanel.addPropertyChangeListener(SubtransactionListPanel.SUM_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ((amount.getValue() != null) && subtransactionsPanel.isAddToTransactionSelected()) {
					double diff = (Double) evt.getNewValue() - (Double) evt.getOldValue();
					if (isExpense()) diff = -diff;
					double newValue = amount.getValue() + diff;
					if (newValue < 0) {
						newValue = -newValue;
						receipt.setSelected(!receipt.isSelected());
					}
					amount.setValue(newValue);
				}
			}
		});
		centerPane.add(subtransactionsPanel, c);

		amount.setValue(0.0);
		return centerPane;
	}

	protected void predefinedDescriptionSelected(String description) {
	}

	protected abstract void buildStatementFields(JPanel centerPane, FocusListener focusListener, GridBagConstraints c);

	protected abstract void buildNumberField(JPanel centerPane, FocusListener focusListener, GridBagConstraints c);

	protected abstract void buildDateField(JPanel centerPane, FocusListener focusListener, GridBagConstraints c);

	/**
	 * Refreshes the mode list according to the current account.
	 * <BR>If a mode with the same name that the currently
	 * selected mode is available, it will be selected. Else, the first mode of
	 * the list will be selected.
	 */
	private void buildModes(boolean expense) {
		// Prevents selection events to be sent
		modes.setActionEnabled(false);
		String current = (String) modes.getSelectedItem();
		modes.removeAllItems();
		Account currentAccount = getAccount();
		int nb = currentAccount.getModesNumber();
		for (int i = 0; i < nb; i++) {
			Mode mode = currentAccount.getMode(i);
			if ((expense?mode.getExpenseVdc():mode.getReceiptVdc())!=null) {
				modes.addItem(mode.getName());
			}
		}
		if ((originalMode!=null) && (originalIsExpense==expense) && !modes.contains(originalMode) && (currentAccount.getMode(originalMode)!=null)) {
			modes.addItem(originalMode);
		}
		// Clears the selection in order future selection to fire a selection change
		// event ... even if the same value is selected (as selectedMode and value date may be
		// changed)
		modes.setSelectedItem(null);
		modes.setActionEnabled(true);
		// Restore the previously selected mode, if it is still available
		if ((current != null) && (modes.contains(current))) {
			modes.setSelectedItem(current);
		} else {
			modes.setSelectedIndex(0);
		}
	}

	public AbstractTransaction getTransaction() {
		return (Transaction) super.getResult();
	}

	private Mode displayNewModeDialog() {
		Mode mode = ModeDialog.open(data.getGlobalData(), getAccount(), this);
		if (mode == null) return null;
		DateStepper vdc = isExpense() ? mode.getExpenseVdc() : mode.getReceiptVdc();
		return (vdc != null) ? mode : null;
	}

	protected Mode getCurrentMode() {
		return getAccount().getMode((String)modes.getSelectedItem());
	}

	class AccountsListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (DEBUG) System.out.println("Account " + getAccount() + " is selected"); //$NON-NLS-1$ //$NON-NLS-2$
			buildModes(isExpense());
			if (pdc!=null) description.setPredefined(pdc.getPredefined(), pdc.getUnsortedSize());
		}
	}

	class ReceiptListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			buildModes(e.getStateChange() == ItemEvent.DESELECTED);
		}
	}

	class ModesListener implements ActionListener {
		private Object lastSelected = null;
		private boolean lastWasExpense = true;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == modes) {
				Object selected = modes.getSelectedItem();
				if (!NullUtils.areEquals(lastSelected, selected) || (isExpense()!=lastWasExpense)) {
					lastSelected = selected;
					lastWasExpense = isExpense();
					if (DEBUG) System.out.println("Mode " + lastSelected + " is selected"); //$NON-NLS-1$ //$NON-NLS-2$
					optionnalUpdatesOnModeChange();
				}
			} else {
				// New mode required
				Mode m = displayNewModeDialog();
				if (m != null) {
					modes.addItem(m.getName());
					modes.setSelectedIndex(modes.getItemCount() - 1);
					pack();
				}
			}
		}
	}

	protected void optionnalUpdatesOnModeChange() {
	}

	@Override
	protected String getOkDisabledCause() {
		if (this.amount.getValue() == null) return LocalizationData.get("TransactionDialog.bad.amount"); //$NON-NLS-1$
		return null;
	}
	
	@Override
	public void setVisible(boolean visible) {
		String prefix = this.getClass().getCanonicalName();
		int max = Preferences.MAX_KEY_LENGTH-15;
		if (prefix.length()>max) {
			prefix = prefix.substring(prefix.length()-max);
		}
		if (visible) {
			subtransactionsPanel.restoreState(prefix);
		} else {
			subtransactionsPanel.saveState(prefix);
		}
		super.setVisible(visible);
	}
}
