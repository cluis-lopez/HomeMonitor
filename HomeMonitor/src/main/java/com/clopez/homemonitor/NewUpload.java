package com.clopez.homemonitor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.nio.channels.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFileOptions.Builder;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

/**
 * Servlet implementation class NewUpload
 */
public class NewUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		// read the input stream
		byte[] buffer = new byte[1024];
		List<byte[]> allBytes = new LinkedList<byte[]>();
		InputStream reader = req.getInputStream();
		while (true) {
			int bytesRead = reader.read(buffer);
			if (bytesRead == -1) {
				break; // have a break up with the loop.
			} else if (bytesRead < 1024) {
				byte[] temp = Arrays.copyOf(buffer, bytesRead);
				allBytes.add(temp);
			} else {
				allBytes.add(buffer);
			}
		}

		// init the bucket access
		GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
		GcsFilename filename = new GcsFilename("homemonitor-156618.appspot.com", "pepe");
		Builder fileOptionsBuilder = new GcsFileOptions.Builder();
		fileOptionsBuilder.mimeType("image/jpeg"); // or "image/jpeg" for image
		// files
		GcsFileOptions fileOptions = fileOptionsBuilder.build();
		GcsOutputChannel outputChannel = gcsService.createOrReplace(filename, fileOptions);

		// write file out
		BufferedOutputStream outStream = new BufferedOutputStream(Channels.newOutputStream(outputChannel));
		for (byte[] b : allBytes) {
			outStream.write(b);
		}
		outStream.close();
		outputChannel.close();
		
		resp.setContentType("text/plain");
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().write("OK");
	}
}
