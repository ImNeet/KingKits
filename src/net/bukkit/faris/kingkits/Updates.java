package net.bukkit.faris.kingkits;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Updates {
	private KingKits plugin;
	private URL filesFeed = null;

	private String version = "1.0.0";
	private String link = "http://dev.bukkit.org/server-mods/kingkits/files/1-king-kits-v1-0-0/";

	public Updates(KingKits instance, String rssUrl) {
		this.plugin = instance;
		try {
			this.filesFeed = new URL(rssUrl);
		} catch (Exception ex) {
			this.plugin.getLogger().log(Level.WARNING, "Couldn't make a connection to " + rssUrl + " from Updates.class! Using default KingKits files.rss");
			try {
				this.filesFeed = new URL("http://dev.bukkit.org/server-mods/kingkits/files.rss");
			} catch (Exception ex2) {
				this.plugin.getLogger().log(Level.WARNING, "Couldn't make a new instance of Updates.class! Error Message: " + ex.getMessage());
				this.filesFeed = null;
			}
		}
	}

	public boolean isUpdated() {
		boolean updated = false;
		if (this.filesFeed != null) {
			try {
				InputStream inputStr = null;
				inputStr = this.filesFeed.openConnection().getInputStream();
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStr);

				Node latestFile = document.getElementsByTagName("item").item(0);
				NodeList children = latestFile.getChildNodes();

				String strVersion = children.item(1).getTextContent();
				if (strVersion.toLowerCase().contains("special")) return true;

				this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
				this.link = children.item(3).getTextContent();

				if (this.plugin.getDescription().getVersion().equals(this.version)) updated = true;
				else updated = false;
			} catch (Exception ex) {
				updated = true;
			}
		} else {
			updated = true;
		}
		return updated;
	}

	public String getVersion() {
		return this.version;
	}

	public String getLink() {
		return this.link;
	}

	public void updateVersion() {
		if (this.filesFeed != null) {
			try {
				InputStream inputStr = null;
				inputStr = this.filesFeed.openConnection().getInputStream();
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStr);

				Node latestFile = document.getElementsByTagName("item").item(0);
				NodeList children = latestFile.getChildNodes();

				String strVersion = children.item(1).getTextContent();
				if (strVersion.toLowerCase().contains("special")) return;
				this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
			} catch (Exception ex) {
			}
		}
	}

	public void updateLink() {
		if (this.filesFeed != null) {
			try {
				InputStream inputStr = null;
				inputStr = this.filesFeed.openConnection().getInputStream();
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStr);

				Node latestFile = document.getElementsByTagName("item").item(0);
				NodeList children = latestFile.getChildNodes();

				this.link = children.item(3).getTextContent();
			} catch (Exception ex) {
			}
		}
	}

}
