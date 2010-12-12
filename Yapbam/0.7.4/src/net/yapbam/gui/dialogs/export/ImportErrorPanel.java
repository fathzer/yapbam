package net.yapbam.gui.dialogs.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.transactiontable.ColoredModel;
import net.yapbam.gui.util.JTableUtils;
import java.awt.Insets;

/** The panel that displays import errors.
 */
public class ImportErrorPanel extends JPanel {
	
	@SuppressWarnings("serial")
	private final class ObjectRenderer extends DefaultTableCellRenderer {	
		public ObjectRenderer () {
			super();
		}

	    @Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    	super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
/*
	    	ColoredModel model = (ColoredModel)table.getModel();
		    row = table.convertRowIndexToModel(row);
		    column = table.convertColumnIndexToModel(column);
			if (isSelected) {
		        setBackground(table.getSelectionBackground());
		        setForeground(table.getSelectionForeground());
		    } else {
		        boolean error = this.getTransaction(row).getAmount()<0;  	
		        setForeground(table.getForeground());
		        setBackground(error?Color.red:table.getBackground());
		    }
		    //		    this.setHorizontalAlignment(model.getAlignment(table.convertColumnIndexToModel(column)));
		    setValue(value);
		    */
	    	if (!isSelected) { // No special rendering on the line number (displayed in the first row)
			    column = table.convertColumnIndexToModel(column);
			    if (column>0) {
				    row = table.convertRowIndexToModel(row);
			        boolean error = errors[row].hasError(column-1);
			        setBackground(error?Color.red:table.getBackground());
			    }
	    	}
	    	return this;
	    }
	}

	@SuppressWarnings("serial")
	private final class ImportErrorTableModel extends AbstractTableModel  implements ColoredModel {
		private int columnsCount;
		private String[] columnsHeaders;
		
		public ImportErrorTableModel() {
			// Compute the column count (max of imported column indexes and number of columns in the error lines)
			// We will add one column for the line number
			columnsCount = 1;
			for (int i = 0; i < importedFileColumns.length; i++) {
				if (importedFileColumns[i]>=0) columnsCount = Math.max(columnsCount, importedFileColumns[i]+1);
			}
			for (int i = 0; i < errors.length; i++) {
				columnsCount = Math.max(columnsCount, errors[i].getFields().length);
			}
			columnsCount++; // Add one column to insert the line number
			columnsHeaders = new String[columnsCount];
			// Compute the column headers (blank or the Yapbam corresponding field)
			// First column has a fixed header (line number)
			columnsHeaders[0] = LocalizationData.get("ImportDialog.errorMessagelienNumberColumnHeader"); //$NON-NLS-1$
			// Initialize with empty strings
			for (int i = 1; i < columnsHeaders.length; i++) {
				columnsHeaders[i] = "";
			}
			for (int i = 0; i < importedFileColumns.length; i++) {
				if (importedFileColumns[i]>=0) {
					// This Yapbam field is imported => Put it in the headers
					columnsHeaders[importedFileColumns[i]+1] = ExportTableModel.columns[i];
				}
			}
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex==0) {
				return errors[rowIndex].getLineNumber();
			} else {
				String[] fields = errors[rowIndex].getFields();
				if (columnIndex>fields.length) return "";
				return fields[columnIndex-1];
			}
		}

		@Override
		public int getRowCount() {
			return errors.length;
		}

		@Override
		public int getColumnCount() {
			return columnsCount;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex==0) return Integer.class;
			return super.getColumnClass(columnIndex);
		}

		@Override
		public String getColumnName(int column) {
			return columnsHeaders[column];
		}

		@Override
		public int getAlignment(int column) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setRowLook(Component renderer, JTable table, int row, boolean isSelected) {
			// TODO Auto-generated method stub
			if (isSelected) {
		        renderer.setBackground(table.getSelectionBackground());
		        renderer.setForeground(table.getSelectionForeground());
		    } else {
		        boolean error = (row==0); //TODO  	
		        renderer.setForeground(table.getForeground());
		        renderer.setBackground(error?Color.RED:table.getBackground());
		    }		
		}
	}

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private JLabel jLabel = null;
	private ImportError[] errors;
	private int[] importedFileColumns;

	/**
	 * This is the default constructor
	 * just for Eclipse Visual Editor
	 */
	public ImportErrorPanel() {
		this(new int[0], new ImportError[0]);
	}
	
	public ImportErrorPanel(int[] importedFileColumns, ImportError[] errors) {
		super();
		this.errors = errors;
		this.importedFileColumns = importedFileColumns;
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.anchor = GridBagConstraints.WEST;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.insets = new Insets(5, 5, 5, 0);
		gridBagConstraints1.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridx = 0;
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(getJScrollPane(), gridBagConstraints);
		this.add(getJLabel(), gridBagConstraints1);
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable(new ImportErrorTableModel());
			jTable.getTableHeader().setReorderingAllowed(false); // Disallow columns reordering
			jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			jTable.setIntercellSpacing(new Dimension(4, jTable.getIntercellSpacing().height));
			jTable.setDefaultRenderer(Object.class, new ObjectRenderer());
			JTableUtils.initColumnSizes(jTable, 200);
			Dimension preferredSize = getJTable().getPreferredSize();
			preferredSize.width = Math.min(preferredSize.width, 1024);
			preferredSize.height = Math.min(preferredSize.height, 600);
			jTable.setPreferredScrollableViewportSize(preferredSize);
	        jTable.setFillsViewportHeight(true);
		}
		return jTable;
	}

	/**
	 * This method initializes jLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getJLabel() {
		if (jLabel == null) {
			jLabel = new JLabel();
			jLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon")); //$NON-NLS-1$
			jLabel.setText(LocalizationData.get("ImportDialog.errorMessagemessage")); //$NON-NLS-1$
		}
		return jLabel;
	}

}
