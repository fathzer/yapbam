package net.yapbam.ihm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.yapbam.ihm.LocalizationData;
import net.yapbam.ihm.MainFrame;

@SuppressWarnings("serial")
public class GeneratePeriodicalTransactionsAction extends AbstractAction {
	private MainFrame frame;
	
	public GeneratePeriodicalTransactionsAction(MainFrame frame) {
		super(LocalizationData.get("MainMenu.Transactions.Periodical")); //$NON-NLS-1$
        putValue(SHORT_DESCRIPTION, LocalizationData.get("MainMenu.Transactions.Periodical.ToolTip")); //$NON-NLS-1$
        putValue(Action.MNEMONIC_KEY, (int)LocalizationData.getChar("MainMenu.Transactions.Periodical.Mnemonic")); //$NON-NLS-1$
        this.frame = frame;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println ("not already implemented ... but it will come very, very soon"); //TODO
	}
}