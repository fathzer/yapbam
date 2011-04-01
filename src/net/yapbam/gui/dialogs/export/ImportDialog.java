package net.yapbam.gui.dialogs.export;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JPanel;

import net.yapbam.data.GlobalData;
import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.dialogs.AbstractDialog;

@SuppressWarnings("serial")
public class ImportDialog extends AbstractDialog<ImportDialog.Container> {
	private ImportPanel importPanel;
	public static Importer lastImporter;

	static final class Container {
		File file;
		GlobalData data;
		
		public Container(File file, GlobalData data) {
			super();
			this.file = file;
			this.data = data;
		}
	}
	
	public ImportDialog(Window owner, GlobalData data, File file) {
		super(owner, LocalizationData.get("ImportDialog.title"), new Container(file, data)); //$NON-NLS-1$
	}

	@Override
	protected Object buildResult() {
		Importer importer = importPanel.getImporter();
		lastImporter = importer;
		return importer;
	}

	@Override
	protected JPanel createCenterPane() {
		importPanel = new ImportPanel();
		importPanel.setData(data.data);
		importPanel.setFile(data.file);
		importPanel.addPropertyChangeListener(ImportPanel.INVALIDITY_CAUSE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateOkButtonEnabled();
			}
		});
		if (lastImporter!=null) importPanel.setImporter(lastImporter);
		return importPanel;
	}

	@Override
	protected String getOkDisabledCause() {
		return importPanel.getInvalidityCause();
	}
	
	@Override
	public void setVisible(boolean visible) {
		this.pack();
		super.setVisible(visible);
	}

	public Importer getImporter() {
		return (Importer) getResult();
	}

	public boolean getAddToCurrentData() {
		return importPanel.getAddToCurrentData();
	}
}
