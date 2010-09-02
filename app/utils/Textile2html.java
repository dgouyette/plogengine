package utils;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;

import play.Logger;

public class Textile2html {

    public static String parse(String textile) {
        MarkupLanguage language = new TextileLanguageCustom();
        MarkupParser markupParser = new MarkupParser();
        markupParser.setMarkupLanguage(language);
        String htmlContent = "Erreur lors de la conversion textile";
        try {
            htmlContent = markupParser.parseToHtml(textile);
        } catch (Exception e) {
            Logger.error("Erreur lors de la conversion textile to html textile = %s, erreur %s ",textile, e.getCause());
        }
        return htmlContent;
    }

}
