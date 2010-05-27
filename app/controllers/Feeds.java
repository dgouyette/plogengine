package controllers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

import models.Post;
import play.mvc.Controller;
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
	public static void index() throws FeedException {

		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setTitle("CestPasDur.com, flux RSS");
		feed.setLink("http://www.cestpasdur.com");
		feed.setDescription("Tutoriaux et ressources du web");
		
		List<Post> posts = Post.allPublished().fetch();
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		
		for( Post post : posts){
			SyndEntry entry = new SyndEntryImpl();
	        entry.setTitle(post.title);
	        entry.setLink(post.url);
	        entry.setPublishedDate(post.postedAt);
	        entries.add(entry);
	        
	        SyndContent description = new SyndContentImpl();
	        description.setType("text/html");
	        description.setValue(Textile2html.parse(post.chapeau));
	        entry.setDescription(description);
		}
		
		feed.setEntries(entries);
		
		SyndFeedOutput output = new SyndFeedOutput();
		renderXml(output.outputString(feed, true));
	}

}
