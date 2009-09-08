package net.yapbam.ihm.administration;

import javax.swing.Action;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.yapbam.data.Category;
import net.yapbam.data.GlobalData;
import net.yapbam.data.Mode;
import net.yapbam.data.PeriodicalTransaction;
import net.yapbam.ihm.LocalizationData;
import net.yapbam.ihm.actions.DeletePeriodicalTransactionAction;
import net.yapbam.ihm.actions.EditPeriodicalTransactionAction;
import net.yapbam.ihm.actions.NewPeriodicalTransactionAction;
import net.yapbam.ihm.transactiontable.SpreadState;

import java.lang.Object;

public class PeriodicTransactionListPanel extends AbstractListAdministrationPanel { //LOCAL
	private static final long serialVersionUID = 1L;

	public PeriodicTransactionListPanel(Object data) {
		super(data);
	}
	
	public String getPanelToolTip() {
		return "Cet onglet permet de g�rer les op�rations p�riodiques";
	}
	
	@SuppressWarnings("serial")
	protected TableModel getTableModel() {
		return new AbstractTableModel() {
			@Override
			public String getColumnName(int columnIndex) {
				if (columnIndex==0) return LocalizationData.get("Transaction.0"); //$NON-NLS-1$
				if (columnIndex==1) return LocalizationData.get("Transaction.account"); //$NON-NLS-1$
				if (columnIndex==2) return LocalizationData.get("Transaction.description"); //$NON-NLS-1$
				if (columnIndex==3) return LocalizationData.get("Transaction.amount"); //$NON-NLS-1$
				if (columnIndex==4) return LocalizationData.get("Transaction.category"); //$NON-NLS-1$
				if (columnIndex==5) return LocalizationData.get("Transaction.mode"); //$NON-NLS-1$
				if (columnIndex==6) return "Prochaine �ch�ance";
				return "?"; //$NON-NLS-1$
			}
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				boolean spread = getJTable().getRowHeight()!=getJTable().getRowHeight(rowIndex);
				PeriodicalTransaction transaction = ((GlobalData)data).getPeriodicalTransaction(rowIndex);
				if (columnIndex==0) return new SpreadState(transaction.getSubTransactionSize()!=0, spread);
				else if (columnIndex==1) return transaction.getAccount().getName();
				else if (columnIndex==2) {
					if (spread) {
						StringBuffer buf = new StringBuffer("<html><body>").append(transaction.getDescription()); //$NON-NLS-1$
						for (int i = 0; i < transaction.getSubTransactionSize(); i++) {
							buf.append("<BR>&nbsp;&nbsp;").append(transaction.getSubTransaction(i).getDescription()); //$NON-NLS-1$
						}
						if (transaction.getComplement()!=0) {
							buf.append("<BR>&nbsp;&nbsp;").append(LocalizationData.get("Transaction.14")); //$NON-NLS-1$ //$NON-NLS-2$
						}
						buf.append("</body></html>"); //$NON-NLS-1$
						return buf.toString();
					} else {
						return transaction.getDescription();
					}
				} else if (columnIndex==3) {
					if (spread) {
						double complement = transaction.getComplement();
						int numberOfLines = transaction.getSubTransactionSize()+1;
						if (complement!=0) numberOfLines++;
						double[] result = new double[numberOfLines];
						result[0] = transaction.getAmount();
						for (int i = 0; i < transaction.getSubTransactionSize(); i++) {
							result[i+1] = transaction.getSubTransaction(i).getAmount();
						}
						if (complement!=0) result[result.length-1] = complement;
						return result;
					} else {
						return new double[]{transaction.getAmount()};
					}
				} else if (columnIndex==4) {
					if (spread) {
						StringBuffer buf = new StringBuffer("<html><body>").append(getName(transaction.getCategory())); //$NON-NLS-1$
						for (int i = 0; i < transaction.getSubTransactionSize(); i++) {
							buf.append("<BR>&nbsp;&nbsp;").append(getName(transaction.getSubTransaction(i).getCategory())); //$NON-NLS-1$
						}
						if (transaction.getComplement()!=0) {
							buf.append("<BR>&nbsp;&nbsp;").append(getName(transaction.getCategory())); //$NON-NLS-1$
						}
						buf.append("</body></html>"); //$NON-NLS-1$
						return buf.toString();
					} else {
						return getName(transaction.getCategory());
					}
				} else if (columnIndex==5) {
					Mode mode = transaction.getMode();
					return mode.equals(Mode.UNDEFINED) ? "" : mode.getName(); //$NON-NLS-1$
				} else if (columnIndex==6) return transaction.getNextDate();
				return "?";
			}
			@Override
			public int getRowCount() {
				return ((GlobalData)data).getPeriodicalTransactionsNumber();
			}
			@Override
			public int getColumnCount() {
				return 7;
			}
			private Object getName(Category category) {
				return category.equals(Category.UNDEFINED) ? "" : category.getName(); //$NON-NLS-1$
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};
	}
	protected Action getNewButtonAction() {
		return new NewPeriodicalTransactionAction();
	}
	protected Action getEditButtonAction() {
		return new EditPeriodicalTransactionAction();
	}
	protected Action getDeleteButtonAction() {
		return new DeletePeriodicalTransactionAction();
	}
}
