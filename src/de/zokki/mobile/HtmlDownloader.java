package de.zokki.mobile;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlDownloader {

    public void getAllLinks() throws Exception {
	String startPage = getHtmlFromUrl(getUrl(0));
	System.out.println(startPage);
	int maxPage = 0;
	Pattern maxPageRegex = Pattern.compile("(<li>)(.+?)(</li>)");
	Matcher maxPageMatcher = maxPageRegex.matcher(startPage);
	while (maxPageMatcher.find()) {
	    Pattern maxPageRegexInner = Pattern.compile("(>)([0-9]+?)(<)");
	    Matcher maxPageMatcherInner = maxPageRegexInner.matcher(maxPageMatcher.group(2));
	    if (maxPageMatcherInner.find()) {
		maxPage = Integer.parseInt(maxPageMatcherInner.group(2));
	    }
	}
	System.out.println(maxPage);
//	for (int i = 0; i <= maxPage; i++) {
//	    String html = getHtmlFromUrl(getUrl(i));
//	    getAndWriteLinks(html);
//	    Thread.sleep(250);
//	}
    }

    private String getHtmlFromUrl(String url) throws Exception {
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

	return builder.toString();
    }

    private String getUrl(int page) {
	return "https://suchen.mobile.de/fahrzeuge/search.html?damageUnrepaired=NO_DAMAGE_UNREPAIRED&doorCount=FOUR_OR_FIVE&grossPrice=true&isSearchRequest=true&maxMileage=100000&maxPrice=3500&minFirstRegistrationDate=2010-01-01&pageNumber="
		+ page + "&scopeId=C&sfmr=false";
    }

    private void getAndWriteLinks(String html) {
	Singleton single = Singleton.getInstance();
	
	Pattern getLinks = Pattern.compile("(https://.+?)(&)");
	Pattern findLinks = Pattern.compile("(href=\")(.+?)(\")");
	Matcher linksMatcher = findLinks.matcher(html);
	while (linksMatcher.find()) {
	    String linkGroup = linksMatcher.group(2);
	    if (linkGroup.startsWith("https://suchen.mobile.de/fahrzeuge/details.html?")) {

		Matcher linksMatcherInner = getLinks.matcher(linkGroup);
		if (linksMatcherInner.find()) {
		    single.writeToFile(linksMatcherInner.group(1) + "\n");
		}
	    }
	}
    }

}
