package controllers;

import models.*;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.Upload;
import play.data.validation.Valid;
import play.libs.OpenID;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.List;
import java.util.Map;

public class Admin extends Controller {

    @Before(unless = {"login", "authenticateOpenId"})
    static void checkAuthenticated() {
        Logger.info("checkAuthenticate : " + Play.mode);
        if (Play.mode == Play.Mode.DEV)
            session.put("user", "dev");
        if (!session.contains("user")) {
            login();
        }
    }

    public static boolean logged() {
        return session != null && session.contains("user");
    }

    public static void login() {
        render();
    }

    public static void logout() {
        session.remove("user");
        login();
    }

    public static void authenticateOpenId(String action,
                                          String openid_identifier) {
        if (OpenID.isAuthenticationResponse()) {
            OpenID.UserInfo verifiedUser = OpenID.getVerifiedID();
            Map<String, String> params = verifiedUser.extensions;

            String userEmail = verifiedUser.extensions.get("email");
            if (userEmail == null) {
                flash.error("L'identification de votre compte sur le site  s'effectue avec votre email."
                        + " Vous devez authoriser le site à accéder à votre email pour vous authentifier.");
                login();
            }

            User user = User.findByMail(userEmail);
            if (user == null) {
                flash.error("Désolé votre compte n'existe pas. Demandez à l'équipe d'ajouter votre email "
                        + userEmail
                        + " pour pouvoir vous authentifier avec ce compte.");
                login();
            } else {
                session.put("user", userEmail);
                index();
            }

        } else {
            if (!OpenID.id(openid_identifier)
                    .required("email", "http://axschema.org/contact/email")
                    .verify()) {
                flash.error("Cannot verify your OpenID");
                login();
            }
        }
    }

    public static void index() {
        List<Post> posts = Post.find("order by postedAt desc").fetch();
        List<Image> images = Image.all().fetch();
        int nbImage = images.size();
        render(posts, images, nbImage);
    }

    public static void cachereset() {
        flash.success("Le cache a ete efface");
        Cache.clear();
        index();
    }

    public static void add() {
        render("@form");
    }

    public static void deleteImage(Long id) {
        Image.findById(id)._delete();
        flash.success("L'image " + id + " a été supprimée");
        index();
    }

    public static void edit(Long id) {
        form(id);
    }

    public static void form(Long id) {
        if (id != null) {
            List<Image> images = Image.find("postId", id).fetch();
            Post post = Post.findById(id);
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
        Image image = new Image(upload.asBytes(), postId, upload.getFileName());
        image.save();
        index();

    }

    public static void deleteImage(long id) {
        Image.delete("id", id);
        index();
    }



    @SuppressWarnings("deprecation")
    public static void save(@Valid Post post) {
        post.save();
        flash.success("L'article " + post.id + " a bien ete sauvegarde");
        index();
    }


    public static void listSearch() {
        renderJSON(Recherche.findAll());
    }

    public static void reindex() throws Exception {
        //Search.rebuildAllIndexes ();
        flash.success("Reindexation en cours");
        index();
    }

}
