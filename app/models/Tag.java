package models;

import play.db.jpa.Model;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;



@Entity
public class Tag extends Model {
	
	public String name;
	
	@ElementCollection
	public List<Long> postIds= new ArrayList<Long>(); 
	
	public Tag(String name){
		this.name = name;
	}
	
	public String toString(){
		return name;
	}
	
//	public static Tag findOrCreateByName(String name) {
//		Tag tag = Model.all(Tag.class).filter("name", name).get();
//	    if(tag == null) {
//	        tag = new Tag(name);
//	    }
//	    return tag;
//	}
	
	public void addTagToPost(Long postId){
		postIds.add(postId);
	}
	
//	public static List<Tag> findAll() {
//		List<Tag> tags = Model.all(Tag.class).fetch();
//		for (Tag tag : tags){
//			Logger.info("Tag.id %s tag.name %s", tag.id , tag.name);
//		}
//		return tags;
//	}
	

//	public static List<Tag> findTagsByPostId(Long id) {
//		List<Tag> tags = Model.all(Tag.class).filter("postIds", id).fetch();
//		for (Tag tag : tags){
//			Logger.info("Tag.id %s tag.name %s", tag.id , tag.name);
//		}
//		return tags;
//	}
	

}
