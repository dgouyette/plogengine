package models;

import java.util.List;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Table;

import com.google.appengine.api.datastore.Blob;

@Table("images")
public class Image extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	public Blob data;
	
	public long postId;
	
	public String contentType;
	
	public String fileName;
	
	
	public Image(){
	}
	
	
	public static Image findById(long id){
		return Model.all(Image.class).filter("id", id).get();
	}
	
	public static List<Image> findByPostId(long id){
		return Model.all(Image.class).filter("postId", id).fetch();
	}
	
	
	
	public static Image findByName(String fileName){
		return Model.all(Image.class).filter("fileName", fileName).get();
	}
	
	public static List<Image> allByPostId(long postId) {
		return Model.all(Image.class).filter("postId", postId).fetch();
	}

	public Image(Blob data, long postId, String fileName) {
		super();
		this.data = data;
		this.postId = postId;
		this.fileName = fileName;
	}
	

}
