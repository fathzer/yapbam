package net.yapbam.update;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import net.yapbam.ihm.LocalizationData;

public class ReleaseInfo implements Comparable<ReleaseInfo> {
	private int majorRevision;
	private int minorRevision;
	private int buildId;
	private Date releaseDate;
	
	ReleaseInfo(String rel) {
		StringTokenizer tokens = new StringTokenizer(rel, ".");
		majorRevision = Integer.parseInt(tokens.nextToken());
		minorRevision = Integer.parseInt(tokens.nextToken());
		tokens = new StringTokenizer(tokens.nextToken()," ()/");
		buildId = Integer.parseInt(tokens.nextToken());
		try {
			int dayOfMonth = Integer.parseInt(tokens.nextToken());
			int month = Integer.parseInt(tokens.nextToken());
			int year = Integer.parseInt(tokens.nextToken());
			releaseDate = new GregorianCalendar(year, month-1, dayOfMonth).getTime();
		} catch (NumberFormatException e) {
			releaseDate = new Date(Long.MAX_VALUE);
		}
	}
	
	public int getMajorRevision() {
		return majorRevision;
	}
	
	public int getMinorRevision() {
		return minorRevision;
	}
	
	public int getBuildId() {
		return buildId;
	}
	
	public Date getReleaseDate() {
		return releaseDate;
	}

	@Override
	public int compareTo(ReleaseInfo o) {
		int result = majorRevision - o.majorRevision;
		if (result == 0) result = minorRevision - o.minorRevision; 
		if (result == 0) result = buildId - o.buildId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return this.compareTo((ReleaseInfo) obj)==0;
	}

	@Override
	public int hashCode() {
		return majorRevision*100 + minorRevision*100 + buildId;
	}

	@Override
	public String toString() {
		return majorRevision+"."+minorRevision+"."+buildId+" ("+
			SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, LocalizationData.getLocale()).format(releaseDate)+")";
	}
}
