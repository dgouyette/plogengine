package controllers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Image;
import models.ImageBackup;
import models.Post;
import models.PostBackup;

import org.apache.commons.lang.StringEscapeUtils;

import play.Logger;
import play.data.Upload;
import play.libs.Codec;
import play.mvc.Controller;
import utils.DateDeserializer;
import utils.DateSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Backup extends Controller {

	/**
	 * Permet de recuperer un gsonbuilder associe aux Serializer/Deserializer de
	 * date pour json
	 */
	private static Gson getGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat(DateFormat.SHORT);
		gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer());
		gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
		Gson gson = gsonBuilder.create();
		return gson;
	}

	/**
	 * Convertit un post (objet) en json et le renvoie au navigateur
	 * @throws IOException 
	 */
	public static void backup(long id) throws IOException {
		PostBackup postToBackup = new PostBackup();

		Logger.debug("backup : %s", id);
		postToBackup.post = Post.findById(id);

		if (postToBackup.post == null) {
			notFound();
		} else {
			List<Image> images = Image.find("id", id).fetch();
			List<ImageBackup> imageBackups = new ArrayList<ImageBackup>();

			Logger.info("postId %s nbImage : %s", id, images.size());
			for (Image image : images) {
				imageBackups.add(new ImageBackup(image.fileName,Codec.encodeBASE64(image.data), image.postId ));
			}
			postToBackup.images = imageBackups;
			renderText(getGson().toJson(postToBackup));
		}

	}

	/**
	 * Recupere un json et le converti en objet Post, et fait les update ou
	 * creation en base.
	 * 
	 * @param file
	 * @throws Base64DecoderException
	 */
	public static void restore(Upload file) {
		Logger.info("restore size %s: ", file.getSize());

		InputStreamReader reader = new InputStreamReader(file.asStream());
		PostBackup postToRestore = getGson().fromJson(reader, PostBackup.class);

		Logger.info("Restauration du post = %s", postToRestore.post.title);

		// y'a til un post existant ?
		Post postexistant = Post.findById(postToRestore.post.id);

		if (postexistant != null) {
			Logger.info("Le post n° %s existe deja,on l'update",postexistant.id);
			postexistant = postToRestore.post;

			List<ImageBackup> imagesToRestore = postToRestore.images;

			// Sauvegarde l'image, meme si elle existe deja
			for (ImageBackup imageToRestore : imagesToRestore) {
				Image image = new Image();
				image.data =Codec.decodeBASE64((imageToRestore.dataBase64));
				image.postId = postexistant.id;
				image.save();
			}

			// On update le post que si le restaure des images a fonctionné
			postexistant.save();

		} else {
			Logger.info("Le post n'existe pas, on le cree");
			Post postToCreate = postToRestore.post;
			
			postToCreate.title = StringEscapeUtils.unescapeHtml(postToCreate.title);
			postToCreate.content = StringEscapeUtils.unescapeHtml(postToCreate.content);
			postToCreate.chapeau = StringEscapeUtils.unescapeHtml(postToCreate.chapeau);
			
			Logger.info("L'id du post est %s", postToCreate.id);

			List<ImageBackup> imagesToRestore = postToRestore.images;

			// Sauvegarde l'image, meme si elle existe deja
			for (ImageBackup imageToRestore : imagesToRestore) {
				Image image = new Image();
				image.data =Codec.decodeBASE64((imageToRestore.dataBase64));
				image.fileName = imageToRestore.fileName;
				image.postId = postToCreate.id;
				image.save();
			}
			
			postToCreate.merge();
			
			
			
			
			

		}

		Admin.index();

	}

}
