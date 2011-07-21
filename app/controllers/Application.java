package controllers;

import models.Image;
import models.Post;
import models.Recherche;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import play.Logger;
import play.modules.search.Query;
import play.modules.search.Search;
import play.mvc.Controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class Application extends Controller {

    public static void index(Long page) {

        int pageSize = 10;
        if (page == null)
            page = 0L;
        List<Post> posts;

        int start = (int) (0 + page * 10);
        posts = Post.find("published=true order by postedAt desc").from(start).fetch(pageSize);

        long count = Post.count("published=true");

        render(posts, count, page, pageSize);
    }

    public static void showById(long id) {
        Post post = Post.findById(id);
        render("@show", post);
    }

    public static void showByDateAndUrl(int annee, int mois, int jour, String url) {
        List<Post> posts = Post.find("published=true order by postedAt desc").from(0).fetch(5);

        Post post = Post.findByURL(url, !session.contains("user"));
        if (post == null) {
            notFound("Cet article n'existe pas.");
        }
        render("@show", post, posts);
    }


    /**
     * Permet d'extraire les termes les plus courant utilises dans les articles et les mets dans une map tries afin d'afficher les termes les plus courants
     * @throws IOException
     */
    public static void indexSearch() throws IOException {
        IndexReader indexReader = Search.getCurrentStore().getIndexSearcher("models.Post").getIndexReader();
        TermEnum terms = indexReader.terms();

        final List<String> termList = new ArrayList<String>();

        final Map<String, Integer> frequencyMap = new HashMap<String, Integer>();

        while (terms.next()) {
            Term term = terms.term();
            String termText = term.text();
            int frequency = indexReader.docFreq(term);
            frequencyMap.put(termText, frequency);
            termList.add(termText);
        }

        List list = new LinkedList(frequencyMap.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });


        Map sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }


        renderJSON(sortedMap);
    }


    public static void search(String search) {
        Logger.info("search %s", search);
        if (!search.isEmpty() && search != null) {
            Recherche recherche = new Recherche();
            recherche.keywords = search;
            recherche.save();
            Query q = Search.search("(title:" + search + " OR content:" + search + " OR chapeau:" + search +") AND published:true", Post.class);
            List<Post> posts = q.fetch();
            render(posts, search);
        } else {
            render();
        }
    }

    public static void showByUrl(String url) {
        Post post = Post.findByURL(url, !session.contains("user"));

        if (post == null) {
            flash.error("Cet article n'existe pas.");
            notFound("Cet article n'existe pas.");
        }
        render("@show", post);
    }


    public static void show(Post post) {
        render(post);
    }

    public static void fileContent(String name) {
        Image image = Image.find("fileName", name).first();
        if (image == null) {
            notFound();
        }
        renderBinary(new ByteArrayInputStream(image.data));
    }

}