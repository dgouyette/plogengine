package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Lob;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Table;

@Table("comments")
public class Comment extends Model {
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
 
    @Required
    public String author;
    
    @Required
    public Date postedAt;
     
    @Lob
    @Required
    @MaxSize(10000)
    public String content;
    
    @Required
    public Post post;
    
    public Comment(Post post, String author, String content) {
        this.post = post;
        this.author = author;
        this.content = content;
        this.postedAt = new Date();
    }
    
    public String toString() {
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
    
    public static List<Post> last5() {
		return Model.all(Post.class).fetch(0, 4);
	}
 
}