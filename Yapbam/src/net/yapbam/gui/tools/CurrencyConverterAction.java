package net.yapbam.gui.tools;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathzer.soft.ajlib.swing.Utils;
import com.fathzer.soft.ajlib.swing.worker.DefaultWorkInProgressPanel;
import com.fathzer.soft.ajlib.swing.worker.WorkInProgressFrame;
import com.fathzer.soft.ajlib.swing.worker.WorkInProgressPanel;
import com.fathzer.soft.ajlib.swing.worker.Worker;

import net.yapbam.currency.AbstractCurrencyConverter;
import net.yapbam.currency.ECBCurrencyConverter;
import net.yapbam.currency.YahooCurrencyConverter;
import net.yapbam.gui.ErrorManager;
import net.yapbam.gui.Preferences;
import net.yapbam.util.Portable;

@SuppressWarnings("serial")
final public class CurrencyConverterAction extends AbstractAction {
	private static final String SOURCE_PREF_KEY = "net.yapbam.gui.tools.currencyConverter.source"; //$NON-NLS-1$
	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyConverterAction.class);
	
	public CurrencyConverterAction() {
		super(Messages.getString("ToolsPlugIn.currencyConverter.title")); //$NON-NLS-1$
		putValue(SHORT_DESCRIPTION, Messages.getString("ToolsPlugIn.currencyConverter.toolTip")); //$NON-NLS-1$
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final Window owner = Utils.getOwnerWindow((Component) e.getSource());
		final Worker<AbstractCurrencyConverter, Void> worker = new Worker<AbstractCurrencyConverter, Void>() {
			@Override
			protected AbstractCurrencyConverter doProcessing() throws Exception {
				Proxy proxy = Preferences.INSTANCE.getHttpProxy();
				CurrencyConverterSource source = getSource();
				if (CurrencyConverterSource.ECB.equals(source)) {
					return new ECBCurrencyConverter(proxy, new FileCache(new File(Portable.getDataDirectory(), "ExchangeRates.xml"))); //$NON-NLS-1$
				} else {
					return new YahooCurrencyConverter(proxy, new FileCache(new File(Portable.getDataDirectory(), "YahooExchangeRates.xml"))); //$NON-NLS-1$
				}
			}
		};
		final WorkInProgressFrame waitFrame = new WorkInProgressFrame(owner, Messages.getString("ToolsPlugIn.currencyConverter.title"), ModalityType.APPLICATION_MODAL, worker) { //$NON-NLS-1$
			@Override
			protected WorkInProgressPanel buildProgressPanel() {
				DefaultWorkInProgressPanel panel = (DefaultWorkInProgressPanel) super.buildProgressPanel();
				panel.setMessage(Messages.getString("ToolsPlugin.currencyConverter.wait.message")); //$NON-NLS-1$
				panel.setIcon(UIManager.getIcon("OptionPane.informationIcon")); //$NON-NLS-1$
				return panel;
			}
		};
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(Worker.STATE_PROPERTY_NAME)) {
					if (evt.getNewValue().equals(StateValue.DONE)) {
						if (!worker.isCancelled()) {
							AbstractCurrencyConverter converter = null;
							try {
								converter = worker.get();
							} catch (InterruptedException e) {
							} catch (ExecutionException e) {
								if (e.getCause() instanceof IOException) {
									ErrorManager.INSTANCE.display(owner, null, Messages.getString("ToolsPlugin.currencyConverter.ioErrorMessage")); //$NON-NLS-1$
								} else {
									String message = MessageFormat.format(Messages.getString("CurrencyConverterPanel.errorMessage"), e.getCause()); //$NON-NLS-1$
									ErrorManager.INSTANCE.display(owner, null, message);
								}
							}
							if (converter!=null) {
								new CurrencyConverterDialog(owner, Messages.getString("ToolsPlugIn.currencyConverter.title"), converter).setVisible(true); //$NON-NLS-1$
							}
						}
					}
				}
			}
		});
		waitFrame.setVisible(true);
	}

	public static CurrencyConverterSource getSource() {
		try {
			String property = Preferences.INSTANCE.getProperty(SOURCE_PREF_KEY, CurrencyConverterSource.ECB.name());
			return CurrencyConverterSource.valueOf(property);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Invalid value in preferences", e);
			return CurrencyConverterSource.ECB;
		}
	}

	static void setSource(CurrencyConverterSource source) {
		if (CurrencyConverterSource.ECB.equals(source)) {
			Preferences.INSTANCE.removeProperty(SOURCE_PREF_KEY);
		} else {
			Preferences.INSTANCE.setProperty(SOURCE_PREF_KEY, source.name());
		}
	}
}