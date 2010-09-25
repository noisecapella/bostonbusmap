package boston.Bus.Map.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.Directions;

public class UMichFeedParser extends UMichInitialFeedParser {

	public UMichFeedParser(Directions directions,
			HashMap<String, String> routeKeysToTitles, Drawable busStop) {
		super(directions, routeKeysToTitles, busStop);
		// TODO Auto-generated constructor stub
	}

}
