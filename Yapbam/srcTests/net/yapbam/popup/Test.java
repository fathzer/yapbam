package net.yapbam.popup;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel pane = new JPanel();
        f.add(pane);
        pane.add(new JLabel("Question : "));
        String[] array = new String[] {"Carrefour", "Plein voiture", "Plein moto", "Carrefour Market", "Magazines", "R�paration", "Charges", "Imp�ts locaux",
        		"Impots fonciers", "Imp�t sur le revenu", "Cadeau Isa", "Inscription vacances CE", "Cin�ma", "Barrete m�moire Maman"/**/};
        final JTextField field = new PopupTextFieldList(array);
        field.setColumns(10);
        pane.add(field);
        f.setSize(400,200);
        f.setLocation(200,200);
        f.setVisible(true);
	}

}
