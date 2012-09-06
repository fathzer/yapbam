package net.yapbam.gui.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.WebAuthSession;

import net.astesana.dropbox.DropboxFileChooser;
import net.yapbam.gui.Preferences;

@SuppressWarnings("serial")
public class YapbamDropboxFileChooser extends DropboxFileChooser {
	private static final String DROPBOX_ACCESS_KEY = "Dropbox.access.key";
	private static final String DROPBOX_ACCESS_SECRET = "Dropbox.access.secret";
	private DropboxAPI<? extends WebAuthSession> dropboxAPI;

	/**
	 * Creates the panel.
	 */
	public YapbamDropboxFileChooser() {
		super();
	}

	@Override
	protected String filter(Entry entry) {
		String fileName = entry.fileName();
		if (fileName.endsWith(".zip")) {
			return fileName.substring(0, fileName.length()-".zip".length());
		} else {
			return null;
		}
	}
	
	@Override
	protected DropboxAPI<? extends WebAuthSession> getDropboxAPI() {
		if (dropboxAPI==null) {
			YapbamDropboxSession session = new YapbamDropboxSession();
			String accessKey = Preferences.INSTANCE.getProperty(DROPBOX_ACCESS_KEY);
			String accessSecret = Preferences.INSTANCE.getProperty(DROPBOX_ACCESS_SECRET);
			if (accessKey!=null || accessSecret!=null) {
				session.setAccessTokenPair(new AccessTokenPair(accessKey, accessSecret));
			}
			dropboxAPI = new DropboxAPI<YapbamDropboxSession>(session);
		}
		return dropboxAPI;
	}

	@Override
	protected boolean accessGranted(AccessTokenPair pair) {
		if (pair!=null) {
			Preferences.INSTANCE.setProperty(DROPBOX_ACCESS_KEY, pair.key);
			Preferences.INSTANCE.setProperty(DROPBOX_ACCESS_SECRET, pair.secret);
		}
		return super.accessGranted(pair);
	}

	@Override
	protected void clearAccess() {
		Preferences.INSTANCE.removeProperty(DROPBOX_ACCESS_KEY);
		Preferences.INSTANCE.removeProperty(DROPBOX_ACCESS_SECRET);
		dropboxAPI = null;
	}
}
