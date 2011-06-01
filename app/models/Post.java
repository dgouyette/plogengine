package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;

import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

@Indexed
@Entity
public class Post extends Model {

	@Field
	public String title;

	public String url;

	@Lob
	@Field
	public String chapeau;

	public Boolean published = false;

	@Field
	public Date postedAt = new Date();

	@Lob
	@Field
	public String content;

	public Long hits = 0L;

	public Post() {

	}

	public Post(String title, String chapeau, String url, String content,
			Boolean published) {
		this.title = title;
		this.url = url;
		this.content = content;
		this.chapeau = chapeau;
		this.published = published;
	}

	@Override
	public String toString() {
		return "id= " + getId() + ", title  = " + title + ", content = "
				+ content;
	}

	// public static Post findById(Long id) {
	// Post post = Model.all(Post.class).filter("id", id).get();
	// return post;
	// }

	// public static Query<Post> all() {
	// return Model.all(Post.class).order("-postedAt");
	// }

	// public static Query<Post> allPublished() {
	// return Model.all(Post.class).order("-postedAt").filter("published",
	// true);
	// }

	public static Post findByURL(String url, boolean incremente) {

		Post post = Post.find("url", url).first();
		// //On incremente les hits uniquement si ce n'est pas un utilisateur
		// qui
		// affiche la page

		if (incremente) {
			if (post.hits == null) {
				post.hits = 0L;
			}

			post.hits = post.hits + 1;
			post.save();
		}
		return post;
	}

}