package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Post;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.modules.gae.GAE;
import play.mvc.Controller;
import play.ns.com.jhlabs.image.PlasmaFilter;
import utils.Textile2html;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

public class Feeds extends Controller {

	public final static String FEEDS = "FEEDS";

	public static void index() throws FeedException {

		SyndFeedImpl feed = Cache.get(FEEDS, SyndFeedImpl.class);

		if (feed == null) {
			feed = new SyndFeedImpl();
			feed.setFeedType("rss_2.0");
			feed.setTitle("CestPasDur.com, flux RSS");
			feed.setLink("http://www.cestpasdur.com");
			feed.setDescription("Tutoriaux et ressources du web");

			List<Post> posts = Post.allPublished().fetch();
			List<SyndEntry> entries = new ArrayList<SyndEntry>();

			for (Post post : posts) {
				SyndEntry entry = new SyndEntryImpl();
				entry.setTitle(post.title);
				entry.setLink(post.url);
				entry.setPublishedDate(post.postedAt);
				entries.add(entry);

				SyndContent description = new SyndContentImpl();
				description.setType("text/html");
				description.setValue(Textile2html.parse(post.chapeau) + " ...");
				entry.setDescription(description);
				entry.setUri(post.url);

				// TODO déplacer la definition de la duree dans le fichier de
				// configuration ave Play.configuration.
				
			}
			feed.setEntries(entries);
			Cache.set(FEEDS, feed, "24h");			
		} else {
			Logger.info("Utilisation de la version cachee des feeds", "");
		}

		SyndFeedOutput output = new SyndFeedOutput();
		renderXml(output.outputString(feed, true));
	}

}
