package net.yapbam.ihm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class NewPeriodicalTransactionAction extends AbstractAction {//LOCAL
	
	public NewPeriodicalTransactionAction() {
		super("Ajouter");
        putValue(SHORT_DESCRIPTION, "Ouvre le dialogue de cr�ation d'une op�ration");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		; //TODO
	}
}