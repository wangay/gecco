package com.geccocrawler.gecco.downloader;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.geccocrawler.gecco.downloader.proxy.Proxys;
import com.geccocrawler.gecco.downloader.proxy.ProxysContext;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import com.geccocrawler.gecco.spider.SpiderThreadLocal;
import com.geccocrawler.gecco.utils.UrlUtils;
import org.apache.http.HttpHost;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 利用htmlunit实现js的dom操作和ajax
 * 
 * @author huchengyi
 *
 */
@com.geccocrawler.gecco.annotation.Downloader("htmlUnitDownloder")
public class HtmlUnitDownloder extends AbstractDownloader {

	private WebClient webClient;
	
	public HtmlUnitDownloder() {
		this.webClient = new WebClient(BrowserVersion.CHROME);
		this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		this.webClient.getOptions().setThrowExceptionOnScriptError(false);
		this.webClient.getOptions().setRedirectEnabled(false);
		this.webClient.getOptions().setCssEnabled(false);
		this.webClient.setJavaScriptTimeout(1000);
		//this.webClient.setJavaScriptErrorListener(new GeccoJavaScriptErrorListener());
	}
	
	public HttpResponse download(HttpRequest request, int timeout) throws DownloadException {
		try {
			URL url = new URL(request.getUrl());
			WebRequest webRequest = new WebRequest(url);
			webRequest.setHttpMethod(HttpMethod.GET);
			if(request instanceof HttpPostRequest) {//post
				HttpPostRequest post = (HttpPostRequest)request;
				webRequest.setHttpMethod(HttpMethod.POST);
				List<NameValuePair> requestParameters = new ArrayList<NameValuePair>();
				for(Map.Entry<String, String> entry : post.getFields().entrySet()) {
					NameValuePair nvp = new NameValuePair(entry.getKey(), entry.getValue().toString());
					requestParameters.add(nvp);
				}
				webRequest.setRequestParameters(requestParameters);	
			}
			//header
			boolean isMobile = SpiderThreadLocal.get().getEngine().isMobile();
			webRequest.setAdditionalHeader("User-Agent", UserAgent.getUserAgent(isMobile));
			webRequest.setAdditionalHeaders(request.getHeaders());
			//proxy
			HttpHost proxy = null;

			Proxys proxys = ProxysContext.get();
			boolean isProxy = ProxysContext.isEnableProxy();
			if(proxys != null && isProxy) {
				proxy = proxys.getProxy();
			}

			if(proxy != null) {
				webRequest.setProxyHost(proxy.getHostName());
				webRequest.setProxyPort(proxy.getPort());
			}
			//timeout
			this.webClient.getOptions().setTimeout(timeout);
			//request,response
			webClient.getPage(webRequest);
			HtmlPage page = webClient.getPage(request.getUrl());
			HttpResponse resp = new HttpResponse();
			WebResponse webResponse = page.getWebResponse();
			int status = webResponse.getStatusCode();
			resp.setStatus(status);
			if(status == 302 || status == 301) {
				String redirectUrl = webResponse.getResponseHeaderValue("Location");
				resp.setContent(UrlUtils.relative2Absolute(request.getUrl(), redirectUrl));
			} else if(status == 200) {
				String content = page.asXml();
				resp.setContent(content);
				resp.setRaw((ByteArrayInputStream)webResponse.getContentAsStream());
				String contentType = webResponse.getContentType();
				resp.setContentType(contentType);
				String charset = getCharset(request.getCharset(), contentType);
				resp.setCharset(charset);
			} else {
				throw new DownloadException("ERROR : " + status);
			}
			return resp;
		} catch(Exception ex) {
			throw new DownloadException(ex);
		}
	}

	public void shutdown() {
		webClient.close();
	}
}
