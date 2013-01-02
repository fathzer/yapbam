package net.yapbam.gui.dialogs;

import java.awt.Dimension;
import java.awt.Window;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.astesana.ajlib.swing.dialog.AbstractDialog;
import net.astesana.ajlib.swing.widget.AbstractTitledPanel;
import net.astesana.ajlib.swing.widget.HTMLPane;
import net.yapbam.gui.LocalizationData;

@SuppressWarnings("serial")
public class BasicHTMLDialog extends AbstractDialog<Object[], Void> {
	private static final Dimension PREFERED_HTML_PANE_SIZE = new Dimension(480,240);

	public static enum Type {
		INFO, WARNING, ERROR, QUESTION;

		public Icon getIcon() {
			if (this.equals(INFO)) {
				return UIManager.getIcon("OptionPane.informationIcon");
			} else if (this.equals(ERROR)) {
				return UIManager.getIcon("OptionPane.errorIcon");
			} else if (this.equals(QUESTION)) {
				return UIManager.getIcon("OptionPane.questionIcon");
			} else if (this.equals(WARNING)) {
				return UIManager.getIcon("OptionPane.warningIcon");
			}
			return null;
		}
	}
	
	private AbstractTitledPanel<Void> panel;
	
	public BasicHTMLDialog(Window owner, String title, String header, Type type) {
		super(owner, title, new Object[]{header, type==null?null:type.getIcon()});
		getCancelButton().setVisible(false);
		getOkButton().setText(LocalizationData.get("GenericButton.close")); //$NON-NLS-1$
		getOkButton().setToolTipText(LocalizationData.get("GenericButton.close.ToolTip")); //$NON-NLS-1$
		getOkButton().requestFocus();
	}

	@Override
	protected Void buildResult() {
		return null;
	}

	@Override
	protected JPanel createCenterPane() {
		if (panel==null) {
			panel = new AbstractTitledPanel<Void>((String)data[0], (Icon)data[1], null) {
				private HTMLPane htmlPane;
				@Override
				public JComponent getCenterComponent() {
					if (htmlPane==null) {
						htmlPane = new HTMLPane();
						htmlPane.setPreferredSize(PREFERED_HTML_PANE_SIZE);
						htmlPane.setFocusable(false);
					}
					return htmlPane;
				}
			};
		}
		return panel;
	}

	@Override
	protected String getOkDisabledCause() {
		return null;
	}
	
	public void setContentType(String contentType) {
		((HTMLPane)panel.getCenterComponent()).setContentType(contentType);
	}
	
	public void setContent(String content) {
		((HTMLPane)panel.getCenterComponent()).setContent(content);
	}
}
