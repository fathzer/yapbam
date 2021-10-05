package net.yapbam.gui.dialogs.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.text.StringEscapeUtils;

public class ExporterJsonFormat implements IExportableFormat {
	private Writer writer;
	private boolean isFirstLine = true;
	private boolean isFirstValue = true;

	public ExporterJsonFormat(OutputStream stream, Charset encoding) {
		this.writer = new OutputStreamWriter(stream, encoding);
	}

	@Override
	public void addHeader() throws IOException {
		this.writer.append("{");
		this.writer.append("\"values\":[");
	}

	@Override
	public void addLineStart() throws IOException {
		if (isFirstLine) {
			isFirstLine = false;
		} else {
			this.writer.append(",");
		}
		this.writer.append("[");
	}

	@Override
	public void addLineEnd() throws IOException {
		this.writer.append("]");
		this.isFirstValue = true;
	}

	@Override
	public void addValue(String value) throws IOException {
		if (isFirstValue) {
			isFirstValue = false;
		} else {
			this.writer.append(",");
		}
		this.writer.append(String.format("\"%s\"", StringEscapeUtils.escapeJson(value)));
	}

	@Override
	public void addFooter() throws IOException {
		this.writer.append("]}");
	}

	@Override
	public void close() throws IOException {
		this.writer.close();
	}
}
