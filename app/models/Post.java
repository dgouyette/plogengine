package models;

import java.util.Date;

import com.google.appengine.api.users.UserServiceFactory;

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
    
    public Long hits=0L;
    
    
    
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
    	Post post = Model.all(Post.class).filter("id", id).get();
        return post;
    }

    public static Query<Post> all() {
        return Model.all(Post.class).order("-postedAt");
    }

    public static Query<Post> allPublished() {
        return Model.all(Post.class).order("-postedAt").filter("published", true);
    }
    

    public static Post findByURL(String url) {
    	Post post= Model.all(Post.class).filter("url", url).get();
    	//On incremente les hits uniquement si ce n'est pas un utilisateur qui affiche la page
    	if (!UserServiceFactory.getUserService().isUserLoggedIn()){
	    		System.out.println(post.hits);
	    		if (post.hits==null){
	    			post.hits=0L;
	    		}
	    		post.hits=post.hits+1;
	    		post.update();
    	}
        return post;
    }
    
   

}