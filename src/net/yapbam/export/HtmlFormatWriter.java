package net.yapbam.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

public class HtmlFormatWriter implements ExportWriter {

	private AtomicInteger tableRowIndex;
	private AtomicInteger tableCellIndex;
	private Writer writer;
	private Charset encoding;
	private String statementId;
	private String startBalance;
	private String endBalance;
	private URL css;

	public HtmlFormatWriter(OutputStream stream, Charset encoding) {
		this(stream, encoding, null);
	}
	
	public HtmlFormatWriter(OutputStream stream, Charset encoding, String statementId) {
		this(stream, encoding, statementId, null);
	}
	
	public HtmlFormatWriter(OutputStream stream, Charset encoding, String statementId, String startBalance) {
		this(stream, encoding, statementId, startBalance, null);
	}
	
	public HtmlFormatWriter(OutputStream stream, Charset encoding, String statementId, String startBalance, String endBalance) {
		this(stream, encoding, statementId, startBalance, endBalance, null);
	}
	
	public HtmlFormatWriter(OutputStream stream, Charset encoding, String statementId, String startBalance, String endBalance, URL css) {
		this.tableRowIndex = new AtomicInteger(0);
		this.tableCellIndex = new AtomicInteger(0); 
		this.writer = new OutputStreamWriter(stream, encoding);
		this.encoding = encoding;
		this.statementId = statementId;
		this.startBalance = startBalance;
		this.endBalance = endBalance;
		this.css = css;
	}

	@Override
	public void addHeader() throws IOException {
		this.writer.append("<!DOCTYPE html>\n");
		this.writer.append("<html>\n");
		this.writer.append("<head>\n");
		this.writer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+encoding.name()+"\"/>");
		if(css != null) {
			try {
				this.writer.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + css.toURI() + "\"/>\n");
			} catch (URISyntaxException ex) {
				throw new IOException(ex.getMessage());
			}
		} else {
			this.writer.append("<style type=\"text/css\"> table, th, td {border: 1px solid black;border-collapse: collapse;} #transaction-table {margin:0 auto;width:90%;}</style>\n");
		}
		this.writer.append("</head>\n");
		this.writer.append("<body>\n");
		if (!StringUtils.isBlank(statementId) || !StringUtils.isBlank(startBalance)) {
			this.writer.append("<center>\n");
			if (!StringUtils.isBlank(statementId)) {
				this.writer.append(String.format("<p id=\"statement-id\">%s</p>\n", statementId));
			}
			if (!StringUtils.isBlank(startBalance)) {
				this.writer.append(String.format("<p id=\"start-balance\">%s</p>\n", startBalance));
			}
			this.writer.append("</center>\n");
		}
		this.writer.append("<table id=\"transaction-table\">\n");
	}

	@Override
	public void addLineStart() throws IOException {
		this.writer.append(String.format("<tr id=\"row-%d\">\n", getTableRowIndex()));
	}

	@Override
	public void addLineEnd() throws IOException {
		this.writer.append("</tr>\n");
	}

	@Override
	public void addValue(String ...value) throws IOException {
		this.writer.append(String.format("<td id=\"cell-%d\"%s>%s</td>", getTabelCellIndex(), ((value.length > 1) ? //
				String.format(" class=\"%s\"", StringUtils.join(ArrayUtils.remove(value, 0), ';')) : ""), StringEscapeUtils.escapeHtml4(value[0])));
	}
	
	@Override
	public void addFooter() throws IOException {
		this.writer.append("</table>");
		if (!StringUtils.isBlank(endBalance)) {
			this.writer.append(String.format("<center><p id=\"end-balance\">%s</p></center>\n", endBalance));
		}
		this.writer.append("</body>");
		this.writer.append("</html>");
	}

	@Override
	public void close() throws IOException {
		this.writer.close();
	}
	
	private Integer getTableRowIndex()  {
		return tableRowIndex.incrementAndGet();
	}
	
	private Integer getTabelCellIndex()  {
		return tableCellIndex.incrementAndGet();
	}
}
