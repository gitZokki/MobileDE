package de.zokki.mobile;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mobile {

    public static void main(String[] args) throws Exception {
	new Mobile();
    }

    private Mobile() throws Exception {
//	new HtmlDownloader().getAllLinks();
//	cleanLinks();

//	test("https://suchen.mobile.de/fahrzeuge/details.html?id=317550118");
    }

    private void cleanLinks() throws IOException {
	BufferedInputStream input = new BufferedInputStream(new FileInputStream(Singleton.getLinkFile()));

	int c = 0;
	StringBuilder builder = new StringBuilder();
	while ((c = input.read()) != -1) {
	    builder.append((char) c);
	}

	Pattern getIds = Pattern.compile("(\\?id=)([0-9]+)");
	Matcher idsMatcher = getIds.matcher(builder.toString());
	ArrayList<String> ids = new ArrayList<String>();

	while (idsMatcher.find()) {
	    String id = idsMatcher.group(2);
	    if (!ids.contains(id)) {
		ids.add(id);
	    }
	}
	Singleton sing = Singleton.getInstance();
	ids.forEach(sing::writeToClearedFile);
    }

    private void test(String url) throws Exception {
	URL mobile = new URL(url);

	HttpURLConnection con = (HttpURLConnection) mobile.openConnection();
	con.setRequestMethod("GET");
	con.setRequestProperty("Host", "charlotte.realforeclose.com");
	con.setRequestProperty("Connection", "keep-alive");
	con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
	con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
	con.setRequestProperty("User-Agent",
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
	con.setRequestProperty("Origin", "http://evil.com/");
	con.setRequestProperty("Referer",
		"https://charlotte.realforeclose.com/index.cfm?zaction=AUCTION&Zmethod=PREVIEW&AUCTIONDATE=07/23/2019");
	con.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

	BufferedInputStream input = new BufferedInputStream(con.getInputStream());

	int c = 0;
	StringBuilder builder = new StringBuilder();
	while ((c = input.read()) != -1) {
	    builder.append((char) c);
	}

	System.out.println(builder.toString());
    }
}
