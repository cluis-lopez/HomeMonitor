<%@page import="java.io.IOException"%>
<%@page import="java.io.PrintStream"%>
<%@page import="javax.servlet.http.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.List"%>
<%@page import="java.util.TreeSet"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.TimeZone"%>
<%@page import="com.google.appengine.api.datastore.DatastoreServiceFactory"%>
<%@page import="com.google.appengine.api.datastore.DatastoreService"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.google.appengine.api.datastore.EntityNotFoundException"%>
<%@page import="com.google.appengine.api.datastore.Key"%>
<%@page import="com.google.appengine.api.datastore.KeyFactory"%>
<%@page import="com.google.appengine.api.datastore.Query"%>
<%@page import="com.google.appengine.api.datastore.PreparedQuery"%>
<%@page import="com.google.appengine.api.datastore.Query.Filter"%>
<%@page import="com.google.appengine.api.datastore.Query.FilterOperator"%>
<%@page import="com.google.appengine.api.datastore.Query.FilterPredicate"%>
<%@page import="com.google.appengine.api.datastore.Entities"%>
<%@page import="com.google.appengine.api.datastore.Query.SortDirection"%>
<%@page import="com.google.appengine.api.datastore.FetchOptions"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<h1> Pictures not null</h1>
<%
Date d = new Date();
long l = d.getTime();
SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd-MM-YYYY z kk:mm:ss");
sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
DatastoreService ds= DatastoreServiceFactory.getDatastoreService(); 
Query q = new Query("Samples");

q.setFilter(FilterOperator.NOT_EQUAL.of("Pict", null));
PreparedQuery pq = ds.prepare(q);

for (Entity result : pq.asIterable())   

   {
        Double picture = (Double) result.getProperty("Pict");
%>
        <LI>URL: <%=picture %>
<%
}

%>
<h1>The last Picture</h1>
<%
Query q2 = new Query ("Samples");
q2.setFilter(FilterOperator.NOT_EQUAL.of("Pict", null));
//q2.addSort("Pict", SortDirection.ASCENDING);
//q2.addSort("__key__", SortDirection.DESCENDING);
PreparedQuery pq2 = ds.prepare(q2);
//Key key = ents.get(0).getKey();
TreeSet<Key> ts = new TreeSet<Key>();

for (Entity e : pq2.asIterable()) {
	ts.add(e.getKey());
%>
<LI>Keys: <%= e.getKey() %>
<%
}

// The last key is the picture inserted

Key key = ts.last();
Entity ent = ds.get(key);
	Double temp = (Double) ent.getProperty("Temp");
	Double light = (Double) ent.getProperty("Light");
	Double url = (Double) ent.getProperty("Pict");
	String k2 = (ent.getKey()).getName();
	Date d2 = new Date(Long.parseLong(k2));
	String fecha = sdf.format(d2);
%>

<LI><%=fecha %>               URL: <%=url %>                   Temp: <%=temp %>           Light:<%=light %>

<h1>
	The last Datastore entry
</h1>

<%
Query q3 =  new Query("Samples").setKeysOnly();
q3.addSort("__key__", SortDirection.DESCENDING);
List<Entity> ents2 = ds.prepare(q3).asList(FetchOptions.Builder.withLimit(1));
key = ents2.get(0).getKey();

ent = ds.get(key);
        temp = (Double) ent.getProperty("Temp");
        light = (Double) ent.getProperty("Light");
        k2 = (ent.getKey()).getName();
        d2 = new Date(Long.parseLong(k2));
        fecha = sdf.format(d2);
%>
        <LI><%=fecha %>"                    Temp: "<%=temp %>"           Light:"<%=light %>
</body>
</html>