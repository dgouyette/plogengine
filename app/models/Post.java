package models;

import java.util.Date;

import siena.DateTime;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.Table;

@Table("posts")
public class Post extends Model {

    @Id(Generator.AUTO_INCREMENT)
    public Long    id;

    public String  title;

    public String  url;

    public String  chapeau;

    public Boolean published = false;

    @DateTime
    public Date    postedAt  = new Date();

    public String  content;
    
    
    public Post(){
    	
    }

    public Post(String title, String chapeau, String url, String content, Boolean published) {
        this.title = title;
        this.url = url;
        this.content = content;
        this.chapeau = chapeau;
        this.published = published;
    }

    @Override
    public String toString() {
        return "title  = " + title + ", content = " + content;
    }

    public static Post findById(Long id) {
        return Model.all(Post.class).filter("id", id).get();
    }

    public static Query<Post> all() {
        return Model.all(Post.class).order("-postedAt");
    }

    public static Query<Post> allPublished() {
        return Model.all(Post.class).order("-postedAt").filter("published", true);
    }

    public static Post findByURL(String url) {
        return Model.all(Post.class).filter("url", url).get();
    }

}