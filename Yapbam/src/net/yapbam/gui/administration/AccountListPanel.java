package net.yapbam.gui.administration;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.yapbam.data.Account;
import net.yapbam.data.AlertThreshold;
import net.yapbam.data.GlobalData;
import net.yapbam.data.event.AccountAddedEvent;
import net.yapbam.data.event.AccountPropertyChangedEvent;
import net.yapbam.data.event.AccountRemovedEvent;
import net.yapbam.data.event.CheckbookAddedEvent;
import net.yapbam.data.event.CheckbookRemovedEvent;
import net.yapbam.data.event.DataEvent;
import net.yapbam.data.event.DataListener;
import net.yapbam.data.event.EverythingChangedEvent;
import net.yapbam.data.event.ModeAddedEvent;
import net.yapbam.data.event.ModeRemovedEvent;
import net.yapbam.data.event.TransactionAddedEvent;
import net.yapbam.data.event.TransactionRemovedEvent;
import net.yapbam.gui.IconManager;
import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.actions.NewAccountAction;
import net.yapbam.gui.dialogs.AbstractDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.Object;
import java.text.MessageFormat;

public class AccountListPanel extends AbstractListAdministrationPanel {
	private static final long serialVersionUID = 1L;
	
	private static String[] alertKinds = new String[] {
		LocalizationData.get("AccountManager.alert.no"), //$NON-NLS-1$
		LocalizationData.get("AccountManager.alert.lessThan"), //$NON-NLS-1$
		LocalizationData.get("AccountManager.alert.greaterThan") //$NON-NLS-1$
	};

	@SuppressWarnings("serial")
	class DeleteAccountAction extends AbstractAction {
		private GlobalData data;
		DeleteAccountAction (GlobalData data) {
			super(LocalizationData.get("GenericButton.delete"), IconManager.DELETE); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, LocalizationData.get("AccountManager.deleteAccount.toolTip")); //$NON-NLS-1$
			this.data = data;
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedRow = getJTable().getSelectedRow();
			Account account = data.getAccount(selectedRow);
			boolean confirmed = true;
			int nb = account.getTransactionsNumber();
			if (nb!=0) {
				String mess = nb==1?LocalizationData.get("AccountManager.deleteMessage.one"): //$NON-NLS-1$
						MessageFormat.format("<HTML>"+LocalizationData.get("AccountManager.deleteMessage.more")+ //$NON-NLS-1$ //$NON-NLS-2$
						"<BR>"+LocalizationData.get("AccountManager.deleteMessage.confirm")+"</HTML>", nb); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Object[] options = {LocalizationData.get("GenericButton.ok"),LocalizationData.get("GenericButton.cancel")}; //$NON-NLS-1$ //$NON-NLS-2$
				int ok = JOptionPane.showOptionDialog(getJTable(), mess, LocalizationData.get("AccountManager.deleteMessage.title"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);		
				confirmed = (ok==0);
			}
			if (confirmed) {
				data.remove(account);
			}
		}
	}

	@SuppressWarnings("serial")
	@Override
	protected JTable instantiateJTable() {
		JTable jTable = new JTable(getTableModel()) {
		    //Implement table cell tool tips.
		    public String getToolTipText(MouseEvent e) {
		        String tip;
		        int column = convertColumnIndexToModel(columnAtPoint(e.getPoint()));
		        if (column == 0) {
		            tip = LocalizationData.get("AccountManager.nameColumn.toolTip"); //$NON-NLS-1$
		        } else if (column == 1) {
		            tip = LocalizationData.get("AccountManager.balanceColumn.toolTip"); //$NON-NLS-1$
		        } else if (column == 2) {
		        	tip = LocalizationData.get("AccountManager.alertKind.toolTip"); //$NON-NLS-1$
		        } else if (column == 3) {
		        	tip = LocalizationData.get("AccountManager.alertThreshold.toolTip"); //$NON-NLS-1$
		        } else if (column == 4) {
		            tip = LocalizationData.get("AccountManager.transactionsNumber.toolTip"); //$NON-NLS-1$
		        } else if (column == 5) {
		            tip = LocalizationData.get("AccountManager.modesNumber.toolTip"); //$NON-NLS-1$
		        } else if (column == 6) {
		            tip = LocalizationData.get("AccountManager.checkbooksNumber.toolTip"); //$NON-NLS-1$
		        } else { //another column
		            tip = super.getToolTipText(e);
		        }
		        return tip;
		    }
		};
		jTable.setDefaultRenderer(Double.class, new DefaultTableCellRenderer(){
		    @Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		    		boolean hasFocus, int row, int column) {
		    	super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			    setText(LocalizationData.getCurrencyInstance().format(value));
			    this.setHorizontalAlignment(SwingConstants.RIGHT);
		    	return this;
		    }
		});
		TableColumn sportColumn = jTable.getColumnModel().getColumn(2);
		JComboBox comboBox = new JComboBox();
		for (int i = 0; i < alertKinds.length; i++) {
			comboBox.addItem(alertKinds[i]);
		}
		sportColumn.setCellEditor(new DefaultCellEditor(comboBox));
		return jTable;
	}
	
	@SuppressWarnings("serial")
	private final class AccountTableModel extends AbstractTableModel implements DataListener {
		public AccountTableModel(GlobalData data) {
			data.addListener(this);
		}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if ((columnIndex==1) || (columnIndex==3)) return Double.class;
			else if ((columnIndex>=4) && (columnIndex<=6)) return Integer.class;
			return String.class;
		}
		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex==0) return LocalizationData.get("Transaction.account"); //$NON-NLS-1$
			if (columnIndex==1) return LocalizationData.get("AccountManager.balanceColumn.title"); //$NON-NLS-1$
			if (columnIndex==2) return LocalizationData.get("AccountManager.alertKind.title"); //$NON-NLS-1$
			if (columnIndex==3) return LocalizationData.get("AccountManager.alertThreshold.title"); //$NON-NLS-1$
			if (columnIndex==4) return LocalizationData.get("AccountManager.transactionsNumber.title"); //$NON-NLS-1$
			if (columnIndex==5) return LocalizationData.get("AccountManager.modesNumber.title"); //$NON-NLS-1$
			if (columnIndex==6) return LocalizationData.get("AccountManager.checkbooksNumber.title"); //$NON-NLS-1$
			return "?"; //$NON-NLS-1$
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Account account = ((GlobalData)data).getAccount(rowIndex);
			if (columnIndex==0) return account.getName();
			else if (columnIndex==1) return account.getInitialBalance();
			else if (columnIndex==2) return getWording(account.getAlertThreshold());
			else if (columnIndex==3) return account.getAlertThreshold().getBalance();
			else if (columnIndex==4) return account.getTransactionsNumber();
			else if (columnIndex==5) return account.getModesNumber()-1; // The undefined mode is returned in getModesNumber
			else if (columnIndex==6) return account.getCheckbooksNumber();
			return "?"; //$NON-NLS-1$
		}
		private String getWording(AlertThreshold alertThreshold) {
			if (alertThreshold.isLifeless()) return alertKinds[0];
			return alertThreshold.isLessThan()?alertKinds[1]:alertKinds[2];
		}
		public void setValueAt(Object value, int row, int col) {
			Account account = ((GlobalData)data).getAccount(row);
			if (col==0) { // Account name
				String name = ((String)value).trim();
				String errorMessage = null;
				if (name.length()==0) {
					errorMessage = LocalizationData.get("AccountManager.error.message.empty"); //$NON-NLS-1$
				} else {
					Account matchAccount = ((GlobalData)data).getAccount(name);
					if (matchAccount!=null) {
						if (matchAccount==account) return;
						errorMessage = MessageFormat.format(LocalizationData.get("AccountManager.error.message.alreadyUsed"), name); //$NON-NLS-1$
					}
				}
				if (errorMessage!=null) {
					JOptionPane.showMessageDialog(AbstractDialog.getOwnerWindow(AccountListPanel.this),
							errorMessage, LocalizationData.get("AccountManager.error.title"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
					fireTableRowsUpdated(row, row);
				} else {
					((GlobalData)data).setName(account, (String)value);
				}
			} else if (col==1) { // Initial Balance
				double val = (Double)value;
				((GlobalData)data).setInitialBalance(account, val);
			} else if (col==2) { // Alert kind
				if (value.equals(alertKinds[0])) {
					((GlobalData)data).setAlertThreshold(account, AlertThreshold.NO);
				} else {
					AlertThreshold old = account.getAlertThreshold();
					double amount = old.isLifeless()?0.0:old.getBalance();
					boolean less = alertKinds[1].equals(value);
					((GlobalData)data).setAlertThreshold(account, new AlertThreshold(amount, less));
				}
			} else if (col==3) { // Alert amount
				AlertThreshold old = account.getAlertThreshold();
				boolean less = old.isLifeless()?true:old.isLessThan();
				((GlobalData)data).setAlertThreshold(account, new AlertThreshold((Double) value, less));
			} else { //Unexpected
				throw new IllegalArgumentException();
			}
	    }

		@Override
		public int getRowCount() {
			return ((GlobalData)data).getAccountsNumber();
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex<4;
		}
		@Override
		public void processEvent(DataEvent event) {
			Account account = null;
			if (event instanceof EverythingChangedEvent) {
				this.fireTableDataChanged();
			} else if (event instanceof AccountAddedEvent) {
				int index = ((GlobalData)data).indexOf(((AccountAddedEvent)event).getAccount());
				this.fireTableRowsInserted(index, index);
			} else if (event instanceof AccountRemovedEvent) {
				int index = ((AccountRemovedEvent)event).getIndex();
				this.fireTableRowsDeleted(index, index);
			} else if (event instanceof AccountPropertyChangedEvent) {
				account = ((AccountPropertyChangedEvent)event).getAccount();
			} else if (event instanceof TransactionAddedEvent) {
				account = ((TransactionAddedEvent)event).getTransaction().getAccount();
			} else if (event instanceof TransactionRemovedEvent) {
				account = ((TransactionRemovedEvent)event).getRemoved().getAccount();
			} else if (event instanceof ModeAddedEvent) {
				account = ((ModeAddedEvent)event).getAccount();
			} else if (event instanceof ModeRemovedEvent) {
				account = ((ModeRemovedEvent)event).getAccount();
			} else if (event instanceof CheckbookAddedEvent) {
				account = ((CheckbookAddedEvent)event).getAccount();
			} else if (event instanceof CheckbookRemovedEvent) {
				account = ((CheckbookRemovedEvent)event).getAccount();
			}
			if (account!=null) {
				int row = ((GlobalData)data).indexOf(account);
				this.fireTableRowsUpdated(row, row);
			}
		}
	}
	
	public AccountListPanel() {
		this(null);
	}

	public AccountListPanel(GlobalData data) {
		super(data);
	}
	
	private TableModel getTableModel() {
		return new AccountTableModel((GlobalData) data);
	}
	protected Action getNewButtonAction() {
		return new NewAccountAction((GlobalData) data);
	}
	protected Action getEditButtonAction() {
		return null;
	}
	protected Action getDeleteButtonAction() {
		return new DeleteAccountAction((GlobalData) data);
	}
	protected Action getDuplicateButtonAction() {
		return null;
	}

	@Override
	public JTable getJTable() {
		return super.getJTable();
	}
}
