package controllers;

import java.io.ByteArrayInputStream;
import java.util.List;

import models.Image;
import models.Post;
import play.mvc.Controller;

public class Application extends Controller {

	public static void index() {
		List<Post> posts = Post.all().fetch();
		render(posts);
	}
	
	
	public static void show(String url){
		Post post = Post.findByURL(url);
		if (post==null){
			flash.error("Article non trouve");
			index();
		}
		render(post);
	}
	
	public static void showJson(String url){
		Post post = Post.findByURL(url);
		if (post==null){
			flash.error("Article non trouve");
			index();
		}
		renderJSON(post);
	}
	
	
	
	
	public static void show(long  id){
		System.out.println("findById");
		Post post = Post.findById(id);
		render(post);
	}
	
	
	
	public static void fileContent(String name){
		Image image = Image.findByName(name);
		ByteArrayInputStream bis = new ByteArrayInputStream(image.data.getBytes());
		renderBinary(bis, name);
	}

}