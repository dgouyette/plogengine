package controllers;

import java.io.ByteArrayInputStream;
import java.util.List;

import models.Image;
import models.Post;
import play.modules.search.Query;
import play.modules.search.Search;
import play.mvc.Controller;

public class Application extends Controller {

	public static void index() {
		List<Post> posts = Post.find("published=true order by postedAt desc")
				.fetch(10);
		render(posts);
	}

	public static void showById(long id) {
		Post post = Post.findById(id);
		render("@show", post);
	}

	public static void showByDateAndUrl(int annee, int mois, int jour,String url) {

		Post post = Post.findByURL(url, !session.contains("user"));

		if (post == null) {
			flash.error("Cet article n'existe pas.");
			notFound("Cet article n'existe pas.");
		}
		render("@show", post);
	}

	public static void showByUrl(String url) {
		Post post = Post.findByURL(url, !session.contains("user"));

		if (post == null) {
			flash.error("Cet article n'existe pas.");
			notFound("Cet article n'existe pas.");
		}
		render("@show", post);
	}
	
	public static void search(String search){
		if (!search.isEmpty() && search!=null){
			Query q = Search.search("title:"+search+" OR content:"+search+" OR chapeau:"+search, Post.class);
			List<Post> posts = q.fetch();
			render(posts, search);
		}
		else{
			render();
		}
	}

	
	public static void show(Post post) {
		render(post);
	}

	public static void fileContent(String name) {
		Image image = Image.find("fileName", name).first();
		if (image == null) {
			notFound();
		}
		renderBinary(new ByteArrayInputStream(image.data));
	}

}