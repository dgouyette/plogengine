package models;

import java.util.Date;
import java.util.List;

import siena.Column;
import siena.DateTime;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.Table;

@Table("posts")
public class Post extends Model {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;

	@Column("title")
	public String title;

	@Column("url")
	public String url;

	@Column("chapeau")
	public String chapeau;

	public Boolean published=false;

	@DateTime
	public Date postedAt = new Date();

	@Column("content")
	public String content;

	@Column("comments")
	public List<Comment> comments;

	public Post(String title, String chapeau, String url, String content) {
		this.title = title;
		this.url = url;
		this.content = content;
		this.chapeau = chapeau;
		this.published = false;
	}

	public String toString() {
		return "title  = " + title + ", content = " + content;
	}

	public static Post findById(Long id) {
		return Model.all(Post.class).filter("id", id).get();
	}

	public static Query<Post> all() {
		return Model.all(Post.class).order("-postedAt");
	}

	public static Post findByURL(String url) {
		return Model.all(Post.class).filter("url", url).get();
	}

}