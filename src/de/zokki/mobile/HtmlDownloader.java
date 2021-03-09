package de.zokki.mobile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlDownloader {

    public void getAllLinks() throws Exception {
	String startPage = getHtmlFromUrl(getUrl(0));
	System.out.println("Startpage found");

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

	System.out.println("Max pages: " + maxPage);
	for (int i = 0; i <= maxPage; i++) {
	    String html = getHtmlFromUrl(getUrl(i));
	    getAndWriteLinks(html);
	    Thread.sleep(250);
	}
    }

    public String getHtmlFromUrl(String url) throws Exception {
	System.out.println("GetFromUrl: " + url);
	URL mobile = new URL(url);

	HttpURLConnection con = (HttpURLConnection) mobile.openConnection();
	con.setRequestMethod("GET");
	con.setRequestProperty("Host", "charlotte.realforeclose.com");
	con.setRequestProperty("Connection", "keep-alive");
	con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
	con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
	con.setRequestProperty("User-Agent",
		"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0");
	con.setRequestProperty("Origin", "http://evil.com/");
	con.setRequestProperty("Referer",
		"https://charlotte.realforeclose.com/index.cfm?zaction=AUCTION&Zmethod=PREVIEW&AUCTIONDATE=07/23/2019");
	con.setRequestProperty("Accept-Language", "en-US,en;q=0.9");

	BufferedInputStream input = new BufferedInputStream(con.getInputStream());
	ByteArrayOutputStream received = new ByteArrayOutputStream();
	byte[] buffer = new byte[32768];
	int length;
	while ((length = input.read(buffer)) != -1) {
	    received.write(buffer, 0, length);
	}
	input.close();

	return received.toString(StandardCharsets.UTF_8);
    }

    private String getUrl(int page) {
	return "https://suchen.mobile.de/fahrzeuge/search.html?cn=DE&damageUnrepaired=NO_DAMAGE_UNREPAIRED&doorCount=FOUR_OR_FIVE&grossPrice=true&isSearchRequest=true&makeModelVariantExclusions%5B0%5D.makeId=25200&makeModelVariantExclusions%5B0%5D.modelId=9&makeModelVariantExclusions%5B1%5D.makeId=5600&makeModelVariantExclusions%5B1%5D.modelId=13&makeModelVariantExclusions%5B2%5D.makeId=8800&makeModelVariantExclusions%5B2%5D.modelId=28&maxConsumptionCombined=6&maxMileage=100000&maxPrice=3500&minFirstRegistrationDate=2010-01-01&pageNumber="
		+ page + "&scopeId=C&sfmr=false";
    }

    private void getAndWriteLinks(String html) {
	Singleton single = Singleton.getInstance();

	Pattern findLinks = Pattern.compile("(href=\")(.+?)(\")");
	Matcher linksMatcher = findLinks.matcher(html);
	while (linksMatcher.find()) {
	    String linkGroup = linksMatcher.group(2);
	    if (linkGroup.startsWith("https://suchen.mobile.de/fahrzeuge/details.html?")) {
		single.writeToFile(linkGroup + "\n");
	    }
	}
    }

}
