package net.yapbam.ihm.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class EditPeriodicalTransactionAction extends AbstractAction {//LOCAL
	
	public EditPeriodicalTransactionAction() {
		super("Editer");
        putValue(SHORT_DESCRIPTION, "Ouvre le dialogue d'�dition de l'op�ration p�riodique s�lectionn�e");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		; //TODO
	}
}