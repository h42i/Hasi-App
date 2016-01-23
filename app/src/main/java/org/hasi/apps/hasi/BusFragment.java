package org.hasi.apps.hasi;

import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

class BusStop {

    public int id;
    public String name;
    public String coords;

    public BusStop(int id, String name, String coords) {
        this.id = id;
        this.name = name;
        this.coords = coords;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " { id: " + this.id + ", name: \"" + this.name + "\", coords: \"" + this.coords + "\" }";
    }

}

class BusConnection {

    public int time; // time in minutes
    public String start_time;
    public String start_date;
    public String bus; // name/number of bus
    public String destination; // string on top of bus

    public BusConnection(int time, String start_time, String start_date, String bus, String destination) {
        this.time = time;
        this.start_time = start_time;
        this.start_date = start_date;
        this.bus = bus;
        this.destination = destination;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " { time: " + this.time + ", start_time: \"" + this.start_time + "\", start_date: \"" + this.start_date + "\", bus: \"" + this.bus + "\", destination: \"" + this.destination + "\" }";
    }

}

public class BusFragment extends Fragment {

    private String sendGetRequest(String host) {
        return HttpRequest.get(host).body();
    }

    private String sendGetRequest(String host, HashMap params) {
        return HttpRequest.get(host, params, true).body();
    }

    private String sendPostRequest(String host, HashMap params) {
        return HttpRequest.post(host, params, true).body();
    }

    private BusStop getBusStop(String busstop) {
        return this.getBusStop(busstop, false);
    }

    private BusStop getBusStop(String busstop, boolean exact) {
        BusStop result = null;
        try {
            String host = "http://efa.vrr.de/vrrstd/XSLT_STOPFINDER_REQUEST";
            HashMap<String, String> params = new HashMap<>();

            params.put("language", "de");
            params.put("outputFormat", "JSON");
            params.put("itdLPxx_usage", "origin");
            params.put("useLocalityMainStop", "true");
            params.put("doNotSearchForStops_sf", "1");
            params.put("odvSugMacro", "true");
            params.put("name_sf", busstop);

            String request = this.sendGetRequest(host, params);
            JSONObject json = new JSONObject(request);
            JSONObject stopFinder = json.getJSONObject("stopFinder");
            Object points = stopFinder.get("points");

            /* multiple possibilities */
            if(points instanceof JSONArray) {
                JSONArray points_array = (JSONArray) points;
                for(int i = 0; i < points_array.length(); i++) {
                    JSONObject point = points_array.getJSONObject(i);

                    String best = point.getString("best");
                    int id = Integer.parseInt(point.getString("stateless"));
                    String name = point.getString("name");
                    String coords = point.getJSONObject("ref").getString("coords");

                    if(exact) {
                        if(name.equals(busstop)) {
                            result = new BusStop(id, name, coords);
                            break;
                        }
                    } else {
                        if (best.equals("1")) {
                            result = new BusStop(id, name, coords);
                            break;
                        }
                    }
                }
            }

            /* just one busstop */
            if(points instanceof JSONObject) {
                JSONObject point = ((JSONObject) points).getJSONObject("point");
                int id = Integer.parseInt(point.getString("stateless"));
                String name = point.getString("name");
                String coords = point.getJSONObject("ref").getString("coords");
                result = new BusStop(id, name, coords);
            }

        } catch (JSONException e) {
            Log.e("Bus", "Failed to get busstop: " + busstop);
        }
        return result;
    }

    private ArrayList<BusConnection> getBusConnections(BusStop busstop, BusStop destination) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        String month = String.valueOf(gregorianCalendar.get(GregorianCalendar.MONTH) + 1);
        String day = String.valueOf(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
        String year = String.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR));
        String date = day + "." + month + "." + year;
        return this.getBusConnections(busstop, destination, date);
    }

    private ArrayList<BusConnection> getBusConnections(BusStop busstop, BusStop destination, String date) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date tmp_date = new Date();
        String time = dateFormat.format(tmp_date);
        return this.getBusConnections(busstop, destination, date, time);
    }

    private ArrayList<BusConnection> getBusConnections(BusStop busstop, BusStop destination, String date, String time) {
        return this.getBusConnections(busstop, destination, date, time, 10);
    }

    private ArrayList<BusConnection> getBusConnections(BusStop origin, BusStop destination, String date, String time, int limit) {
        ArrayList<BusConnection> result = new ArrayList<>();
        try {
            String host = "http://app.vrr.de/oeffi/XSLT_TRIP_REQUEST2";
            HashMap<String, String> params = new HashMap<>();

            String[] date_parts = date.split("\\.");
            date = date_parts[2] + String.format("%1$2s", date_parts[1]).replace(" ", "0") + String.format("%1$2s", date_parts[0]).replace(" ", "0");

            time = time.replace(":", "");

            params.put("outputFormat", "JSON");
            params.put("language", "de");
            params.put("stateless", "1");
            params.put("coordOutputFormat", "WGS84");
            params.put("sessionID", "0");
            params.put("requestID", "0");
            params.put("coordListOutputFormat", "STRING");
            params.put("type_origin", "stop");
            params.put("name_origin", String.valueOf(origin.id));
            params.put("type_destination", "stop");
            params.put("name_destination", String.valueOf(destination.id));
            params.put("itdDate", date);
            params.put("itdTime", time);
            params.put("calcNumberOfTrips", String.valueOf(limit));   // not shure what this is doing
            params.put("ptOptionsActive", "1");
            params.put("itOptionsActive", "1");
            params.put("changeSpeed", "normal");
            params.put("includedMeans", "checkbox");
            params.put("inclMOT_0", "on");
            params.put("inclMOT_1", "on");
            params.put("inclMOT_2", "on");
            params.put("inclMOT_3", "on");
            params.put("inclMOT_4", "on");
            params.put("inclMOT_5", "on");
            params.put("inclMOT_6", "on");
            params.put("inclMOT_7", "on");
            params.put("inclMOT_8", "on");
            params.put("inclMOT_9", "on");
            params.put("inclMOT_10", "on");
            params.put("inclMOT_11", "on");
            params.put("lineRestriction", String.valueOf(limit));
            params.put("trITMOTvalue100", "10");
            params.put("locationServerActive", "1");
            params.put("useRealtime", "1");
            params.put("nextDepsPerLeg", "1");

            String request = this.sendGetRequest(host, params);
            JSONObject json = new JSONObject(request);
            JSONArray trips = json.getJSONArray("trips");

            for(int i = 0; i < trips.length(); i++) {
                JSONObject trip = trips.getJSONObject(i);
                JSONArray legs = trip.getJSONArray("legs");
                JSONObject leg = legs.getJSONObject(0);

                int timeMinute = leg.getInt("timeMinute");
                String start_time = leg.getJSONArray("points").getJSONObject(0).getJSONObject("dateTime").getString("time");
                String start_date = leg.getJSONArray("points").getJSONObject(0).getJSONObject("dateTime").getString("date");
                JSONObject mode = leg.getJSONObject("mode");
                String bus = mode.getString("number");
                String connection_destination = mode.getString("destination");

                if(bus.equals("")) {
                    continue;
                }

                BusConnection connection = new BusConnection(timeMinute, start_time, start_date, bus, connection_destination);
                result.add(connection);
            }
        } catch (JSONException e) {
            Log.e("Bus", "Failed to get connection between " + origin.name + " and " + destination.name);
        }

        return result;
    }

    private void updateData() {
        /* get data of origin and destination */
        BusStop origin = this.getBusStop("Siegen, ZOB", true);
        BusStop destination = this.getBusStop("Siegen, P+R Siegerlandhalle", true);

        /* change textview */
        TextView bus_textview_from = (TextView) getActivity().findViewById(R.id.bus_textview_from);
        TextView bus_textview_to = (TextView) getActivity().findViewById(R.id.bus_textview_to);
        bus_textview_from.setText(origin.name);
        bus_textview_to.setText(destination.name);

        /* log results */
        Log.i("Bus", "Found busstop: " + origin.toString());
        Log.i("Bus", "Found busstop: " + destination.toString());

        /* get connection */
        final ArrayList<BusConnection> connections = this.getBusConnections(origin, destination);
        Log.i("Bus", connections.toString());

        /* get listview-element */
        ListView connection_list = (ListView) getActivity().findViewById(R.id.bus_connections);

        /* show connection */
        ArrayAdapter adapter = new ArrayAdapter<BusConnection>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, connections) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                BusConnection connection = connections.get(position);
                String str1 = connection.bus + ": " + connection.destination;
                String str2 = connection.start_time + " Uhr am " + connection.start_date;

                text1.setText(str1);
                text2.setText(str2);

                return view;
            }
        };
        connection_list.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bus_fragment, container, false);

        /* enabling network in main-thread */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /* update click-listener */
        view.findViewById(R.id.bus_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });

        return view;
    }

}
