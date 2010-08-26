package controllers;

import java.io.ByteArrayInputStream;
import java.util.List;

import models.Image;
import models.Post;
import play.mvc.Controller;

public class Application extends Controller {

    public static void index() {
        List<Post> posts = Post.allPublished().fetch();
        render(posts);
    }

    public static void showById(long id) {
        System.out.println("findById");
        Post post = Post.findById(id);
        render("@show", post);
    }

    public static void showByDateAndUrl(long annee, long mois, long jour, String url) {
        Post post = Post.findByURL(url);
        if (post == null) {
            flash.error("Article non trouve");
            index();
        }
        render("@show", post);
    }

    public static void show(Post post) {
        render(post);
    }

    public static void showJson(String url) {
        Post post = Post.findByURL(url);
        if (post == null) {
            flash.error("Article non trouve");
            index();
        }
        renderJSON(post);
    }

    public static void fileContent(String name) {
        Image image = Image.findByName(name);
        ByteArrayInputStream bis = new ByteArrayInputStream(image.data.getBytes());
        renderBinary(bis, name);
    }

}