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
 * Servlet implementation class GetHistoric
 */
public class GetHistoric extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		LinkedList<Map<String, Object>> lista = new LinkedList<Map<String, Object>>();

		String error;

		// Get the last 30 samples stored

		Query q = new Query("Historic").setKeysOnly();
		q.addSort("__key__", SortDirection.DESCENDING);
		List<Entity> ents = ds.prepare(q).asList(FetchOptions.Builder.withLimit(30));

		Entity e;

		if (ents.size() == 0) { // No entries in the datastore ??
			error = "Not historic records in the datastore";
		} else {
			for (int i = 0; i < ents.size(); i++) { // Reverse the order to
															// get last item in
															// DS last
				try {
					e = ds.get((ents.get(i)).getKey());
					Map<String, Object> mapa = new HashMap<String, Object>();
					mapa.put("Ts", e.getKey().getName());
					mapa.put("Temp", (Double) e.getProperty("Temp"));
					mapa.put("Hum", (Double) e.getProperty("Hum"));
					mapa.put("Light", (Double) e.getProperty("Light"));
					// System.out.println(mapa.toString());
					lista.add(mapa);
				} catch (EntityNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}

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
