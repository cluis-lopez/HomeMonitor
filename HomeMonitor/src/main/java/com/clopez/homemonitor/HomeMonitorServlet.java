package com.clopez.homemonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("serial")
public class HomeMonitorServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		int n = Integer.parseInt(req.getParameter("NumSamples"));
		String datos = req.getParameter("JSON");
		// System.out.println("Numero de muestras: "+n);

		Gson gson = new Gson();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

		// Check first if Datastore keeps more than 100 "Sample" entries.
		// If so, archive the older (>100) summarizing in "Historic" entities, one per day

		Query q = new Query("Samples").setKeysOnly();
		q.addSort("__key__", SortDirection.ASCENDING);
		List<Entity> ents = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
		
		//System.out.println("Numero de entidades:" + ents.size());

		if (ents.size() > 100) {

			for (int i = 0; i < ents.size()-100 ; i++) {
				Date date = new Date(Long.parseLong(((ents.get(i)).getKey()).getName()));
				String ts = sdf.format(date);
				double temp = 0.0;
				double hum = 0.0;
				double light = 0.0;
				// Retrieve the values of this "older" entry in the datastore
				try {
					Entity ent = ds.get(ents.get(i).getKey());
					temp = (double) ent.getProperty("Temp");
					hum = (double) ent.getProperty("Hum");
					light = (double) ent.getProperty("Light");
					//System.out.println("Entradas older");
					//System.out.println("Key: "+ ent.getKey());
				} catch (EntityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Check if an Historic entity with this date already exists
				Key k = KeyFactory.createKey("Historic", ts);
				Query tq = new Query("Historic").setKeysOnly().setFilter(FilterOperator.EQUAL.of("__key__", k) );
				Entity hist = ds.prepare(tq).asSingleEntity();
				
				//System.out.println("Encontrada la entidad Historic: " + hist);
				
				if (hist == null){
					// No entry with that date so we create one
					//System.out.println("Creando la entidad con Temp:" + temp);
					hist = new Entity("Historic", ts);
					hist.setProperty("Temp", temp);
					hist.setProperty("Hum", hum);
					hist.setProperty("Light", light);
					// Insert the historic entry
					ds.put(hist);
					// And delete the older
					ds.delete(ents.get(i).getKey());
				} else {
					// There's already an entry in the datastore
					// for that date
					try {
						Entity e = ds.get(hist.getKey());
						double temp2 = (double) e.getProperty("Temp");
						double hum2 = (double) e.getProperty("Hum");
						double light2 = (double) e.getProperty("Light");
						e.setProperty("Temp", (temp+temp2)/2);
						e.setProperty("Hum", (hum+hum2)/2);
						e.setProperty("Light", (light+light2)/2);
						ds.put(e);
						ds.delete(ents.get(i).getKey());
					} catch (EntityNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} 
			}
		}

		// Now update the datastore with the data received in the JSON 
		TreeMap<String, Object> map = gson.fromJson(datos, new TypeToken<TreeMap<String, Object>>(){}.getType());
		for(String key: map.keySet()){
			Map<String, Object> map2 = gson.fromJson(map.get(key).toString(),  new TypeToken<Map<String, Object>>(){}.getType());
			double d = (double) map2.get("Timestamp");
			long l = (new Double(d)).longValue()*1000;

			Entity sample = new Entity("Samples", String.valueOf(l));
			sample.setProperty("Temp", map2.get("Temp"));
			sample.setProperty("Hum", map2.get("Hum"));
			sample.setProperty("Light", map2.get("Light"));
			sample.setProperty("Pict", map2.get("Pict"));
			ds.put(sample);
		};

		resp.setContentType("text/plain");
		resp.getWriter().println("OK from backend servlet");
	}
}
