package controllers;

import java.util.Date;
import java.util.List;

import models.Image;
import models.Post;
import play.Logger;
import play.data.Upload;
import play.mvc.Controller;
import play.mvc.With;
import security.CheckIsAdmin;
import security.Secure;

import com.google.appengine.api.datastore.Blob;

@With(Secure.class)
@CheckIsAdmin("isAdmin")
public class Admin extends Controller {

	
	public static void index() {
		List<Post> posts = Post.all().fetch();
		render(posts);
	}
	
	
	public static void upload(Upload file, long postId){
		Image image = new Image(new Blob(file.asBytes()), postId, file.getFileName());
		image.insert();
		index();
		
	}

	public static void form(Long id) {
		if (id != null) {
			List<Image> images=  Image.allByPostId(id);
			Post post = Post.findById(id);
			render(post, images);
		}
		render();
	}

	public static void deleteImage(long id) {
		Image.findById(id).delete();
		index();
	}
	public static void delete(long id) {
		Post post = Post.findById(id);
		post.delete();
		flash.success("L'article " + id + " a bien ete supprime");
		index();
	}

	@SuppressWarnings("deprecation")
	public static void save(Long id, String title, String chapeau,String url, String content, String postedAt, boolean published) {
		Logger.info("Save id = %s, title = %s, content = %s, postedAt = %s", id, title, content, postedAt);
		if (id == null) {
			Post post = new Post(title, chapeau, url, content);
			post.insert();
			flash.success("L'article " + post.id + " a bien ete ajoute");
		} else {
			Post post = Post.findById(id);
			post.content = content;
			post.title = title;
			post.chapeau = chapeau;
			post.url = url;
			post.published=published;
			post.postedAt = new Date(postedAt);
			post.update();
			flash.success("L'article " + post.id + " a bien ete mis a jour");
		}

		index();
	}

}