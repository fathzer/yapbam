package net.yapbam.gui.transactiontable;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

public class SpreadableMouseAdapter extends MouseAdapter {
	// This listener waits for clicks on the "spread sub-transaction" zone
	@Override
	public void mousePressed(MouseEvent e) {
		JTable table = (JTable) e.getSource(); 
		Point p = e.getPoint();
		int column = table.convertColumnIndexToModel(table.columnAtPoint(p));
		int viewRow = table.rowAtPoint(p);
		int row = table.convertRowIndexToModel(viewRow);
		SpreadableTableModel model = (SpreadableTableModel) table.getModel();
		if ((column == model.getSpreadColumnNumber()) && (row >= 0) && model.isSpreadable(row)) {
			boolean spread = model.isSpread(row);
			model.setSpread(row, !spread);
			if (spread) {
				table.setRowHeight(viewRow, table.getRowHeight());
			} else {
				int numberOfLines = model.getSpreadLines(row);
				table.setRowHeight(viewRow, table.getRowHeight() * numberOfLines);
			}
		}
	}
}
