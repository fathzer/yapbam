package net.yapbam.gui.transfer;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.yapbam.data.GlobalData;
import net.yapbam.gui.util.AbstractDialog;

public class TransferAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	private GlobalData data;
	
	TransferAction(GlobalData data) {
		super("New transfer");
		this.data = data;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new TransferDialog(AbstractDialog.getOwnerWindow((Component) e.getSource()), "New transfer", data).setVisible(true);

	}
}
