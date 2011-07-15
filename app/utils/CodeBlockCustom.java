package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.eclipse.mylyn.internal.wikitext.textile.core.Textile;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.markup.Block;

public class CodeBlockCustom extends Block {
	
	private static final int LINE_REMAINDER_GROUP_OFFSET = Textile.ATTRIBUTES_BLOCK_GROUP_COUNT + 2;

	private static final int EXTENDED_GROUP = Textile.ATTRIBUTES_BLOCK_GROUP_COUNT + 1;

	static final Pattern startPattern = Pattern.compile("bc" + Textile.REGEX_BLOCK_ATTRIBUTES + "\\.(\\.)?\\s+(.*)"); //$NON-NLS-1$ //$NON-NLS-2$

	private boolean extended;

	private int blockLineCount = 0;

	private Matcher matcher;

	public CodeBlockCustom() {
	}

	@Override
	public int processLineContent(String line, int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			

			Textile.configureAttributes(attributes, matcher, 1, true);
			offset = matcher.start(LINE_REMAINDER_GROUP_OFFSET);
			extended = matcher.group(EXTENDED_GROUP) != null;
			
			attributes.setCssClass("sh_"+attributes.getCssClass());

			builder.beginBlock(BlockType.PREFORMATTED, attributes);
			
			builder.beginBlock(BlockType.CODE, attributes);
		}
		if (markupLanguage.isEmptyLine(line) && !extended) {
			setClosed(true);
			return 0;
		} else if (extended && Textile.explicitBlockBegins(line, offset)) {
			setClosed(true);
			return offset;
		}
		++blockLineCount;

		final String lineText = offset > 0 ? line.substring(offset) : line;
		if (blockLineCount > 1 || lineText.trim().length() > 0) {
			builder.characters(lineText);
			builder.characters("\n"); //$NON-NLS-1$
		}

		return -1;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		if (lineOffset == 0) {
			matcher = startPattern.matcher(line);
			return matcher.matches();
		} else {
			matcher = null;
			return false;
		}
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			builder.endBlock();// code
			builder.endBlock();// pre
		}
		super.setClosed(closed);
	}

}
