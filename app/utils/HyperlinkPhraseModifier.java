package utils;

import org.eclipse.mylyn.internal.wikitext.textile.core.Textile;
import org.eclipse.mylyn.internal.wikitext.textile.core.TextileContentState;
import org.eclipse.mylyn.wikitext.core.parser.ImageAttributes;
import org.eclipse.mylyn.wikitext.core.parser.LinkAttributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;

/**
 * @author David Green
 * @since 1.2
 * @see SpanType#LINK
 */
public class HyperlinkPhraseModifier extends PatternBasedElement {

	protected static final int ATTRIBUTES_OFFSET = 2;

	@Override
	protected String getPattern(int groupOffset) {
		return "(?:(\")" + Textile.REGEX_ATTRIBUTES + "([^\"]+)\\" + (1 + groupOffset) + ":([^\\s]*[^\\s!.)(,:;]))"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	protected int getPatternGroupCount() {
		return 3 + Textile.ATTRIBUTES_GROUP_COUNT;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new HyperlinkProcessor();
	}

	private static class HyperlinkProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String hyperlinkBoundaryText = group(1);
			String hyperlinkSrc = group(2 + Textile.ATTRIBUTES_GROUP_COUNT);
			String href = group(3 + Textile.ATTRIBUTES_GROUP_COUNT);
			String namedLinkUrl = ((TextileContentState) getState()).getNamedLinkUrl(href);
			if (namedLinkUrl != null) {
				href = namedLinkUrl;
			}

			if (hyperlinkBoundaryText.equals("\"")) { //$NON-NLS-1$
				LinkAttributes attributes = new LinkAttributes();

				//attributes.setHref(href);
				Textile.configureAttributes(this, attributes, ATTRIBUTES_OFFSET, false);
				//builder.beginSpan(SpanType.LINK, attributes);
				getMarkupLanguage().emitMarkupLine(parser, state, start(2), hyperlinkSrc, 0);
				builder.endSpan();
			} else {
				final ImageAttributes attributes = new ImageAttributes();
				Textile.configureAttributes(this, attributes, ATTRIBUTES_OFFSET, false);
				builder.imageLink(attributes, href, hyperlinkSrc);
			}
		}
	}

}
