package com.clopez.homemonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


@SuppressWarnings("serial")
public class Old_HomeMonitorServlet extends HttpServlet {
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
		// If so, archive older (>100) summarizing in "Historic" entities, one per day
		
		Query q = new Query("Samples").setKeysOnly();
		q.addSort("__key__", SortDirection.DESCENDING);
		List<Entity> ents = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
		if (ents.size()>100){
			// Cleanup the "Samples" datastore
			// Definimos un HashMap para almacenar los valores
			Map<String, Double> values = new HashMap<String, Double>();
			// Y otro HashMap que indexa por la fecha (YYYY-MM-dd)
			Map<String, Map<String, Double>> historic = new HashMap<String, Map<String, Double>>();
			
			for (int i = 100; i< ents.size(); i++){
				Double temp = 0.0, light = 0.0 , hum = 0.0;
				Key tkey = ents.get(i).getKey(); // tkey es el timestamp de esta Entity
				Date date = new Date(Long.parseLong(tkey.getName()));
				String ts = sdf.format(date);
				
				Entity ent;
				
				try {
					ent = ds.get(tkey);
					temp = temp + (Double) ent.getProperty("Temp");
					light = light + (Double) ent.getProperty("Light");
					hum = hum + (Double) ent.getProperty("Hum");
					
					if (! historic.containsKey(ts)){ //No record for this date
						values.put("Temp", temp); values.put("Hum", hum); values.put("Light", light); values.put("num", 0.0);
					} else { // A record already exists for this date
						values.put("Temp", values.get("Temp")+temp);
						values.put("Hum", values.get("Hum")+hum);
						values.put("Light", values.get("Light")+light);
						values.put("num", values.get("num")+1);
					}
					// Let's delete this old entry
					ds.delete(tkey);
					// And insert the values in the "historic" map
					historic.put(ts, values);
					
				} catch (EntityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Now "historic" map should be a list of keys = date (YYYY-MM-dd format)
			// and values a map with the sum of temps, hum, etc. plus number of entries
			// Calculate the average for each entry
			
			for (String key: historic.keySet()){
				Map<String, Double> map = historic.get(key);
				
				Double temp = map.get("Temp")/map.get("num");
				Double hum = map.get("Hum")/map.get("num");
				Double light = map.get("Light")/map.get("num");
				
				// Now save an entity of kind "Historic", keyed with the day date (YYYY-MM-dd)
				// and the average temp, humidity, etc, data
				
				Entity hist = new Entity("Historic", key);
				hist.setProperty("Temp", temp);
				hist.setProperty("Hum", hum);
				hist.setProperty("Light", light);
				ds.put(hist);
				}
				
			}
			
			/** Query tq = new Query("Historic").setKeysOnly().setFilter(FilterOperator.EQUAL.of("__key__", ts) );
			Entity e = ds.prepare(tq).asSingleEntity(); // Chequeamos si ya existe una entidad con ese "key" que corresponde a un día del año
			
			if (e != null){
				temp = (Double) e.getProperty("Temp");
				light = (Double) e.getProperty("Light");
				hum = (Double) e.getProperty("Hum");
				
			} **/
		
		
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
