package com.clopez.homemonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Servlet implementation class Alerts
 */
public class Alerts extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Posting to this servlet assumes the body contains JSON fotmatted
		// alerts to store in the Datasore
		// Only one alert is sent with each POST

		String datos = req.getParameter("Alerta");

		Gson gson = new Gson();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

		// Now update the datastore with the data received in the JSON
		TreeMap<String, Object> map = gson.fromJson(datos, new TypeToken<TreeMap<String, Object>>() {}.getType());
		
		// System.out.println(map);
		
		double d = (double) map.get("Timestamp");
		long l = (new Double(d)).longValue() * 1000;

		Entity sample = new Entity("Alerts", String.valueOf(l));
		sample.setProperty("Level", map.get("Level"));
		sample.setProperty("Message", map.get("Message"));
		sample.setProperty("Type", map.get("Type"));
		sample.setProperty("Data", map.get("Data"));
		ds.put(sample);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Getting this servlet returns a JSON formatted list of Alerts read
		// from the Datastore
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		LinkedList<Map<String, Object>> lista = new LinkedList<Map<String, Object>>();
		String error = "";
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		Date date;
		String ts;

		Query q = new Query("Alerts").setKeysOnly();
		q.addSort("__key__", SortDirection.DESCENDING);
		List<Entity> ents = ds.prepare(q).asList(FetchOptions.Builder.withLimit(30));

		Entity e;

		if (ents.size() == 0) { // No entries in the datastore ??
			error = "No Alerts";
		} else {
			for (int i = 0; i < ents.size(); i++) { // Reverse the order to
													// get last item in
													// DS first
				try {
					e = ds.get((ents.get(i)).getKey());
					date = new Date(Long.parseLong((e.getKey()).getName()));
					ts = sdf.format(date);
					// System.out.println("Timestamp: "+ts);
					Map<String, Object> mapa = new HashMap<String, Object>();
					mapa.put("Ts", ts);
					mapa.put("Level", e.getProperty("Level"));
					mapa.put("Type", e.getProperty("Type"));
					mapa.put("Message", e.getProperty("Message"));
					mapa.put("Data", e.getProperty("Data"));
					// System.out.println(mapa.toString());
					lista.add(mapa);
				} catch (EntityNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}
		// Send JSON to the Javascript consumer

		Gson gson = new Gson();
		String json = gson.toJson(lista);

		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("cache-control", "no-cache");
		resp.getWriter().write(json);
		resp.flushBuffer();

	}
}
