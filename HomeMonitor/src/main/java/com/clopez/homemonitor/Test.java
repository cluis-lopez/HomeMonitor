package com.clopez.homemonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

/**
 * Servlet implementation class Test
 */
public class Test extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
  
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//String datos = req.getParameter("JSON");
		//Gson json = new Gson();
		DatastoreService ds =  DatastoreServiceFactory.getDatastoreService();
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		
		Query q = new Query("Samples").setKeysOnly();
		q.addSort("__key__", SortDirection.ASCENDING);
		List<Entity> ents = ds.prepare(q).asList(FetchOptions.Builder.withDefaults());
		
		System.out.println("Numero de entidades 'Sample' en el Datastore:" + ents.size());
		
		if (ents.size()>100){
			System.out.println("Vamos a borrar: " + (ents.size()-100) + " entidades");
			for (int i = ents.size(); i>100; i--){
				try {
					Date date = new Date(Long.parseLong(((ents.get(i)).getKey()).getName()));
					String ts = sdf.format(date);
					Entity e = ds.get(ents.get(i).getKey());
					System.out.println(ts +"\t" + ents.get(i).getKey() +"\t Temp: " + e.getProperty("Temp") + "\t Hum: " + e.getProperty("Hum"));
				} catch (EntityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

}
