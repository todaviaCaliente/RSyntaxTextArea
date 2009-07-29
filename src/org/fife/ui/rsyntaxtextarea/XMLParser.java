package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.BadLocationException;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.fife.io.DocumentReader;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;




/**
 * A parser for XML documents.
 *
 * @author Robert Futrell
 * @version 1.0
 */
/*
 * TODO: Figure out why this is buggy...
 */
public class XMLParser implements Parser {

	private SAXParserFactory spf;
	private RSyntaxTextArea textArea;
	private DefaultParseResult result;


	public XMLParser(RSyntaxTextArea textArea) {
		this.textArea = textArea;
		result = new DefaultParseResult(this);
		try {
			spf = SAXParserFactory.newInstance();
		} catch (FactoryConfigurationError fce) {
			fce.printStackTrace();
		}
	}


	public ParseResult parse(RSyntaxDocument doc, String style) {

		result.clearNotices();

		if (spf==null) {
			return result;
		}

		try {
			SAXParser sp = spf.newSAXParser();
			Handler handler = new Handler();
			DocumentReader r = new DocumentReader(doc);
			InputSource input = new InputSource(r);
			sp.parse(input, handler);
			r.close();
		} catch (SAXParseException spe) {
			// A fatal parse error - ignore; a ParserNotice was already created.
		} catch (Exception e) {
			e.printStackTrace();
			result.addNotice(new ParserNotice(this,
					"Error parsing XML: " + e.getMessage(), 0, -1, -1));
		}

		return result;

	}


	private class Handler extends DefaultHandler {

		private void doError(SAXParseException e) {
			int line = e.getLineNumber() - 1;
			try {
				int offs = textArea.getLineStartOffset(line);
				int len = textArea.getLineEndOffset(line) - offs + 1;
				ParserNotice pn = new ParserNotice(XMLParser.this,
											e.getMessage(), line, offs, len);
				result.addNotice(pn);
				System.err.println(">>> " + offs + "-" + len + " -> "+ pn);
			} catch (BadLocationException ble) {
				ble.printStackTrace();
			}
		}

		public void error(SAXParseException e) throws SAXException {
			doError(e);
		}

		public void fatalError(SAXParseException e) throws SAXException {
			doError(e);
		}

		public void warning(SAXParseException e) throws SAXException {
			doError(e);
		}

	}


}