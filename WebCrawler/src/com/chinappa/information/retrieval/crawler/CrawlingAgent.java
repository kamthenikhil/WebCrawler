package com.chinappa.information.retrieval.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chinappa.information.retrieval.configuration.WebCrawlerConfiguration;
import com.chinappa.information.retrieval.constants.CommonConstants;
import com.chinappa.information.retrieval.constants.CrawlerConstants;
import com.chinappa.information.retrieval.util.DuplicateDocumentDetectionUtil;
import com.chinappa.information.retrieval.util.FileHandlerUtil;
import com.chinappa.information.retrieval.util.RobotExclusionUtil;

public class CrawlingAgent implements Runnable {

	public static AtomicInteger fileno = new AtomicInteger(0);

	private ConcurrentHashMap<String, String> urlMap;

	private ConcurrentLinkedQueue<Runnable> threadQueue;

	private String urlString;

	private int level;

	private boolean isSuccess = false;

	/**
	 * Parameterized Constructor
	 * 
	 * @param urlMap
	 * @param threadQueue
	 * @param url
	 * @param level
	 */
	public CrawlingAgent(ConcurrentHashMap<String, String> urlMap,
			ConcurrentLinkedQueue<Runnable> threadQueue, String url, int level) {
		this.urlMap = urlMap;
		this.threadQueue = threadQueue;
		this.urlString = url;
		this.level = level;
	}

	@Override
	public void run() {
		if (level > 0
				&& WebCrawlerConfiguration.getInstance().getPageLimit()
						.longValue() > 0) {
			level--;
			if (urlString != null) {
				try {
					urlString = urlString.replaceAll("(/*)$", "");
					URL url = new URL(urlString);
					boolean isNewURL = false;
					synchronized (urlMap) {
						if (!urlMap.containsKey(urlString)) {
							urlMap.put(urlString, CommonConstants.EMPTY_STRING);
							isNewURL = true;
						}
					}
					if (isNewURL) {
						ArrayList<String> childURLs = writeFileAndfetchHyperLinks();
						if (childURLs != null && childURLs.size() > 0) {
							for (String childURL : childURLs) {
								threadQueue.add(new CrawlingAgent(urlMap,
										threadQueue, childURL, level));
							}
						}
					} else {
					}
				} catch (MalformedURLException e) {
				}
			}
		}
		if (isSuccess) {
			WebCrawlerConfiguration.getInstance().getPageLimit()
					.decrementAndGet();
		}
	}

	/**
	 * The following method is used to fetch the doc using jsoup and returns a
	 * list of child URLs. It also writes down the content of the HTML page in a
	 * file on a local system.
	 * 
	 * @param url
	 * @param level
	 * @return
	 */
	private ArrayList<String> writeFileAndfetchHyperLinks() {

		if (isValidURLProtocol(urlString)
				&& RobotExclusionUtil.robotsShouldFollow(urlString)) {
			WebCrawlerConfiguration webCrawlerConfiguration = WebCrawlerConfiguration
					.getInstance();
			String extension = fetchDocumentType(urlString);
			switch (extension) {
			case CrawlerConstants.HTTP_PROTOCOL_PREFIX:
				Document doc = null;
				ArrayList<String> childURLs = new ArrayList<String>();
				try {
					Connection jsoupConnection = Jsoup.connect(urlString)
							.timeout(5000);
					jsoupConnection.followRedirects(true);
					doc = jsoupConnection.get();
					String documentText = FileHandlerUtil.fetchDocumentText(doc);
					boolean processDoc = true;
					if (webCrawlerConfiguration.isDuplicateDetectionEnabled()) {
						// DuplicateDocumentDetectionUtil util = new
						// DuplicateDocumentDetectionUtil();
						if (DuplicateDocumentDetectionUtil.isDuplicateDocument(
								documentText, urlString)) {
							processDoc = false;
						}
					}
					if (processDoc) {
						int temp = fileno.addAndGet(1);
						urlMap.put(urlString, level + CommonConstants.FULL_STOP
								+ temp
								+ CrawlerConstants.OUTPUT_FILE_EXTENTION_HTML);
						isSuccess = true;
						if (webCrawlerConfiguration.isCompressionEnabled()) {
							FileHandlerUtil
									.writeToCompressedHTMLFile(
											doc.html(),
											webCrawlerConfiguration
													.getOutputDirectory(),
											level + CommonConstants.FULL_STOP
													+ temp,
											urlString,
											CrawlerConstants.OUTPUT_FILE_EXTENTION_HTML);
						} else {
							FileHandlerUtil
									.writeToFile(
											doc.html(),
											webCrawlerConfiguration
													.getOutputDirectory(),
											level + CommonConstants.FULL_STOP
													+ temp,
											urlString,
											CrawlerConstants.OUTPUT_FILE_EXTENTION_HTML);
						}
						if (level > 0)
							childURLs = fetchChildURLs(urlString, doc);
					} else {
					}
				} catch (IOException e) {
				} catch (IllegalArgumentException e) {
				}
				return childURLs;
			default:
				int temp = fileno.addAndGet(1);
				FileHandlerUtil.writeURLFileToDisk(urlString,
						webCrawlerConfiguration.getOutputDirectory(), level
								+ CommonConstants.FULL_STOP + temp, extension);
				urlMap.put(urlString, level + CommonConstants.FULL_STOP + temp
						+ extension);
				isSuccess = true;
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * The following method is used to determine the document type from the URL
	 * string.
	 * 
	 * @param url
	 * @return
	 */
	private String fetchDocumentType(String url) {

		if (url.toLowerCase().endsWith(CrawlerConstants.PDF_SUFFIX)) {
			return CrawlerConstants.PDF_SUFFIX;
		} else if (url.toLowerCase().endsWith(CrawlerConstants.JPEG_SUFFIX)) {
			return CrawlerConstants.JPEG_SUFFIX;
		} else if (url.toLowerCase().endsWith(CrawlerConstants.JPG_SUFFIX)) {
			return CrawlerConstants.JPG_SUFFIX;
		} else if (url.toLowerCase().endsWith(CrawlerConstants.BMP_SUFFIX)) {
			return CrawlerConstants.BMP_SUFFIX;
		} else if (url.toLowerCase().endsWith(CrawlerConstants.GIF_SUFFIX)) {
			return CrawlerConstants.GIF_SUFFIX;
		} else {
			return CrawlerConstants.HTTP_PROTOCOL_PREFIX;
		}
	}

	/**
	 * The following method check if URL with a given protocol should be parsed.
	 * Currently we only parse HTTP URLs.
	 * 
	 * @param urlString
	 * @return
	 */
	private boolean isValidURLProtocol(String urlString) {

		try {
			URL url = new URL(urlString);
			switch (url.getProtocol().toString().toLowerCase()) {
			case CrawlerConstants.HTTP_PROTOCOL_PREFIX:
				return true;
			case CrawlerConstants.HTTPS_PROTOCOL_PREFIX:
				return false;
			case CrawlerConstants.FTP_PROTOCOL_PREFIX:
				return false;
			default:
				return false;
			}
		} catch (MalformedURLException e) {
			return false;
		}
	}

	/**
	 * The following method extracts all the hyper-links in the HTML document.
	 * It then runs the child URLs through a bunch of filters and finally
	 * returns the list eligible child URLs.
	 * 
	 * @param url
	 * @param doc
	 * @return
	 */
	private ArrayList<String> fetchChildURLs(String url, Document doc) {
		Elements elements = doc.select(CrawlerConstants.HTML_LINKS_HREF);
		ArrayList<String> childURLs = null;
		if (elements != null && elements.size() > 0) {
			childURLs = new ArrayList<String>();
			for (Element element : elements) {
				String childURLString = element
						.attr(CrawlerConstants.HTML_HREF).trim();
				childURLString = childURLString.replaceAll("^/", "");
				if (!checkIfBookmark(childURLString)) {
					if (!childURLString
							.startsWith(CrawlerConstants.HTTP_PROTOCOL_PREFIX)) {
						if (childURLString.length() <= 1) {
							try {
								URL childURL = new URL(childURLString);
								if (!childURL.getHost().toLowerCase()
										.endsWith(CrawlerConstants.EDU_SUFFIX)) {
									break;
								}
							} catch (MalformedURLException e) {
								break;
							}
						} else {
							childURLString = url
									+ CommonConstants.FORWARD_SLASH
									+ childURLString;
						}
					}
					childURLString.replaceAll("/$", "");
					childURLs.add(childURLString);
				}
			}
		}
		return childURLs;
	}

	/**
	 * The following method checks if the URL is a HTML bookmark.
	 * 
	 * @param childURL
	 * @return
	 */
	private boolean checkIfBookmark(String childURL) {

		if (childURL.startsWith(CommonConstants.HASH)) {
			return true;
		} else {
			return false;
		}
	}

}