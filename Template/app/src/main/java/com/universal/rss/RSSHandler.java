package com.universal.rss;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  This class is used to help us get the right information from the data from the feed.
 */

public class RSSHandler extends DefaultHandler {
	
	private State currentState = State.unknown;

    private RSSFeed feed;
    private RSSItem item;

    private boolean itemFound = false;

    private StringBuilder tagContent;

    public RSSHandler() {
    }

    @Override
    public void startDocument() throws SAXException {
        feed = new RSSFeed();
        item = new RSSItem();
    }

    @Override
    public void startElement(final String uri, final String localName, 
            final String qName, final Attributes attributes)
            throws SAXException {
        currentState = State.unknown;
        tagContent = new StringBuilder();
        if (localName.equalsIgnoreCase("item") || localName.equalsIgnoreCase("entry")) {
            itemFound = true;
            item = new RSSItem();
            currentState = State.unknown;
        } else if (qName.equalsIgnoreCase("title")) {
            currentState = State.title;
        } else if (qName.equalsIgnoreCase("description") || qName.equalsIgnoreCase("content:encoded")) {
            currentState = State.description;
        } else if (qName.equalsIgnoreCase("link") || qName.equalsIgnoreCase("origLink")) {
            currentState = State.link;
        } else if (qName.equalsIgnoreCase("pubdate") || qName.equalsIgnoreCase("published")) {
            currentState = State.pubdate;
        } else if (qName.equalsIgnoreCase("media:thumbnail") || qName.equalsIgnoreCase("media:content")) { 
            currentState = State.thumbnailurl;    
        	String attrValue = attributes.getValue("url");
        	item.setThumburl(attrValue);
        } 
        System.out.println("new state: " + currentState);

    }

    @Override
    public void endElement(final String uri, final String localName, 
            final String qName) throws SAXException {
        if (localName.equalsIgnoreCase("item") || localName.equalsIgnoreCase("entry")) {
            feed.addItem(item);
        }
        if (itemFound == true) {
            // "item" tag found, it's item's parameter
            switch (currentState) {
                case title:
                    item.setTitle(tagContent.toString().trim());
                    break;
                case description:
                    item.setDescription(tagContent.toString());                   
                    //if thumburl not already set
                    if (item.getThumburl() == null || item.getThumburl() == ""){
                    	String html = tagContent.toString();
                    	org.jsoup.nodes.Document docHtml = Jsoup
                    	.parse(html);
                    	Elements imgEle = docHtml.select("img");
                    	String source = imgEle.attr("src");
                    	item.setThumburl(source);
                    }
                    break;
                case link:
                    item.setLink(tagContent.toString());
                    break;
                case pubdate:
                    item.setPubdate(tagContent.toString());
                    break;
                case thumbnailurl:
                	break;
                default:
                    break;
            }
        } else {
            // not "item" tag found, it's feed's parameter
            switch (currentState) {
                case title:
                    feed.setTitle(tagContent.toString());
                    break;
                case description:
                    feed.setDescription(tagContent.toString());
                    break;
                case link:
                    feed.setLink(tagContent.toString());
                    break;
                case pubdate:
                    feed.setPubdate(tagContent.toString());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) 
            throws SAXException {
        tagContent.append(ch, start, length);
    }

    public RSSFeed getFeed() {
        return feed;
    }
    
    public enum State {

        unknown, title, description, link, pubdate, thumbnailurl

    }

}