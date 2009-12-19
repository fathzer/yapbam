package net.yapbam.gui;

import java.awt.event.*;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import net.yapbam.data.Account;
import net.yapbam.data.FilteredData;
import net.yapbam.data.GlobalData;
import net.yapbam.data.event.AccountAddedEvent;
import net.yapbam.data.event.AccountPropertyChangedEvent;
import net.yapbam.data.event.AccountRemovedEvent;
import net.yapbam.data.event.DataEvent;
import net.yapbam.data.event.DataListener;
import net.yapbam.data.event.EverythingChangedEvent;
import net.yapbam.data.event.NeedToBeSavedChangedEvent;
import net.yapbam.gui.actions.*;
import net.yapbam.gui.dialogs.AboutDialog;
import net.yapbam.gui.dialogs.AccountDialog;
import net.yapbam.gui.transactiontable.GeneratePeriodicalTransactionsAction;

public class MainMenuBar extends JMenuBar implements ActionListener, DataListener {
	private static final long serialVersionUID = 1L;

	private MainFrame frame;

	private JMenuItem menuItemNew;
    private JMenuItem menuItemOpen;
    private JMenuItem menuItemSave;
    private JMenuItem menuItemSaveAs;
    private JMenuItem menuItemQuit;

	private Action editPreferences;
    
	private JMenuItem menuItemAbout;
	
	private JMenu accountMenu;
	private JMenu filterMenu;
	private JMenu transactionMenu;

    MainMenuBar (MainFrame frame) {
        super();

        this.frame = frame;
        
        //Build the file menu.
        JMenu menu = new JMenu(LocalizationData.get("MainMenu.File")); //$NON-NLS-1$
        menu.setMnemonic(LocalizationData.getChar("MainMenu.File.Mnemonic")); //$NON-NLS-1$
        menu.setToolTipText(LocalizationData.get("MainMenu.File.ToolTip")); //$NON-NLS-1$
        this.add(menu);

        this.menuItemNew = new JMenuItem(LocalizationData.get("MainMenu.NewFile"), LocalizationData.getChar("MainMenu.NewFile.Mnemonic")); //$NON-NLS-1$ //$NON-NLS-2$
        this.menuItemNew.setIcon(IconManager.NEW_FILE);
        this.menuItemNew.setToolTipText(LocalizationData.get("MainMenu.NewFile.ToolTip")); //$NON-NLS-1$
        this.menuItemNew.addActionListener(this);
        menu.add(this.menuItemNew);
		this.menuItemOpen = new JMenuItem(LocalizationData.get("MainMenu.Open"), LocalizationData.getChar("MainMenu.Open.Mnemonic")); //$NON-NLS-1$ //$NON-NLS-2$
        this.menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(LocalizationData.getChar("MainMenu.Open.Accelerator"), ActionEvent.CTRL_MASK)); //$NON-NLS-1$
        this.menuItemOpen.setToolTipText(LocalizationData.get("MainMenu.Open.ToolTip")); //$NON-NLS-1$
        this.menuItemOpen.addActionListener(this);
        menu.add(this.menuItemOpen);
        this.menuItemSave = new JMenuItem(LocalizationData.get("MainMenu.Save"), IconManager.SAVE); //$NON-NLS-1$
        this.menuItemSave.setAccelerator(KeyStroke.getKeyStroke(LocalizationData.getChar("MainMenu.Save.Accelerator"), ActionEvent.CTRL_MASK)); //$NON-NLS-1$
        this.menuItemSave.setMnemonic(LocalizationData.getChar("MainMenu.Save.Mnemonic")); //$NON-NLS-1$
        this.menuItemSave.setToolTipText(LocalizationData.get("MainMenu.Save.ToolTip")); //$NON-NLS-1$
        this.menuItemSave.addActionListener(this);
        this.menuItemSave.setEnabled(frame.getData().somethingHasChanged());
        menu.add(this.menuItemSave);
        this.menuItemSaveAs = new JMenuItem(LocalizationData.get("MainMenu.SaveAs"), IconManager.SAVE_AS); //$NON-NLS-1$
        this.menuItemSaveAs.setMnemonic(LocalizationData.getChar("MainMenu.SaveAs.Mnemonic")); //$NON-NLS-1$
        this.menuItemSaveAs.setToolTipText(LocalizationData.get("MainMenu.SaveAs.ToolTip")); //$NON-NLS-1$
        this.menuItemSaveAs.addActionListener(this);
        this.menuItemSaveAs.setEnabled(!frame.getData().isEmpty());
        menu.add(this.menuItemSaveAs);
        insertPluginMenuItems(menu, AbstractPlugIn.FILE_MANIPULATION_PART);

        menu.addSeparator();
        editPreferences = new EditPreferenceAction(frame);
        menu.add(editPreferences);
        insertPluginMenuItems(menu, AbstractPlugIn.PREFERENCES_PART);

        menu.addSeparator();
        this.menuItemQuit = new JMenuItem(LocalizationData.get("MainMenu.Quit")); //$NON-NLS-1$
        this.menuItemQuit.setAccelerator(KeyStroke.getKeyStroke(LocalizationData.getChar("MainMenu.Quit.Accelerator"), ActionEvent.CTRL_MASK)); //$NON-NLS-1$
        this.menuItemQuit.setMnemonic(LocalizationData.getChar("MainMenu.Quit.Mnemonic")); //$NON-NLS-1$
        this.menuItemQuit.setToolTipText(LocalizationData.get("MainMenu.Quit.ToolTip")); //$NON-NLS-1$
        this.menuItemQuit.addActionListener(this);
        menu.add(this.menuItemQuit);

        accountMenu = new JMenu(LocalizationData.get("MainMenu.Accounts")); //$NON-NLS-1$
        accountMenu.setMnemonic(LocalizationData.getChar("MainMenu.Accounts.Mnemonic")); //$NON-NLS-1$
        accountMenu.setToolTipText(LocalizationData.get("MainMenu.Accounts.ToolTip")); //$NON-NLS-1$
        updateAccountMenu();
        this.add(accountMenu);
        
        transactionMenu = new JMenu(LocalizationData.get("MainMenu.Transactions"));
        transactionMenu.setMnemonic(LocalizationData.getChar("MainMenu.Transactions.Mnemonic")); //$NON-NLS-1$
        transactionMenu.setToolTipText(LocalizationData.get("MainMenu.Transactions.ToolTip")); //$NON-NLS-1$
        JMenuItem item = new JMenuItem(new NewTransactionAction(frame.getData()));
        item.setAccelerator(KeyStroke.getKeyStroke(LocalizationData.getChar("MainMenu.Transactions.New.Accelerator"), ActionEvent.CTRL_MASK)); //$NON-NLS-1$
        transactionMenu.add(item);
        insertPluginMenuItems(transactionMenu, AbstractPlugIn.TRANSACTIONS_PART);
        transactionMenu.addSeparator();
        transactionMenu.add(new JMenuItem(new GeneratePeriodicalTransactionsAction(frame.getData())));
        insertPluginMenuItems(transactionMenu, AbstractPlugIn.PERIODIC_TRANSACTIONS_PART);
        this.add(transactionMenu);
        
        //Build the filter menu
        filterMenu = new JMenu(LocalizationData.get("MainMenuBar.Filter")); //$NON-NLS-1$
        filterMenu.setToolTipText(LocalizationData.get("MainMenuBar.Filter.Tooltip")); //$NON-NLS-1$
        filterMenu.setMnemonic(LocalizationData.getChar("MainMenuBar.Filter.Mnemonic")); //$NON-NLS-1$
        updateFilterMenu();
        this.add(filterMenu);

        //Build plugins menus
        for (int i = 0; i < this.frame.getPlugInsNumber(); i++) {
            JMenu[] menus = this.frame.getPlugIn(i).getPlugInMenu(); //TODO What if a plugin just wants to add a menuItem to an existing Menu ?
    		if (menus!=null) {
    			for (int j = 0; j < menus.length; j++) {
					this.add(menus[j]);
				}
    		}
		}
        
        //Build Help menu.
        menu = new JMenu(LocalizationData.get("MainMenu.QuestionMark")); //$NON-NLS-1$
        menu.setMnemonic(LocalizationData.getChar("MainMenu.QuestionMark.Mnemonic")); //$NON-NLS-1$
        menu.setToolTipText(LocalizationData.get("MainMenu.QuestionMark.ToolTip")); //$NON-NLS-1$
        this.add(menu);

        this.menuItemAbout = new JMenuItem(LocalizationData.get("MainMenu.About")); //$NON-NLS-1$
        this.menuItemAbout.setMnemonic(LocalizationData.getChar("MainMenu.About.Mnemonic")); //$NON-NLS-1$
        this.menuItemAbout.setToolTipText(LocalizationData.get("MainMenu.About.ToolTip")); //$NON-NLS-1$
        this.menuItemAbout.addActionListener(this);
        menu.add(this.menuItemAbout);
        insertPluginMenuItems(menu, AbstractPlugIn.ABOUT_PART);
        menu.addSeparator();
        menu.add(new CheckNewReleaseAction(this.frame));
        insertPluginMenuItems(menu, AbstractPlugIn.UPDATES_PART);
    }

	private void insertPluginMenuItems(JMenu menu, int part) {
		for (int i = 0; i < this.frame.getPlugInsNumber(); i++) {
            JMenuItem[] items = this.frame.getPlugIn(i).getMenuItem(part);
    		if (items!=null) {
    			for (int j = 0; j < items.length; j++) {
    				if (items[i]==null) {
    					menu.addSeparator();
    				} else {
    					menu.add(items[j]);
    				}
				}
    		}
		}
	}

	private void refreshState(GlobalData data) {
    	boolean somethingToSave = !data.isEmpty();
        this.menuItemSave.setEnabled(data.somethingHasChanged());
        this.menuItemSaveAs.setEnabled(somethingToSave);
    }

    public void actionPerformed(ActionEvent e) {
//		output.setText("Menu selected"+e.getSource().toString());
		Object source = e.getSource();
		if (source.equals(this.menuItemQuit)) {
			this.frame.dispatchEvent(new WindowEvent(this.frame, WindowEvent.WINDOW_CLOSING));
		} else if (source.equals(this.menuItemNew)) {
			if (SaveManager.MANAGER.verify(this.frame)) {
				this.frame.getData().clear();
			}
		} else if (source.equals(this.menuItemOpen)) {
			if (SaveManager.MANAGER.verify(this.frame)) {
				File path = frame.getData().getPath();
				String parent = path==null?null:path.getParent();
				JFileChooser chooser = new JFileChooser(parent);
				File file = chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION?chooser.getSelectedFile():null;
				if (file!=null) {
					try {
						frame.getData().read(file);
					} catch (Exception exception) {
						ErrorManager.INSTANCE.display(frame, exception, MessageFormat.format(LocalizationData.get("MainMenu.Open.Error.DialogContent"),file)); //$NON-NLS-1$
					}
				}
			}
		} else if (source.equals(this.menuItemSave)) {
			SaveManager.MANAGER.save(this.frame);
		} else if (source.equals(this.menuItemSaveAs)) {
			SaveManager.MANAGER.saveAs(this.frame);
		} else if (source.equals(this.menuItemAbout)) {
			new AboutDialog(MainMenuBar.this.frame).setVisible(true);
		}
	}

	public void processEvent(DataEvent event) {
		GlobalData data = (GlobalData) event.getSource();
		if ((event instanceof NeedToBeSavedChangedEvent) || (event instanceof EverythingChangedEvent)) {
			this.refreshState(data);
			if (event instanceof EverythingChangedEvent) {
				this.updateAccountMenu();
				this.updateFilterMenu();
			}
		} else if ((event instanceof AccountAddedEvent) || (event instanceof AccountRemovedEvent) ||
				((event instanceof AccountPropertyChangedEvent) && (((AccountPropertyChangedEvent)event).getProperty().equals(AccountPropertyChangedEvent.NAME)))) {
			this.updateAccountMenu();
		}
	}
	
	public void updateAccountMenu() {
		this.accountMenu.removeAll();
		JMenuItem menuItemNewAccount = new JMenuItem(LocalizationData.get("MainMenu.Accounts.New"), IconManager.NEW_ACCOUNT); //$NON-NLS-1$
	    menuItemNewAccount.setMnemonic(LocalizationData.getChar("MainMenu.Accounts.New.Mnemonic")); //$NON-NLS-1$
	    menuItemNewAccount.setToolTipText(LocalizationData.get("MainMenu.Accounts.New.ToolTip")); //$NON-NLS-1$
	    menuItemNewAccount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AccountDialog.open(frame.getData(), frame, null);
			}
		});
	    this.accountMenu.add(menuItemNewAccount);
        insertPluginMenuItems(this.accountMenu, AbstractPlugIn.ACCOUNTS_PART);
	    this.accountMenu.addSeparator();
	    GlobalData data = this.frame.getData();
	    if (data!=null) {
			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JMenuItem item = (JMenuItem) e.getSource();
					Account account = frame.getData().getAccount(item.getText());
					frame.getFilteredData().setAccounts(new Account[]{account});
					updateAccountMenu();
				}
			};
	        FilteredData filter = frame.getFilteredData();
			boolean hasAccountFilter = filter.hasFilterAccount();
	        for (int i=0;i<data.getAccountsNumber();i++) {
	        	Account account = data.getAccount(i);
	        	JRadioButtonMenuItem item = new JRadioButtonMenuItem(account.getName());
	        	if (hasAccountFilter) item.setSelected(filter.isOk(account));
	        	accountMenu.add(item);
	        	item.addActionListener(listener);
	        }
        	JRadioButtonMenuItem item = new JRadioButtonMenuItem(LocalizationData.get("MainMenuBar.NoFilter")); //$NON-NLS-1$
        	item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					frame.getFilteredData().setAccounts(null);
					updateAccountMenu();
				}
			});
        	item.setSelected(!hasAccountFilter);
        	accountMenu.add(item);
	    }
	}
	
	public void updateFilterMenu() {
		filterMenu.removeAll();
		if (frame.getFilteredData()!=null) {
        	buildBooleanFilterChoiceMenu(new String[]{LocalizationData.get("MainMenuBar.checked"), //$NON-NLS-1$
        			LocalizationData.get("MainMenuBar.notChecked")}, new int[]{FilteredData.CHECKED, FilteredData.NOT_CHECKED}); //$NON-NLS-1$
        	filterMenu.addSeparator();
        	buildBooleanFilterChoiceMenu(new String[]{LocalizationData.get("MainMenuBar.Expenses"), LocalizationData.get("MainMenuBar.Receipts")}, //$NON-NLS-1$ //$NON-NLS-2$
        			new int[]{FilteredData.EXPENSE, FilteredData.RECEIPT});
			
        	filterMenu.addSeparator();
			filterMenu.add(new JCheckBoxMenuItem(new CustomFilterAction(frame.getFilteredData(), this)));
		}
	}
	
	private void buildBooleanFilterChoiceMenu(String[] texts, int[] properties) {
        FilteredData filter = frame.getFilteredData();
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(texts[0]);
        menuItem.setSelected(filter.isOk(properties[0]) && !filter.isOk(properties[1]));
		filterMenu.add(menuItem);
		group.add(menuItem);
		menuItem.addActionListener(new FilterActionItem(properties[0]));
		menuItem = new JRadioButtonMenuItem(texts[1]);
        menuItem.setSelected(!filter.isOk(properties[0]) && filter.isOk(properties[1]));
        filterMenu.add(menuItem);
		group.add(menuItem);
		menuItem.addActionListener(new FilterActionItem(properties[1]));
		menuItem = new JRadioButtonMenuItem(LocalizationData.get("MainMenuBar.NoFilter")); //$NON-NLS-1$
        menuItem.setSelected(filter.isOk(properties[0]) && filter.isOk(properties[1]));
        filterMenu.add(menuItem);
		group.add(menuItem);
		menuItem.addActionListener(new FilterActionItem(properties[0] | properties[1]));
	}

	class FilterActionItem implements ActionListener {
		private int property;
		FilterActionItem (int property) {
			this.property = property;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			frame.getFilteredData().setFilter(property);
		}
	}

	public void updateMenu(AbstractPlugIn plugIn) {
		accountMenu.setVisible(plugIn.allowMenu(AbstractPlugIn.ACCOUNT_MENU));
		transactionMenu.setVisible(plugIn.allowMenu(AbstractPlugIn.TRANSACTIONS_MENU));
		filterMenu.setVisible(plugIn.allowMenu(AbstractPlugIn.FILTER_MENU));
	}
}
