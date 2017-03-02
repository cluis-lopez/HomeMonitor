package com.clopez.homemonitor;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;



@SuppressWarnings("serial")
public class GetURL extends HttpServlet {
public void doGet(HttpServletRequest req, HttpServletResponse resp)
     throws IOException {
	final Logger log = Logger.getLogger(GetURL.class.getName());
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	String url = blobstoreService.createUploadUrl("/UploadBlob");
    resp.setContentType("text/plain");
    resp.setCharacterEncoding("UTF-8");
    resp.addHeader("Access-Control-Allow-Origin", "*");
    resp.setHeader("Cache-Control", "no-store");  
    resp.setHeader("Pragma", "no-cache"); 
    log.info("URL = "+url);
    resp.getWriter().write(url);
    resp.flushBuffer();
	}
}