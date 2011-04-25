package controllers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import models.Image;
import models.Post;
import models.Tag;
import models.User;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import play.Logger;
import play.cache.Cache;
import play.data.Upload;
import play.libs.OpenID;
import play.mvc.Before;
import play.mvc.Controller;

public class Admin extends Controller {

	@Before(unless = { "login", "authenticateOpenId" })
	static void checkAuthenticated() {
		if (!session.contains("user")) {
			login();
		}
	}
	
	public static boolean logged(){
		return session.contains("user");
	}

	public static void login() {
		render();
	}
	
	public static void logout() {
        session.remove("user");
        login();
    }

	public static void authenticateOpenId(String action, String openid_identifier) {
		if (OpenID.isAuthenticationResponse()) {
            OpenID.UserInfo verifiedUser = OpenID.getVerifiedID();
            Map<String, String> params = verifiedUser.extensions;
             
            String userEmail = verifiedUser.extensions.get("email");
            if (userEmail == null) {
                flash.error("L'identification de votre compte sur le site  s'effectue avec votre email." +
                        " Vous devez authoriser le site à accéder à votre email pour vous authentifier."
                );
                login();
            }
            
            User user = User.findByMail(userEmail);
            if (user==null){
            	flash.error("Désolé votre compte n'existe pas. Demandez à l'équipe d'ajouter votre email "
                        + userEmail
                        + " pour pouvoir vous authentifier avec ce compte.");
            	login();
            }else{
            	session.put("user", userEmail);
    			index();
            }
			
		} else {
			if (!OpenID.id(openid_identifier).required("email","http://axschema.org/contact/email").verify()) {
				flash.error("Cannot verify your OpenID");
				login();
			}
		}
	}

	public static void index() {
		List<Post> posts = Post.all().fetch();
		List<Image> images = Image.all().fetch();
		int nbImage = images.size();
		List<Tag> tags = Tag.findAll();
		render(posts, images, nbImage, tags);
	}

	public static void cachereset() {
		flash.success("Le cache a ete efface");
		Cache.clear();
		index();
	}

	public static void add() {
		render("@form");
	}

	public static void edit(Long id) {
		form(id);
	}

	public static void form(Long id) {
		if (id != null) {
			List<Image> images = Image.find("postId", id).fetch();
			Post post = Post.findById(id);
			// List<Tag> tags = Tag.find("postIds", id).fetch();
			// Tag.findAll();
			render(post, images);
		}
		render();
	}

	public static void delete(long id) {
		Post post = Post.findById(id);

		List<Image> imagesToDelete = Image.find("postId", id).fetch();
		for (Image imageToDelete : imagesToDelete) {
			imageToDelete.delete();
		}

		post.delete();
		flash.success("L'article " + id + " a bien ete supprime");
		index();
	}

	public static void upload(Upload upload, long postId) {
		Image image = new Image(upload.asBytes(), postId, upload.getFieldName());
		image.save();
		index();

	}

	public static void deleteImage(long id) {
		Image.delete("id", id);
		index();
	}

	public static void saveTag(@NotNull @NotEmpty String tagName,
			@NotNull @NotEmpty long postId) {
		Logger.info("Ajout du tag %s a l'article id %s", tagName, postId);
		Tag tag = null;// Tag.findOrCreateByName(tagName);
		Logger.info(" avant tag.id %s, tag.name %s, tag.postIds %s", tag.id,
				tag.name, tag.postIds);
		tag.postIds.add(postId);
		if (tag.id == null) {
			tag.save();
		} else {
			tag.save();
		}
		Logger.info("apres  tag.id %s, tag.name %s, tag.postIds %s", tag.id,
				tag.name, tag.postIds);
		form(postId);
	}

	@SuppressWarnings("deprecation")
	public static void save(Long id, @NotNull @NotEmpty String title,
			String chapeau, String url, String content, String postedAt,
			boolean published) {
		Logger.info(
				"Save id = %s, title = %s, content = %s, postedAt = %s, published = %s",
				id, title, content, postedAt, published);
		if (id == null) {
			Post post = new Post(title, chapeau, url, content, published);
			post.save();
			flash.success("L'article " + post.id + " a bien ete ajoute");
		} else {
			Post post = Post.findById(id);
			post.content = content;
			post.title = title;
			post.chapeau = chapeau;
			post.url = url;
			post.published = published;
			post.postedAt = new Date(postedAt);
			post.save();
			flash.success("L'article " + post.id + " a bien ete mis a jour");
		}

		index();
	}

}
