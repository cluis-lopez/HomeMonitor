The client side of the app. It's made of 5 HTML files, one per page of the application.
All include JQuery and Bootstrap frameworks:

- index.html: the landing page that shows the dashboard with the last sample stored at Google,
the last picture and a chart showing the evolution of the stored data. Uses javascript
[MorrisCharts](http://morrisjs.github.io/morris.js/) for chartingThey and makes two AJAX calls to
backend servlets to retrieve the data packed into JSON objects:

  - LastUpdate: [see LastUpdate.java](https://github.com/cluis-lopez/HomeMonitor/blob/master/HomeMonitor/src/main/java/com/clopez/homemonitor/LastUpdate.java)
  returns a JSON with the Timestamp of the last stored sample along with the sampled values
  including (if any) the URL of the last pictured stored in Google Storage
  - ChartData: [see ChartData.java](https://github.com/cluis-lopez/HomeMonitor/blob/master/HomeMonitor/src/main/java/com/clopez/homemonitor/ChartData.java)
  retrieves the last samples (limit is 100) packed in JSON to create the charts
