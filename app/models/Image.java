package models;

import javax.persistence.Entity;
import javax.persistence.Lob;

import play.db.jpa.Model;

@Entity
public class Image extends Model {

	@Lob
	public byte[] data;

	public long postId;

	public String contentType;

	public String fileName;

	public Image() {
	}

	// public static Image findById(long id){
	//
	// return Model.all(Image.class).filter("id", id).get();
	// }

	// public static List<Image> findByPostId(long id){
	// return Model.all(Image.class).filter("postId", id).fetch();
	// }
	//

	// public static Image findByName(String fileName){
	// return Model.all(Image.class).filter("fileName", fileName).get();
	// }

	// public static List<Image> allByPostId(long postId) {
	// return Model.all(Image.class).filter("postId", postId).fetch();
	// }

	@Override
	public String toString() {
		return "[fileName : " + fileName + ", postId: " + postId + "]";
	}

	public Image(byte[] data, long postId, String fileName) {
		super();
		this.data = data;
		this.postId = postId;
		this.fileName = fileName;
	}

}
