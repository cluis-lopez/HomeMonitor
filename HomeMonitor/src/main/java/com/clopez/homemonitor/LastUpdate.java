package com.clopez.homemonitor;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;



@SuppressWarnings("serial")
public class LastUpdate extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		final Logger log = Logger.getLogger(LastUpdate.class.getName());

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd-MM-YYYY z HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		
		// Create the URL to serve the pictures
		AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
		String host = appIdentity.getDefaultGcsBucketName();

		// The variables that make the JSON answer

		Double temp = 0.0, light = 0.0 , hum = 0.0;
		String keyname = "", ts = "", tsurl = "", error="";
		String url = "";
		Date date;

		// Get the last sample stored

		Query q = new Query("Samples").setKeysOnly();
		q.addSort("__key__", SortDirection.DESCENDING);
		List<Entity> ents = ds.prepare(q).asList(FetchOptions.Builder.withLimit(1));
		if (ents.size() == 0){ //No entries in the datastore ??
			error = "No data in the cloud";
		} else {
			Key lastkey = ents.get(0).getKey(); // lastkey points to the last entry in the datastore for kind "Samples"

			//Get the last photo stored in the blobstore

			Query q2 = new Query("Samples");
			q2.setFilter(FilterOperator.NOT_EQUAL.of("Pict", null));
			PreparedQuery pq2 = ds.prepare(q2);
			TreeSet<Key> trees = new TreeSet<Key>(); //Now we'll store all the not nulls URLs in a TreeSet

			for (Entity e : pq2.asIterable()) {
				trees.add(e.getKey());
			}
			
		// Now let's extract the properties from the datastore

			Entity lastent;
			try {
				lastent = ds.get(lastkey);
				temp = (Double) lastent.getProperty("Temp");
				light = (Double) lastent.getProperty("Light");
				hum = (Double) lastent.getProperty("Hum");
				keyname = (lastent.getKey()).getName();
				date = new Date(Long.parseLong(keyname));
				ts = sdf.format(date);
			} catch (EntityNotFoundException e1) {
				error = "Cannot locate entity for the last sample";
				PrintStream ps = new PrintStream("Cannot locate entity for the last sample");
				e1.printStackTrace(ps);
				e1.printStackTrace();
			}

			
			if (trees.size() == 0){ // No Pics in the blobstore
				error = error +"\r No Pics in blobstore";
			} else {
				Key urlkey = trees.last(); //As the TreeSet is ordered, the last entry is the last non-null URL in the datastore
				Entity blob;
				try {
					blob = ds.get(urlkey);
					url = host + "/" + (String) blob.getProperty("Pict");
					keyname = (blob.getKey()).getName();
					date = new Date(Long.parseLong(keyname));
					tsurl = sdf.format(date);
				} catch (EntityNotFoundException e1) {
					error = "Cannot locate entity for the last picture";
					PrintStream ps = new PrintStream("Cannot locate entity for the last picture");
					e1.printStackTrace(ps);
					e1.printStackTrace();
				}
			}
		}
		// Getting the URL from the stored blobkey

		// Map the data

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Temp", temp);
		data.put("Light", light);
		data.put("Hum", hum);
		data.put("Timestamp", ts);
		data.put("URL", url);
		data.put("TS_URL", tsurl);
		data.put("Error", error);

		Gson gson = new Gson();
		String json = gson.toJson(data);
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("cache-control", "no-cache");
		resp.getWriter().write(json);
		resp.flushBuffer();
		// System.out.println("Enviado JSON LastUpdate");
		// System.out.println(data);
	}
}
