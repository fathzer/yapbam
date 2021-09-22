package net.yapbam.gui.dialogs.export;

import static j2html.TagCreator.body;
import static j2html.TagCreator.document;
import static j2html.TagCreator.html;
import static j2html.TagCreator.style;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.tr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import com.fathzer.soft.ajlib.utilities.CSVWriter;

import j2html.attributes.Attribute;
import j2html.tags.ContainerTag;
import net.yapbam.data.Account;
import net.yapbam.data.FilteredData;
import net.yapbam.data.SubTransaction;
import net.yapbam.data.Transaction;
import net.yapbam.gui.LocalizationData;

//TODO Maybe could be split in two classes (CSV Exporter and HTML Exporter) 
public class Exporter {
	private ExporterParameters parameters;
	private DateFormat dateFormatter;
	private NumberFormat amountFormatter;
	
	public Exporter(ExporterParameters parameters) {
		super();
		this.parameters = parameters;
		dateFormatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, LocalizationData.getLocale());
		amountFormatter = CSVWriter.getDecimalFormater(LocalizationData.getLocale());
	}
	
	public void exportFile(File file, FilteredData data) throws IOException {
		int[] fields = parameters.getExportedIndexes();
		Iterator<Transaction> transactions = parameters.isExportFilteredData() ? new FilteredTransactions(data) : new GlobalTransactions(data);

		Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), parameters.getEncoding());
		try {
			if (ExportFormatType.CSV.equals(parameters.getExportFormat())) {
				CSVWriter writer = new CSVWriter(fileWriter);
				writer.setSeparator(parameters.getSeparator());
				if (parameters.isInsertHeader()) {
					// insert the header line
					for (int i = 0; i < fields.length; i++) {
						writer.writeCell(ExportTableModel.COLUMNS[fields[i]]);
					}
					writer.newLine();
				}
				if (parameters.isExportInitialBalance()) {
					// Export accounts initial balance
					for (int i = 0; i < data.getGlobalData().getAccountsNumber(); i++) {
						Account account = data.getGlobalData().getAccount(i);
						if (data.getFilter().isOk(account) || !parameters.isExportFilteredData()) {
							for (int j = 0; j < fields.length; j++) {
								writer.writeCell(getField(account, fields[j]));
							}
							writer.newLine();
						}
					}
				}
				while (transactions.hasNext()) {
					Transaction transaction = transactions.next();
					for (int i = 0; i < fields.length; i++) {
						writer.writeCell(getField(transaction, fields[i]));
					}
					writer.newLine();
					for (int j = 0; j < transaction.getSubTransactionSize(); j++) {
						SubTransaction sub = transaction.getSubTransaction(j);
						for (int i = 0; i < fields.length; i++) {
							writer.writeCell(getField(sub, fields[i]));
						}
						writer.newLine();
					}
				}
				writer.flush();
			} else if (ExportFormatType.HTML.equals(parameters.getExportFormat())) {

				ContainerTag body = body();
				ContainerTag tBody = tbody();

				ContainerTag style = style();
				style.withText("table, th, td {border: 1px solid black;border-collapse: collapse;}");
				body.with(style);

				if (parameters.isInsertHeader()) {
					// insert the header line
					ContainerTag tr = tr();
					for (int i = 0; i < fields.length; i++) {
						tr.with(td(ExportTableModel.COLUMNS[fields[i]]));
					}
					tBody.with(tr);
				}

				if (parameters.isExportInitialBalance()) {
					for (int i = 0; i < data.getGlobalData().getAccountsNumber(); i++) {
						Account account = data.getGlobalData().getAccount(i);
						if (data.getFilter().isOk(account) || !parameters.isExportFilteredData()) {
							ContainerTag tr = tr();
							for (int j = 0; j < fields.length; j++) {
								tr.with(td(getField(account, fields[j])));
							}
							tBody.with(tr);
						}
					}
				}

				while (transactions.hasNext()) {
					Transaction transaction = transactions.next();

					ContainerTag tr = tr();
					for (int i = 0; i < fields.length; i++) {
						tr.with(td(getField(transaction, fields[i])));
					}

					tBody.with(tr);

					for (int j = 0; j < transaction.getSubTransactionSize(); j++) {
						tr = tr();
						SubTransaction sub = transaction.getSubTransaction(j);
						for (int i = 0; i < fields.length; i++) {
							tr.with(td(getField(sub, fields[i])));
						}
						tBody.with(tr);
					}

				}

				ContainerTag table = table(tBody);
				table.attr(new Attribute("width", "90%"));
				table.attr(new Attribute("style", "margin:0 auto;"));

				body.with(table);

				fileWriter.append(document(html(body)));
				fileWriter.flush();
			} else {
				throw new IOException("Unsupported format: " + parameters.getExportFormat());
			}
		} finally {
			fileWriter.close();
		}
	}
	
	private String getField(Account account, int field) {
		String result = null;
		if ((field==ExportTableModel.DATE_INDEX) || (field==ExportTableModel.CATEGORY_INDEX) ||
				(field==ExportTableModel.MODE_INDEX) || (field==ExportTableModel.NUMBER_INDEX) ||
				(field==ExportTableModel.STATEMENT_INDEX) || (field==ExportTableModel.VALUE_DATE_INDEX) ||
				(field==ExportTableModel.DESCRIPTION_INDEX) || (field==ExportTableModel.COMMENT_INDEX)) {
			result = null;
		} else if (field==ExportTableModel.ACCOUNT_INDEX) {
			result = account.getName();
		} else if (field==ExportTableModel.AMOUNT_INDEX) {
			double amount = account.getInitialBalance();
			result = format(amount);
		} else {
			throw new IllegalArgumentException();
		}
		return result==null?"":result; //$NON-NLS-1$
	}

	private String format(double amount) {
		return amountFormatter.format(amount);
	}
	
	private String getField(SubTransaction transaction, int field) {
		String result = null;
		if ((field==ExportTableModel.ACCOUNT_INDEX) || (field==ExportTableModel.DATE_INDEX) ||
				(field==ExportTableModel.MODE_INDEX) || (field==ExportTableModel.NUMBER_INDEX) ||
				(field==ExportTableModel.STATEMENT_INDEX) || (field==ExportTableModel.VALUE_DATE_INDEX)
				|| (field==ExportTableModel.COMMENT_INDEX)) {
			result = null;
		} else if (field==ExportTableModel.AMOUNT_INDEX) {
			result = format(transaction.getAmount());
		} else if (field==ExportTableModel.CATEGORY_INDEX) {
			result = transaction.getCategory().getName();
		} else if (field==ExportTableModel.DESCRIPTION_INDEX) {
			result = transaction.getDescription();
		} else {
			throw new IllegalArgumentException();
		}
		return result==null?"":result; //$NON-NLS-1$
	}
	
	private String getField(Transaction transaction, int field) {
		String result = null;
		if (field==ExportTableModel.ACCOUNT_INDEX) {
			result = transaction.getAccount().getName();
		} else if (field==ExportTableModel.AMOUNT_INDEX) {
			result = format(transaction.getAmount());
		} else if (field==ExportTableModel.CATEGORY_INDEX) {
			result = transaction.getCategory().getName();
		} else if (field==ExportTableModel.DATE_INDEX) {
			result = dateFormatter.format(transaction.getDate());
		} else if (field==ExportTableModel.DESCRIPTION_INDEX) {
			result = transaction.getDescription();
		} else if (field==ExportTableModel.COMMENT_INDEX) {
			result = transaction.getComment();
		} else if (field==ExportTableModel.MODE_INDEX) {
			result = transaction.getMode().getName();
		} else if (field==ExportTableModel.NUMBER_INDEX) {
			result = transaction.getNumber();
		} else if (field==ExportTableModel.STATEMENT_INDEX) {
			result = transaction.getStatement();
		} else if (field==ExportTableModel.VALUE_DATE_INDEX) {
			result = dateFormatter.format(transaction.getValueDate());
		} else {
			throw new IllegalArgumentException();
		}
		return result==null?"":result; //$NON-NLS-1$
	}
	
	private static class GlobalTransactions implements Iterator<Transaction> {
		protected int index = 0;
		protected FilteredData data;
		
		private GlobalTransactions(FilteredData data) {
			this.index = 0;
			this.data = data;
		} 
		
		@Override
		public boolean hasNext() {
			return index<data.getGlobalData().getTransactionsNumber();
		}

		@Override
		public Transaction next() {
			Transaction transaction = data.getGlobalData().getTransaction(index);
			index++;
			return transaction;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	private static class FilteredTransactions extends GlobalTransactions {
		private FilteredTransactions(FilteredData data) {
			super(data);
		}

		@Override
		public boolean hasNext() {
			return index<data.getTransactionsNumber();
		}

		@Override
		public Transaction next() {
			Transaction transaction = data.getTransaction(index);
			index++;
			return transaction;
		}
		
	}
}
