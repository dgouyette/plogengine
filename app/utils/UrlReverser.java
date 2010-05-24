package utils;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.JodaTimePermission;

import models.Post;
import play.mvc.Router;

public class UrlReverser {

	@SuppressWarnings("deprecation")
	//convertir un post en url frendly
	static String parse(Post post) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("annee", post.postedAt.getYear()+1900);
		String mois = String.valueOf(post.postedAt.getMonth()+1);
		if (mois.length()<2){
			mois="0"+mois;
		}
		params.put("mois", mois);
		String jour = String.valueOf(post.postedAt.getDate());
		if (jour.length()<2){
			jour ="0"+jour;
		}
		params.put("jour", jour);
		params.put("url", post.url);
		String url = Router.reverse("Application.show", params).url;
		return url;
	}
}
