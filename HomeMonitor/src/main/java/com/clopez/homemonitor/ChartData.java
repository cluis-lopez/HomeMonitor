package com.clopez.homemonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

/**
 * Servlet implementation class ChartData
 */
public class ChartData extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		LinkedList<Map<String, Object>> lista = new LinkedList<Map<String, Object>>();

		String error;

		// Get the last 100 samples stored

		Query q = new Query("Samples").setKeysOnly();
		q.addSort("__key__", SortDirection.DESCENDING);
		List<Entity> ents = ds.prepare(q).asList(FetchOptions.Builder.withLimit(100));

		Entity e;
		
		if (ents.size() < 5){ //No entries in the datastore ??
			error = "Not enough data stored in the cloud";
		} else {
			for (int i = ents.size()-1; i >= 0; i--) { //Reverse the order to get last item in DS last
				try {
					e = ds.get((ents.get(i)).getKey());
					Map<String, Object> mapa = new HashMap<String, Object>();
					mapa.put("Ts", Long.parseLong((e.getKey()).getName()));
					mapa.put("Temp", e.getProperty("Temp"));
					mapa.put("Hum", e.getProperty("Hum"));
					mapa.put("Light", e.getProperty("Light"));
					//System.out.println(mapa.toString());
					lista.add(mapa);
				} catch (EntityNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		}
		//System.out.println("Lista: "+lista.toString());

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
