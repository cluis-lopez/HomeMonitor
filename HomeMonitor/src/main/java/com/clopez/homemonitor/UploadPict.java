package com.clopez.homemonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.appengine.repackaged.com.google.common.io.ByteStreams;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

public class UploadPict extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		// Get GCS service
		GcsService gcsService = GcsServiceFactory.createGcsService();

		// Set Option for that file
		GcsFileOptions options = new GcsFileOptions.Builder().mimeType("image/jpg").acl("public-read").build();

		// For multipart support
		ServletFileUpload upload = new ServletFileUpload();

		// Trying to create file
		try {

			FileItemIterator iterator = upload.getItemIterator(req);

			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				InputStream stream = item.openStream();
				String name = item.getName();
				String bucket = item.getFieldName();
				GcsFilename filename = new GcsFilename(bucket, name);
				GcsOutputChannel writeChannel = gcsService.createOrReplace(filename, options);
				
				byte[] bytes = ByteStreams.toByteArray(stream);

				try {
					// Write data from photo
					writeChannel.write(ByteBuffer.wrap(bytes));

				} finally {

					writeChannel.close();
					stream.close();
					res.setStatus(HttpServletResponse.SC_CREATED);
					res.setContentType("text/plain");
					PrintWriter out = res.getWriter();
					out.write("http://" + bucket + "/" + name);
					out.flush();
					out.close();
				}
			}

		} catch (FileUploadException e) {

			e.printStackTrace();
		}

	}
}