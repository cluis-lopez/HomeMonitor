package com.clopez.homemonitor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

@SuppressWarnings("serial")
public class UploadBlob extends HttpServlet {
private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
@Override
public void doPost(HttpServletRequest req, HttpServletResponse resp)
     throws IOException {

    String blobid= "";
    
	// Store the picture and get the key
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
    List<BlobKey> blobKey = blobs.get("Foto");
    BlobKey myblob = blobKey.get(0);

    		
    if (myblob == null) {
        resp.sendRedirect("/Error");
    } else {
        blobid = myblob.getKeyString();
    }

    //System.out.println("Foto id = "+blobid);
	
	// Return the blobid (the key)	
	resp.setContentType("text/plain");
	resp.setCharacterEncoding("UTF-8");
	resp.getWriter().write(blobid);
	
	}
}