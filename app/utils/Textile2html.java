package utils;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;

public class Textile2html {

    public static String parse(String textile) {
        MarkupLanguage language = new TextileLanguageCustom();
        MarkupParser markupParser = new MarkupParser();
        markupParser.setMarkupLanguage(language);
        String htmlContent = null;
        try {
            htmlContent = markupParser.parseToHtml(textile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlContent;
    }

}
