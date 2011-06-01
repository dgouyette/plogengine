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
