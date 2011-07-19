package models;

import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.Date;

@Indexed
@Entity
public class Post extends Model {

    @Field
    public String title;

    public String url;

    @Lob
    @Field
    public String chapeau;

    @Field
    public Boolean published = false;

    public Date postedAt = new Date();

    @Lob
    @Field
    public String content;

    public Long hits = 0L;

    public Post() {

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
        return "id= " + getId() + ", title  = " + title + ", content = " + content;
    }


    public static Post findByURL(String url, boolean incremente) {

        Post post = Post.find("url", url).first();
        // //On incremente les hits uniquement si ce n'est pas un utilisateur
        // qui
        // affiche la page

        if (null == post) {
            return null;
        }

        if (incremente) {
            if (post.hits == null) {
                post.hits = 0L;
            }

            post.hits = post.hits + 1;
            post.save();
        }
        return post;
    }

    public Post previous() {
        return Post.find("postedAt < ? and published=true order by postedAt desc", postedAt).first();
    }

    public Post next() {
        return Post.find("postedAt > ? and published=true order by postedAt asc", postedAt).first();
    }


}