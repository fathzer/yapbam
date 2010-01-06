package net.yapbam.gui.statistics;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.yapbam.data.Category;
import net.yapbam.data.FilteredData;
import net.yapbam.data.SubTransaction;
import net.yapbam.data.Transaction;
import net.yapbam.gui.LocalizationData;

public class Budget {
	private FilteredData data;
	private boolean year;
	private HashMap<Key, Double> values;
	private List<Date> dates;
	private List<Category> categories;
	private MyTableModel tableModel;
	private MyRowHeaderModel rowHeaderModel;
	
	private static final class Key {
		Date date;
		Category category;
		Key(Date date, Category category) {
			super();
			this.date = date;
			this.category = category;
		}
		@Override
		public boolean equals(Object obj) {
			Key key = (Key) obj;
			return this.date.equals(key.date) && this.category.equals(key.category);
		}
		@Override
		public int hashCode() {
			return category.hashCode()+date.hashCode();
		}
	}
	
	/** Constructor.
	 * @param data The filtered data on which to build the budget
	 * @param year true to construct a budget per year, false to have it per month
	 */
	Budget(FilteredData data, boolean year) {
		//FIXME Listening to events is not done
		this.data = data;
		this.year = year;
		this.values = new HashMap<Key, Double>();
		this.dates = new LinkedList<Date>();
		this.categories = new LinkedList<Category>();
		
		for (int i = 0; i < data.getTransactionsNumber(); i++) {
			Transaction transaction = data.getTransaction(i);
			Date date = getNormalizedDate(transaction.getDate());
			for (int j = 0; j < transaction.getSubTransactionSize(); j++) {
				SubTransaction subTransaction = transaction.getSubTransaction(j);
				if (this.data.isOk(subTransaction)) {
					add (new Key(date, subTransaction.getCategory()), subTransaction.getAmount());
				}
			}
			Category category = transaction.getCategory();
			if (this.data.isOk(category)) {
				//FIXME Must test if amount is ok with the filter
				add (new Key(date, transaction.getCategory()), transaction.getComplement());
			}
		}
		this.tableModel = new MyTableModel();
		this.rowHeaderModel = new MyRowHeaderModel();
	}

	private void add(Key key, double amount) {
		if (amount!=0) {
			addToSortedList (dates, key.date); //FIXME Need to also add missing date between last date and this one (or this and first one). Maybe easier to remember only first and last date.
			addToSortedList(categories, key.category);
			Double value = this.values.get(key);
			if (value==null) value = 0.0;
			value += amount;
			this.values.put (key, value);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void addToSortedList(List list, Object element) {
		int index = Collections.binarySearch(list, element);
		if (index<0) list.add(-index-1, element);
	}

	private Date getNormalizedDate(Date date) {
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		if (year) c.set(Calendar.MONTH, 0);
		return c.getTime();
	}
	
	TableModel getTableModel() {
		return tableModel;
	}
	
	TableModel getRowHeaderModel() {
		return rowHeaderModel;
	}
	
	@SuppressWarnings("serial")
	private class MyTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return dates.size();
		}

		@Override
		public int getRowCount() {
			return categories.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Double value = values.get(new Key(dates.get(columnIndex), categories.get(rowIndex)));
			return ((value==null)||(value==0.0))?"":LocalizationData.getCurrencyInstance().format(value);
		}

		@Override
		public String getColumnName(int column) {
			//TODO Format the date
			return dates.get(column).toString();
		}
	}
	
	@SuppressWarnings("serial")
	private class MyRowHeaderModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return categories.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return categories.get(rowIndex);
		}
	}
}
