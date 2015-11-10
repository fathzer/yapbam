package net.yapbam.gui.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

/**
 * A fake cell renderer that displays HTML content, including properly displayed links.
 * @see URLTableCellEditor
 */
public class URLTableCellRenderer extends JEditorPane implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	public URLTableCellRenderer() {
        // Set the content type
        setContentType("text/html");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    	if (table instanceof PaintedTable) {
			TablePainter painter = ((PaintedTable)table).getPainter();
			painter.setRowLook(this, table, table.convertRowIndexToModel(row), isSelected);
    	}
        setBorder(new LineBorder(getBackground(), 1));
        setText("<html><body style=\"" + getStyle() + "\">" + value + "</body></html>");
        return this;
    }
    
	StringBuilder getStyle() {
		Color color = getBackground();
	    // create some css from the label's font
		StringBuilder style = new StringBuilder();
	    style.append("background-color: rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+");");
	    style.append("margin-left: 5px;");
	    return style;
	}
}