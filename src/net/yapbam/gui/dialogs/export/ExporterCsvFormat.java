package net.yapbam.gui.dialogs.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import com.fathzer.soft.ajlib.utilities.CSVWriter;

public class ExporterCsvFormat implements IExportableFormat {
	
	private Writer writer;
	private CSVWriter csv;

	public ExporterCsvFormat(OutputStream stream, char separator, Charset encoding) {
		this.writer = new OutputStreamWriter(stream, encoding);
		this.csv = new CSVWriter(writer);
		this.csv.setSeparator(separator);
	}

	@Override
	public void addHeader() throws IOException {
		return;
	}

	@Override
	public void addLineStart() throws IOException {
		return;
	}

	@Override
	public void addLineEnd() throws IOException {
		csv.newLine();
	}

	@Override
	public void addValue(String value) throws IOException {
		csv.writeCell(value);
	}
	
	@Override
	public void addFooter() throws IOException {
		return;
	}

	@Override
	public void close() throws IOException {
		csv.flush();
		writer.close();
	}

}
