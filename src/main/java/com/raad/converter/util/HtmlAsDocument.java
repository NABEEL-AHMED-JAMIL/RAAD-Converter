package com.raad.converter.util;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlInlineFrame;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URL;

@Component
@Scope("prototype")
public class HtmlAsDocument {

	public Logger logger = LoggerFactory.getLogger(SocketServerComponent.class);

	@Autowired
	private WebDriver webDriver;

	private final String SSL = "SSL";
	private final String HTML = "<html></html>";
	public final String HTTP = "http";
	public final String IFRAME = "iframe";

	/**
	 * Trust all web certificates from any web source.
	 */
	@PostConstruct
	private void acceptAllCertificates() {
		// Create a new trust manager that trust all certificates
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
			}
		};

		try {
			SSLContext sc = SSLContext.getInstance(SSL);
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception ex) {
			logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
		}
	}

	public Document getHtml(String url, String tag) {
		Document document = Jsoup.parse("<html></html>");
		try {
			// Connection.Response used here to resolve the issue with legifrance url
			Connection.Response execute = Jsoup.connect(url).header("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.109 Safari/537.36")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
					.header("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3").header("Accept-Encoding", "none")
					.header("Accept-Language", "en-US,en;q=0.9").header("Connection", "keep-alive").execute();
			document = Jsoup.parse(execute.body(), url);
			if (tag == null || tag.trim().isEmpty() || document.select(tag).isEmpty()) {
				document = getJSRenderHtml(url, tag);
			}
			return document;
		} catch (Exception ex) {
			document = getJSRenderHtml(url, tag);
			logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
		}
		return document;
	}


	public Document getJSRenderHtml(String url, String tag) {
		logger.info("HEADERLESS BROWSER REQUEST RECIEVED FOR URL : [ " + url + " ]");
		Document document = Jsoup.parse(HTML);
		try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.getOptions().setTimeout(100 * 1000);
			// new change added by nabeel
			webClient.getOptions().setAppletEnabled(false);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			webClient.setCssErrorHandler(new SilentCssErrorHandler());
			webClient.setHTMLParserListener(null);
			webClient.setJavaScriptErrorListener(null);
			webClient.setJavaScriptTimeout(10000);
			webClient.setRefreshHandler(new ThreadedRefreshHandler());
			HtmlPage page = webClient.getPage(new URL(url));
			webClient.waitForBackgroundJavaScript(30 * 1000);
			document = Jsoup.parse(page.asXml(), url);

			if (tag == null || tag.trim().isEmpty() || document.select(tag).isEmpty()) {
				try {
					if (!page.getElementsByTagName(IFRAME).isEmpty()) {
						HtmlInlineFrame iframe = (HtmlInlineFrame) page.getElementsByTagName(IFRAME).get(0);
						if (!iframe.getSrcAttribute().isEmpty()) {
							if (iframe.getEnclosedPage().isHtmlPage() && !iframe.getSrcAttribute().startsWith(HTTP)
									&& isNotSocialSite(iframe.getSrcAttribute())) {
								HtmlPage innerPage = (HtmlPage) iframe.getEnclosedPage();
								if (!innerPage.asText().isEmpty()) {
									logger.info("Iframe rendering for URL [ " + url + " ]");
									document = Jsoup.parse(innerPage.asXml(), url);
								}
							}
						}
					}
				} catch (Exception ex) {
					logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
				}
			}
			if (tag != null && StringUtils.isNotEmpty(tag.trim()) && document.select(tag).isEmpty()) {
				document = seleniumChromeDriver(url, tag);
			}
			page.cleanUp();
			page.remove();
			webClient.getCache().clear();
			webClient.close();
		} catch (Exception ex) {
			logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
		}
		return document;
	}

	public Document seleniumChromeDriver(String url, String tag) {
		Document document = Jsoup.parse("<html></html>");
		try {
			logger.info("SELENIUM CHROME REQUEST RECIEVED FOR URL : [ " + url + " ]");
			webDriver.get(url);
			WebDriverWait wait = new WebDriverWait(webDriver, 30);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(tag)));
			document = Jsoup.parse(webDriver.getPageSource(), url);
			webDriver.manage().deleteAllCookies();
		} catch (Exception ex) {
			logger.error("Exception occurred " + ex);
		}
		return document;
	}

	public boolean isNotSocialSite(String src) {
		src = src.toLowerCase();
		String[] socialSites = { "twitter", "facebook", "linkedin", "gmail" };
		for (String sc : socialSites) {
			if(src.contains(sc)) { return false; }
		}
		return true;
	}

}