package controllers;

import java.io.ByteArrayInputStream;
import java.util.List;

import models.Image;
import models.Post;

import org.joda.time.LocalDate;

import play.mvc.Controller;

public class Application extends Controller {

    public static void index() {
        List<Post> posts = Post.allPublished().fetch(10);
        render(posts);
    }

    public static void showById(long id) {
        Post post = Post.findById(id);
        render("@show", post);
    }

    public static void showByDateAndUrl(int annee, int mois, int jour, String url) {
        Post post = Post.findByURL(url);
        if (post == null) {
            flash.error("Cet article n'existe pas.");
            notFound("Cet article n'existe pas.");
        }
        // on verifie que la date de l'url est bien celle de l'article
        LocalDate postDate = LocalDate.fromDateFields(post.postedAt);
        LocalDate urlDate = new LocalDate(annee, mois, jour);
        if (postDate.compareTo(urlDate) != 0) {
            flash.error("Cette url n'éxiste pas.");
            notFound("Cette url n'éxiste pas.");
        }
        render("@show", post);
    }

    public static void show(Post post) {
        render(post);
    }

    public static void fileContent(String name) {
        Image image = Image.findByName(name);
        ByteArrayInputStream bis = new ByteArrayInputStream(image.data.getBytes());
        renderBinary(bis, name);
    }

}