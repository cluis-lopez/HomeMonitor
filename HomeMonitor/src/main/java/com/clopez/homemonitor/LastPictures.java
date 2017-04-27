package com.clopez.homemonitor;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.ServletException;
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
import com.google.gson.Gson;

/**
 * Servlet implementation class LastPictures
 */
public class LastPictures extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LastPictures() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd-MM-YYYY z HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		LinkedList<Map<String, Object>> lista = new LinkedList<Map<String, Object>>();

		// Create the URL to serve the pictures
		AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
		String bucket = appIdentity.getDefaultGcsBucketName();

		String error = "";

		// Get the last 9 entities of kind "Sample" with Pict != null

		Query q = new Query("Samples");
		q.setFilter(FilterOperator.NOT_EQUAL.of("Pict", null)); // Change this
		List<Entity> ents = ds.prepare(q).asList(FetchOptions.Builder.withLimit(9)); // 3x3
		
		TreeSet<Key> trees = new TreeSet<Key>(); // Now we'll store all the not
												// null url entity keys in a TreeSet
		for (Entity e : ents) {
			trees.add(e.getKey());
		}

		int size= trees.size();
			
		if (size == 0) {
			error = "No pictures stored";
		} else {
			for (int i = 0; i < size; i++) {
				Key urlkey = trees.pollLast();  // As the TreeSet is ordered,
												// this entry is the last
												// non-null URL in the datastore
				try {
					Entity e = ds.get(urlkey);
					Date date = new Date(Long.parseLong(e.getKey().getName()));
					String ts = sdf.format(date);
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("Ts", ts);
					map.put("URL", bucket + "/" + e.getProperty("Pict"));
					lista.add(map);
				} catch (EntityNotFoundException e1) {
					error = "Cannot locate entity for the required Picture";
					PrintStream ps = new PrintStream("Cannot locate entity for the last sample");
					e1.printStackTrace(ps);
					e1.printStackTrace();
				}

			}
		}
		
		// Generate the JSON here
		
		Gson gson = new Gson();
		String json = gson.toJson(lista);

		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("cache-control", "no-cache");
		resp.getWriter().write(json);
		resp.flushBuffer();
		//System.out.println("Enviado JSON Chart");
		//System.out.println(json);

	}

}
