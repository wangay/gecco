package com.geccocrawler.gecco.downloader;

import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.response.HttpResponse;
import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *chrome headless的java 支持包:cdp4j
 * https://github.com/webfolderio/cdp4j
 *
 * 这样可以不用费力分析对方的ajax
 * 直接得到完整加载后的网页.
 * httpClient只能处理到第一次Http 请求到的内容, 很多元素是ajax加载到的. 这部分需要手动分析.
 * 没有无界面浏览器技术来的方便.
 *
 * htmlunit本框架也支持,但还未调通, 也应该没chrome headless好用
 * @author noboday
 *  
 *
 */
@com.geccocrawler.gecco.annotation.Downloader("chromeCdp4jDownloader")
public class ChromeCdp4jDownloader extends AbstractDownloader {

	private static Log log = LogFactory.getLog(ChromeCdp4jDownloader.class);

	private Launcher launcher;

	public ChromeCdp4jDownloader() {
		launcher = new Launcher();
	}

	/***
	 * 使用cdp4j下载
	 * @param request
	 * @param timeout
	 * @return
	 * @throws DownloadException
	 */
	@Override
	public HttpResponse download(HttpRequest request, int timeout) throws DownloadException {
		try {
			HttpResponse resp = new HttpResponse();
			resp.setStatus(200);
			resp.setRaw(null);
			String contentType = null;
			resp.setContentType(contentType);
			if(!isImage(contentType)) {
				String charset = getCharset(request.getCharset(), contentType);
				resp.setCharset(charset);
				String content = "";
				try (SessionFactory factory = launcher.launch();Session session = factory.create()) {
					session.navigate(request.getUrl());
					session.waitDocumentReady();
					content = session.getContent();
				}catch (Exception e){
					e.printStackTrace();
				}

				resp.setContent(content);
			}
			return resp;
		} catch (Exception e) {
			//超时等
			throw new DownloadException(e);
		} finally {
		}
	}
	
	@Override
	public void shutdown() {
	}
	

	private boolean isImage(String contentType) {
		if(contentType == null) {
			return false;
		}
		if(contentType.toLowerCase().startsWith("image")) {
			return true;
		}
		return false;
	}
}
