package net.yapbam.gui.filter;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.Color;

import javax.swing.JLabel;

import net.yapbam.gui.IconManager;
import net.yapbam.gui.IconManager.Name;

import org.jfree.ui.DateChooserPanel;

import sun.swing.SwingUtilities2;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class FilterElementView extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel label;

	/**
	 * Create the panel.
	 */
	public FilterElementView(String text) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		
		label = new JLabel();
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.weightx = 1.0;
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		add(label, gbc_label);
		
		setLabel(text);		
		
		final JPopupMenu popup = new JPopupMenu();

		final JLabel button = new JLabel(IconManager.get(Name.SPREADABLE));
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.WEST;
		gbc_button.fill = GridBagConstraints.NONE;
		gbc_button.gridx = 1;
		gbc_button.gridy = 0;
		add(button, gbc_button);
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (button.isEnabled() && !popup.isVisible()) {
					popup.removeAll();
					Component widget = buildPopupContent();
					popup.add(widget);
					popup.show(button, button.getSize().width, widget.getHeight());
				}
			}
		});
	}

	public void setLabel(String text) {
		String clipped = SwingUtilities2.clipStringIfNecessary(label, label.getFontMetrics(label.getFont()), text, 150);
		label.setText(clipped);
//		Dimension d = label.getPreferredSize();
//		Dimension newDimension = new Dimension(150,d.height);
//    label.setPreferredSize(newDimension);
//    label.setSize(newDimension);
	}

	protected Component buildPopupContent() {
		DateChooserPanel widget = new DateChooserPanel();
		widget.setChosenDateButtonColor(Color.RED);
		widget.setChosenOtherButtonColor(Color.GRAY);
		widget.setChosenMonthButtonColor(Color.WHITE);
		return widget;
	}
}
