package net.yapbam.gui.dialogs;

import net.astesana.ajlib.swing.Utils;
import net.yapbam.data.Account;
import net.yapbam.data.GlobalData;
import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.widget.AbstractSelector;

public class AccountWidget extends AbstractSelector<Account, GlobalData> {
	private static final long serialVersionUID = 1L;
	public static final String ACCOUNT_PROPERTY = "account"; //$NON-NLS-1$
	
	public AccountWidget(GlobalData data) {
		super(data);
	}
	
	@Override
	protected String getLabel() {
		return LocalizationData.get("AccountDialog.account"); //$NON-NLS-1$
	}
	
	@Override
	protected String getNewButtonTip() {
		return LocalizationData.get("TransactionDialog.account.new.tooltip"); //$NON-NLS-1$
	}

	@Override
	protected String getPropertyName() {
		return ACCOUNT_PROPERTY;
	}

	@Override
	protected void populateCombo() {
		if (getParameters()!=null) {
			for (int i = 0; i < getParameters().getAccountsNumber(); i++) {
				getCombo().addItem(getParameters().getAccount(i));
			}
		}
	}

	@Override
	protected Object getDefaultRenderedValue(Account account) {
		return account==null ? account : account.getName();
	}

	@Override
	protected Account createNew() {
		if (getParameters()!=null) {
			return EditAccountDialog.open(getParameters(), Utils.getOwnerWindow(this), null);
		} else {
			return null;
		}
	}
}
