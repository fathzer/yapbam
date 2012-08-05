package net.yapbam.gui.dropbox;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JList;
import javax.swing.JTextField;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.session.AccessTokenPair;

import net.astesana.ajlib.swing.Utils;
import net.astesana.ajlib.swing.worker.WorkInProgressFrame;
import net.astesana.ajlib.swing.worker.Worker;
import net.yapbam.gui.LocalizationData;
import net.yapbam.gui.Preferences;
import net.yapbam.gui.dropbox.ConnectionPanel.State;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JProgressBar;

@SuppressWarnings("serial")
public class DropboxFileChooser extends JPanel {
	private JPanel southPanel;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel centerPanel;
	private JList fileList;
	private JPanel filePanel;
	private JLabel lblNewLabel;
	private JTextField fileNameField;
	
	private DropboxAPI<YapbamDropboxSession> dropboxAPI;
	private JLabel lblAccount;
	private JButton disconnectButton;
	private JPanel northPanel;
	private JButton refreshButton;
	private JProgressBar progressBar;

	/**
	 * Create the panel.
	 */
	public DropboxFileChooser() {
		setLayout(new BorderLayout(0, 0));
		add(getNorthPanel(), BorderLayout.NORTH);
		add(getSouthPanel(), BorderLayout.SOUTH);
		add(getCenterPanel(), BorderLayout.CENTER);
	}
	
	public void connect() {
		if (getDropboxAPI()==null) {
			final ConnectionPanel connectionPanel = new ConnectionPanel();
			connectionPanel.addPropertyChangeListener(ConnectionPanel.STATE_PROPERTY, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					ConnectionPanel.State state = (State) evt.getNewValue();
					if (state.equals(ConnectionPanel.State.FAILED)) {
						JOptionPane.showMessageDialog(DropboxFileChooser.this, "There was something wrong", "Error", JOptionPane.ERROR_MESSAGE);
					} else if (state.equals(ConnectionPanel.State.GRANTED)) {
						AccessTokenPair pair = connectionPanel.getAccessTokenPair();
						Preferences.INSTANCE.setProperty("Dropbox.access.key", pair.key);
						Preferences.INSTANCE.setProperty("Dropbox.access.secret", pair.secret);
						getSouthPanel().setVisible(true);
						getNorthPanel().setVisible(true);
						remove(connectionPanel);
						add(getCenterPanel(), BorderLayout.CENTER);
						refresh();
					}
				}
			});
			getNorthPanel().setVisible(false);
			getSouthPanel().setVisible(false);
			remove(getCenterPanel());
			add(connectionPanel, BorderLayout.CENTER);
		} else {
			refresh();
		}
	}

	private static class DropboxInfo {
		Account account;
		List<Entry> files;
	}
	
	public DropboxAPI<YapbamDropboxSession> getDropboxAPI() {
		if (dropboxAPI==null) {
			String accessKey = Preferences.INSTANCE.getProperty("Dropbox.access.key");
			String accessSecret = Preferences.INSTANCE.getProperty("Dropbox.access.secret");
			if (accessKey!=null || accessSecret!=null) {
				YapbamDropboxSession session = new YapbamDropboxSession();
				session.setAccessTokenPair(new AccessTokenPair(accessKey, accessSecret));
				dropboxAPI = new DropboxAPI<YapbamDropboxSession>(session);
			}
		}
		return dropboxAPI;
	}
	
	private void refresh() {
		add(northPanel, BorderLayout.NORTH);
		new WorkInProgressFrame(Utils.getOwnerWindow(this), "Please wait", ModalityType.APPLICATION_MODAL, new Worker<DropboxInfo, Void>() {
			@Override
			protected DropboxInfo doInBackground() throws Exception {
				setPhase("Connecting to Dropbox", -1);
				DropboxInfo info = new DropboxInfo();
				info.account = getDropboxAPI().accountInfo();
				info.files = getDropboxAPI().metadata("", 0, null, true, null).contents;
				return info;
			}

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#done()
			 */
			@Override
			protected void done() {
				try {
					DropboxInfo info = get();
					getLblAccount().setText(MessageFormat.format("Account: {0}", info.account.displayName));
					getFileList().setListData(info.files.toArray(new Entry[info.files.size()]));
					long percentUsed = 100*(info.account.quotaNormal+info.account.quotaShared) / info.account.quota; 
					getProgressBar().setValue((int)percentUsed);
					double remaining = info.account.quota-info.account.quotaNormal-info.account.quotaShared;
					String unit = "bytes";
					if (remaining>1024) {
						unit = "kB";
						remaining = remaining/1024;
						if (remaining>1024) {
							unit = "MB";
							remaining = remaining/1024;
							if (remaining>1024) {
								unit = "GB";
								remaining = remaining/1024;
							}
						}
					}
					getProgressBar().setString(MessageFormat.format("{1}{0} free", unit, new DecimalFormat("0.0").format(remaining)));
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CancellationException e) {
					// The task was cancelled, do nothing
				}
				super.done();
			}
			
		}).setVisible(true);
	}
	
	private JPanel getSouthPanel() {
		if (southPanel == null) {
			southPanel = new JPanel();
			GridBagLayout gbl_southPanel = new GridBagLayout();
			southPanel.setLayout(gbl_southPanel);
			GridBagConstraints gbc_okButton = new GridBagConstraints();
			gbc_okButton.anchor = GridBagConstraints.EAST;
			gbc_okButton.insets = new Insets(0, 0, 5, 5);
			gbc_okButton.weightx = 1.0;
			gbc_okButton.gridx = 0;
			gbc_okButton.gridy = 0;
			southPanel.add(getOkButton(), gbc_okButton);
			GridBagConstraints gbc_cancelButton = new GridBagConstraints();
			gbc_cancelButton.insets = new Insets(0, 0, 5, 5);
			gbc_cancelButton.gridx = 1;
			gbc_cancelButton.gridy = 0;
			southPanel.add(getCancelButton(), gbc_cancelButton);
		}
		return southPanel;
	}
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(LocalizationData.get("GenericButton.ok"));
			okButton.setEnabled(false);
		}
		return okButton;
	}
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(LocalizationData.get("GenericButton.cancel"));
		}
		return cancelButton;
	}
	private JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			GridBagLayout gbl_centerPanel = new GridBagLayout();
			centerPanel.setLayout(gbl_centerPanel);
			GridBagConstraints gbc_fileList = new GridBagConstraints();
			gbc_fileList.weightx = 1.0;
			gbc_fileList.fill = GridBagConstraints.BOTH;
			gbc_fileList.gridwidth = 0;
			gbc_fileList.weighty = 1.0;
			gbc_fileList.insets = new Insets(5, 5, 5, 5);
			gbc_fileList.gridx = 0;
			gbc_fileList.gridy = 0;
			centerPanel.add(getFileList(), gbc_fileList);
			GridBagConstraints gbc_filePanel = new GridBagConstraints();
			gbc_filePanel.fill = GridBagConstraints.HORIZONTAL;
			gbc_filePanel.gridwidth = 0;
			gbc_filePanel.insets = new Insets(0, 5, 5, 5);
			gbc_filePanel.gridx = 0;
			gbc_filePanel.gridy = 1;
			centerPanel.add(getFilePanel(), gbc_filePanel);
		}
		return centerPanel;
	}
	private JList getFileList() {
		if (fileList == null) {
			fileList = new JList();
			fileList.setBorder(new LineBorder(new Color(0, 0, 0)));
		}
		return fileList;
	}
	private JPanel getFilePanel() {
		if (filePanel == null) {
			filePanel = new JPanel();
			filePanel.setLayout(new BorderLayout(0, 0));
			filePanel.add(getLblNewLabel(), BorderLayout.WEST);
			filePanel.add(getFileNameField(), BorderLayout.CENTER);
		}
		return filePanel;
	}
	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("File: ");
		}
		return lblNewLabel;
	}
	private JTextField getFileNameField() {
		if (fileNameField == null) {
			fileNameField = new JTextField();
			fileNameField.setColumns(10);
		}
		return fileNameField;
	}
	private JLabel getLblAccount() {
		if (lblAccount == null) {
			lblAccount = new JLabel("Account :");
		}
		return lblAccount;
	}
	private JButton getDisconnectButton() {
		if (disconnectButton == null) {
			disconnectButton = new JButton("Disconnect", new ImageIcon(getClass().getResource("brokenLink.png")));
		}
		return disconnectButton;
	}
	private JPanel getNorthPanel() {
		if (northPanel == null) {
			northPanel = new JPanel();
			GridBagLayout gbl_northPanel = new GridBagLayout();
			northPanel.setLayout(gbl_northPanel);
			GridBagConstraints gbc_lblAccount = new GridBagConstraints();
			gbc_lblAccount.weightx = 1.0;
			gbc_lblAccount.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblAccount.anchor = GridBagConstraints.WEST;
			gbc_lblAccount.insets = new Insets(0, 5, 5, 5);
			gbc_lblAccount.gridx = 0;
			gbc_lblAccount.gridy = 0;
			northPanel.add(getLblAccount(), gbc_lblAccount);
			GridBagConstraints gbc_refreshButton = new GridBagConstraints();
			gbc_refreshButton.gridheight = 0;
			gbc_refreshButton.insets = new Insets(0, 0, 0, 5);
			gbc_refreshButton.gridx = 1;
			gbc_refreshButton.gridy = 0;
			northPanel.add(getRefreshButton(), gbc_refreshButton);
			GridBagConstraints gbc_disconnectButton = new GridBagConstraints();
			gbc_disconnectButton.gridheight = 0;
			gbc_disconnectButton.gridx = 2;
			gbc_disconnectButton.gridy = 0;
			northPanel.add(getDisconnectButton(), gbc_disconnectButton);
			GridBagConstraints gbc_progressBar = new GridBagConstraints();
			gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
			gbc_progressBar.insets = new Insets(0, 5, 0, 5);
			gbc_progressBar.gridx = 0;
			gbc_progressBar.gridy = 1;
			northPanel.add(getProgressBar(), gbc_progressBar);
		}
		return northPanel;
	}
	private JButton getRefreshButton() {
		if (refreshButton == null) {
			refreshButton = new JButton("Refresh", new ImageIcon(getClass().getResource("synchronize.png")));
			refreshButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					refresh();
				}
			});
		}
		return refreshButton;
	}
	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
			progressBar.setStringPainted(true);
			progressBar.setString("");
		}
		return progressBar;
	}
}
