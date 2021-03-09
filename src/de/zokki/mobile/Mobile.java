package de.zokki.mobile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mobile {

    public static void main(String[] args) throws Exception {
	new Mobile();
    }

    private Mobile() throws Exception {
	new HtmlDownloader().getAllLinks();
	cleanLinks();
	getAllInfos();
//	test("https://suchen.mobile.de/fahrzeuge/details.html?id=312109886&amp;damageUnrepaired=NO_DAMAGE_UNREPAIRED&amp;doorCount=FOUR_OR_FIVE&amp;grossPrice=true&amp;isSearchRequest=true&amp;maxMileage=100000&amp;maxPrice=3500&amp;minFirstRegistrationDate=2010-01-01&amp;pageNumber=0&amp;scopeId=C&amp;sfmr=false&amp;action=topOfPage&amp;top=1:1&amp;searchId=5ceece0d-0569-d05c-de98-bc6bead65fa3");
//	test2();
//	test("https://suchen.mobile.de/fahrzeuge/details.html?id=317134958&cn=DE&damageUnrepaired=NO_DAMAGE_UNREPAIRED&doorCount=FOUR_OR_FIVE&gn=01994%2C+Annah%C3%BCtte%2C+Brandenburg&isSearchRequest=true&ll=51.55957049950955%2C13.896665096108336&makeModelVariant1.makeId=22500&makeModelVariant1.modelId=7&maxMileage=100000&minFirstRegistrationDate=2011-01-01&pageNumber=1&rd=200&scopeId=C&sfmr=false&sortOption.sortBy=searchNetGrossPrice&sortOption.sortOrder=ASCENDING&fnai=prev&searchId=0b9ab256-49c4-5f60-5808-8f86bad6e5f0");
    }

    private void cleanLinks() throws IOException {
	Singleton sing = Singleton.getInstance();

	Pattern getIds = Pattern.compile("(\\?id=)([0-9]+)");
	ArrayList<String> ids = new ArrayList<String>();
	ArrayList<String> links = new ArrayList<String>();

	Files.lines(Singleton.getInstance().getLinkFile().toPath(), StandardCharsets.UTF_8).forEach(line -> {
	    Matcher lineMatcher = getIds.matcher(line);
	    if (!lineMatcher.find()) {
		System.err.println("not found");
		return;
	    }

	    String id = lineMatcher.group();
	    if (!ids.contains(id)) {
		ids.add(id);
		links.add(line);
	    }
	});

	System.out.println(links.size());
	links.forEach(sing::writeToClearedFile);
    }

    private void getAllInfos() throws IOException {
	HtmlDownloader contentGetter = new HtmlDownloader();
	Files.lines(Singleton.getInstance().getClearFile().toPath(), StandardCharsets.UTF_8).forEach(line -> {
	    try {
		StringBuilder builder = new StringBuilder();

		String page = contentGetter.getHtmlFromUrl(line);
		page = page.replaceAll("\n", " ");
		page = page.replaceAll("(<head>)(.+?)(</head>)", "");
		page = page.replaceAll("(<header)(.+?)(</header>)", "");
		page = page.replaceAll("(<script)(.+?)(</script>)", "");
		page = page.replaceAll("(<article)(.+?)(</article>)", "");
		page = page.replaceAll("(<span class=\"tooltip-wrapper\")(.+?)(</span>)", "");
		page = page.replaceAll("((?:(src=\")|(url\\())(data:image/png;base64.+?)(?:(\")|(\\))))", "");
		if (!page.contains("<div class=\"container-contact-form\"") || !page.contains(
			"<div id=\"S_05_ADSENSE_CSA_VIP_BOTTOM\" class=\"adv S_05_ADSENSE_CSA_VIP_BOTTOM_de\" > <div id=\"S_05_ADSENSE_CSA_VIP_BOTTOM-container\" class=\"ad_container\" style=\"width:auto\"></div>   </div>")) {
		    builder.append("contact-form not found" + "\n");
		    System.err.println("contact-form not found");
		}
		page = page.replaceAll(
			"(<div class=\"container-contact-form\">)(.+?)(<div id=\"S_05_ADSENSE_CSA_VIP_BOTTOM\" class=\"adv S_05_ADSENSE_CSA_VIP_BOTTOM_de\" > <div id=\"S_05_ADSENSE_CSA_VIP_BOTTOM-container\" class=\"ad_container\" style=\"width:auto\"></div>   </div>)",
			"");

		Pattern titlePattern = Pattern.compile("(<h1 id=\"rbt-ad-title\" class=\"h2\">)(.+?)(</h1>)");
		Pattern imagesPattern = Pattern
			.compile("(?:(src=\")|(data-lazy=\"))((\\/\\/i.ebayimg.com\\/)(.+?\\.jpg)+?)(\")");
		Pattern datasPattern = Pattern.compile(
			"(<div class=\"cBox-body cBox-body--technical-data\" id=\"rbt-td-box\">)(.+?)(</div><a id=\"vip-features\"></a>)");
		Pattern featuresPattern = Pattern.compile(
			"(<div class=\"cBox-body\" id=\"rbt-features\">)(.+?)(</div><a id=\"vip-price-rating-anchor\"></a>)");

		Matcher titleMatcher = titlePattern.matcher(page);
		Matcher imagesMatcher = imagesPattern.matcher(page);
		Matcher datasMatcher = datasPattern.matcher(page);
		Matcher featuresMatcher = featuresPattern.matcher(page);

		while (titleMatcher.find()) {
		    builder.append(titleMatcher.group(2) + "\n");
		}
		builder.append("-----------------------------------------------------\n");
		while (imagesMatcher.find()) {
		    builder.append(imagesMatcher.group(3) + "\n");
		}
		builder.append("-----------------------------------------------------\n");
		if (datasMatcher.find()) {
		    String data = datasMatcher.group();
		    Pattern dataPattern = Pattern
			    .compile("(<div class=\"g-row u-margin-bottom-9\">)(.+?)(</div></div>)");
		    Matcher dataMatcher = dataPattern.matcher(data);
		    while (dataMatcher.find()) {
			String innerData = dataMatcher.group();
			Pattern dataInnerPattern = Pattern
				.compile("(<strong>(.+?)</strong>)(.+?)((<div class=\"(.+?)\">(.+?)</div>)+)");
			Matcher dataInnerMatcher = dataInnerPattern.matcher(innerData);
			if (dataInnerMatcher.find()) {
			    builder.append("\n" + dataInnerMatcher.group(2) + ": ");
			    String inner = dataInnerMatcher.group(4);
			    Pattern innerPricePattern = Pattern.compile(
				    "(<span>(.+?)</span>)(.+?)(<div class=\"mde-price-rating__badge__label\">(.+?)</div>)");
			    Matcher innerPriceMatcher = innerPricePattern.matcher(inner);

			    if (innerPriceMatcher.find()) {
				builder.append("\n\t" + innerPriceMatcher.group(2) + " - "
					+ innerPriceMatcher.group(innerPriceMatcher.groupCount()));
				continue;
			    }

			    Pattern innerPattern = Pattern.compile("(>(.+?)</div>)");
			    Matcher innerMatcher = innerPattern.matcher(inner);

			    while (innerMatcher.find()) {
				builder.append("\n\t"
					+ innerMatcher.group(2).replace("<div class=\"u-margin-bottom-9\">", ""));
			    }
			} else {
			    builder.append("Innerdata not found" + "\n");
			    System.err.println("Innerdata not found");
			}
		    }
		    builder.append("\n");
		} else {
		    builder.append("Innerdata not found" + "\n");
		    System.err.println("datas not found");
		}
		builder.append("-----------------------------------------------------" + "\n");
		if (featuresMatcher.find()) {
		    String features = featuresMatcher.group();
		    Pattern featurePattern = Pattern.compile("(<p>(.+?)</p>)");
		    Matcher featureMatcher = featurePattern.matcher(features);
		    builder.append("Ausstattung\n");
		    while (featureMatcher.find()) {
			builder.append(featureMatcher.group(2) + "\n");
		    }
		} else {
		    builder.append("features not found" + "\n");
		    System.err.println("features not found");
		}
		builder.append("-----------------------------------------------------" + "\n");
		Pattern featurePattern = Pattern.compile("(<p id=\"rbt-db-address\">(.+?)</p>)");
		Matcher featureMatcher = featurePattern.matcher(page);
		if (featureMatcher.find()) {
		    builder.append("Adresse: \n\t" + featureMatcher.group(2) + "\n");
		}

		String id;
		Pattern idPattern = Pattern.compile("(<li>Detailansicht \\((.+?)\\)</li>)");
		Matcher idMatcher = idPattern.matcher(page);
		if (idMatcher.find()) {
		    id = idMatcher.group(2);
		    builder.append("ID: \n\t" + id + "\n");
		} else {
		    id = Math.random() + "";
		    builder.append("ID not found\n");
		}

		Pattern descriptionPattern = Pattern.compile("(<div class=\"g-col-12 description\">(.+?)</b></div>)");
		Matcher descriptionMatcher = descriptionPattern.matcher(page);
		if (descriptionMatcher.find()) {
		    builder.append(descriptionMatcher.group() + "\n");
		}

		File file = new File(id + ".txt");
		file.createNewFile();
		Singleton.getInstance().writeToFile(file, builder.toString());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	});
    }

}
