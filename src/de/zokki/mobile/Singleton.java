package de.zokki.mobile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class Singleton {

    private static Singleton instance;

    private static File linkFile;
    private static File clearedLinkFile;
    private static BufferedOutputStream linkWriter;
    private static BufferedOutputStream clearedLinkWriter;

    private Singleton() {
	try {
	    linkFile = new File("links.txt");
	    clearedLinkFile = new File("cleanLinks.txt");
	    linkWriter = new BufferedOutputStream(new FileOutputStream(linkFile, true));
	    clearedLinkWriter = new BufferedOutputStream(new FileOutputStream(clearedLinkFile, true));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static Singleton getInstance() {
	if (instance == null) {
	    instance = new Singleton();
	}
	return instance;
    }

    public synchronized void writeToFile(String link) {
	try {
	    linkWriter.write(link.getBytes(StandardCharsets.UTF_8));
	    linkWriter.flush();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public synchronized void writeToClearedFile(String link) {
	try {
	    clearedLinkWriter.write((link + "\n").getBytes(StandardCharsets.UTF_8));
	    clearedLinkWriter.flush();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public synchronized void writeToFile(File file, String content) {
	try {
	    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
	    output.write(content.getBytes(StandardCharsets.UTF_8));
	    output.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public File getLinkFile() {
	return linkFile;
    }
    
    public File getClearFile() {
	return clearedLinkFile;
    }
}
