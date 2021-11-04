package net.yapbam.gui.dialogs.checkbook;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import com.fathzer.jlocal.Formatter;
import com.fathzer.soft.ajlib.swing.Utils;
import com.fathzer.soft.ajlib.swing.table.RowSorter;

import net.yapbam.data.Account;
import net.yapbam.data.Checkbook;
import net.yapbam.data.GlobalData;
import net.yapbam.data.event.CheckbookPropertyChangedEvent;
import net.yapbam.data.event.DataEvent;
import net.yapbam.data.event.DataListener;
import net.yapbam.gui.IconManager;
import net.yapbam.gui.IconManager.Name;
import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.administration.AbstractAlertPanel;
import net.yapbam.gui.administration.AbstractListAdministrationPanel;

@SuppressWarnings("serial")
public class CheckbookListPanel extends AbstractListAdministrationPanel<GlobalData> implements AbstractAlertPanel {

	private Account account;

	private JButton alertButton;

	public CheckbookListPanel(GlobalData data) {
		super(data);
		this.data.addListener(new DataListener() {
			@Override
			public void processEvent(DataEvent event) {
				((AbstractTableModel)getJTable().getModel()).fireTableDataChanged(); //TODO We should test the event class and ignore some events (ie : ModeAddedEvent) 
				
				updateStateOfAlertButton();
				
				if (event instanceof CheckbookPropertyChangedEvent) { // Test if a checkbook is finished
					CheckbookPropertyChangedEvent checkbookChangedEvt = (CheckbookPropertyChangedEvent)event;
					Checkbook old = checkbookChangedEvt.getOldCheckbook();
					if (!old.isEmpty() && (checkbookChangedEvt.getNewCheckbook().isEmpty())) {
						// A checkbook just finished, ask what to do
						String message = Formatter.format(LocalizationData.get("checkbookDialog.finished.message"),old.getFullNumber(old.getFirst()),old.getFullNumber(old.getLast())); //$NON-NLS-1$
						String[] options = new String[]{LocalizationData.get("GenericButton.ignore"),LocalizationData.get("checkbookDialog.finished.delete"),LocalizationData.get("checkbookDialog.finished.createNew"),LocalizationData.get("checkbookDialog.finished.DeleteAndCreateNew")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						int choice = JOptionPane.showOptionDialog(Utils.getOwnerWindow(CheckbookListPanel.this), message, LocalizationData.get("checkbookDialog.finished.title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[3]);  //$NON-NLS-1$
						if ((choice==1) || (choice==3)) {
							CheckbookListPanel.this.data.remove(checkbookChangedEvt.getAccount(), checkbookChangedEvt.getNewCheckbook());
						}
						if (choice > 1) {
							createBook(Utils.getOwnerWindow(CheckbookListPanel.this), checkbookChangedEvt.getAccount());
						}
					}
				}
			}
		});
		this.account = null;
		getJTable().setPreferredScrollableViewportSize(new Dimension(1, getJTable().getRowHeight() * 6));
		getJTable().setRowSorter(new RowSorter<TableModel>(getJTable().getModel()));
		getJTable().getColumnModel().getColumn(0).setCellRenderer(new AlertCellRender());
	}
		
	public void setContent(Account account) {
		this.account = account;
		this.getNewButton().getAction().setEnabled(account!=null);
		((AbstractTableModel)getJTable().getModel()).fireTableDataChanged();
		updateStateOfAlertButton();
	}
	
	private void updateStateOfAlertButton() {
		getAlertButton().setEnabled(account != null && account.getCheckbooksNumber() > 0);
	}

	@Override
	protected Action getDeleteButtonAction() {
		return new DeleteBookAction();
	}

	@Override
	protected Action getEditButtonAction() {
		return new EditBookAction();
	}

	@Override
	protected Action getNewButtonAction() {
		return new NewCheckbookAction();
	}

	private final class NewCheckbookAction extends AbstractAction {
		public NewCheckbookAction() {
			super(LocalizationData.get("GenericButton.new"), IconManager.get(Name.NEW)); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, LocalizationData.get("checkbookDialog.New.tooltip")); //$NON-NLS-1$
			setEnabled(false);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			createBook(Utils.getOwnerWindow((Component)e.getSource()), account);
		}
	}
	private final class EditBookAction extends AbstractAction {
		public EditBookAction() {
			super(LocalizationData.get("GenericButton.edit"), IconManager.get(Name.EDIT)); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, LocalizationData.get("checkbookDialog.Edit.tooltip")); //$NON-NLS-1$
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			int row = getJTable().convertRowIndexToModel(getJTable().getSelectedRow());
			Checkbook old = account.getCheckbook(row);
			CheckbookDialog dialog = new CheckbookDialog(Utils.getOwnerWindow((Component)e.getSource()));
			dialog.setContent(old);
			dialog.setVisible(true);
			Checkbook checkbook = dialog.getResult();
			if (checkbook!=null) {
				((GlobalData)data).setCheckbook(account, old, checkbook);
			}
		}
	}
	private final class DeleteBookAction extends AbstractAction {			
		public DeleteBookAction() {
			super(LocalizationData.get("GenericButton.delete"), IconManager.get(Name.DELETE)); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, LocalizationData.get("checkbookDialog.Delete.tooltip")); //$NON-NLS-1$
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int row = getJTable().convertRowIndexToModel(getJTable().getSelectedRow());
			data.remove(account, account.getCheckbook(row));
		}
	}
	
	@Override
	protected JTable instantiateJTable() {
		return new com.fathzer.soft.ajlib.swing.table.JTable(getTableModel());
	}
	
	protected TableModel getTableModel() {
		return new AbstractCheckbookListModel() {		
			@Override
			public int getRowCount() {
				if (account==null) {
					return 0;
				}
				return account.getCheckbooksNumber();
			}
			
			@Override
			protected Checkbook getCheckBook(int rowIndex) {
				return account.getCheckbook(rowIndex);
			}
		};
	}

	protected JButton getAlertButton() {
		if (this.alertButton == null) {
			final AbstractAction preferencesAction = new AbstractAction(LocalizationData.get("checkbookAlertsWidget.openPreferencesDialog")) {
				@Override
				public void actionPerformed(ActionEvent e) {
					CheckbookAlertsPreferencesDialog.open(data, account,
							Utils.getOwnerWindow((Component) e.getSource()));
				}
			};
			preferencesAction.putValue(Action.SHORT_DESCRIPTION, LocalizationData.get("checkbookAlertsWidget.openPreferencesDialog.tooltip"));
			this.alertButton = new JButton(preferencesAction);
			this.alertButton.setIcon(IconManager.get(Name.SETTINGS));
			this.alertButton.setEnabled(false);
			this.alertButton.setPreferredSize(new Dimension(240, 30));
		}
		return this.alertButton;
	}

	@Override
	protected Action getDuplicateButtonAction() {
		return null;
	}

	@Override
	protected Component getTopComponent() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
		topPanel.add(getAlertButton(), BorderLayout.EAST);
		return topPanel;
	}

	private void createBook(Window owner, Account account) {
		CheckbookDialog dialog = new CheckbookDialog(owner);
		dialog.setVisible(true);
		Checkbook book = dialog.getResult();
		if (book!=null) {
			data.add(account, book);
		}
	}

	@Override
	public boolean hasAlert() {
		boolean result = false;
		if (account != null) {
			for (int i = 0; i < account.getCheckbooksNumber() && !result; i++) {
				if (account.getCheckbook(i).getRemaining() > 0 && //
						account.getCheckbook(i).getRemaining() <= account.getCheckNumberAlertThreshold()) {
					result = true;
				}
			}
		}
		return result;
	}
	
	class AlertCellRender extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (account.getCheckbook(row).getRemaining() > 0 && //
					account.getCheckbook(row).getRemaining() <= account.getCheckNumberAlertThreshold()) {
				this.setIcon(IconManager.get(Name.ALERT));
			} else {
				this.setIcon(null);
			}
			return this;
		}

	}

}
