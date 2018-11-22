package edu.gvsu.cis.cis656.clock;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VectorClock implements Clock {

    // suggested data structure ...
    Map<String,Integer> clock = new Hashtable<String,Integer>();


    @Override
    public void update(Clock other) {
        for(String time : ((VectorClock) other).clock.keySet()) {
                if(clock.containsKey(time)) {
                    clock.put(time, Math.max(clock.get(time), other.getTime(Integer.parseInt(time))));
                } else {
                    clock.put(time, other.getTime((Integer.parseInt(time))));
                }
    }
}

    @Override
    public void setClock(Clock other) {
        for(int i = 0; i < clock.size(); i++) {
            clock.put(Integer.toString(i), other.getTime(i));
        }
    }

    @Override
    public void tick(Integer pid) {
        clock.put(pid.toString(), clock.get(Integer.toString(pid)) + 1);
    }

    @Override
    public boolean happenedBefore(Clock other) {
        for (String key : clock.keySet()) {
           if(clock.get(key) > other.getTime(Integer.parseInt(key))) {
               return false;
            }
        }
        return true;
    }

    public String toString() {
        AtomicReference<String> s = new AtomicReference<>("");
        if(!clock.isEmpty()) {
            clock.forEach((key, value) -> s.updateAndGet(v -> v + '"' + key + '"' + ":" + clock.get(key) + ","));
            String y = String.valueOf(s).substring(0, String.valueOf(s).length() - 1);
            return "{" + y + "}";
        }
        else {
            return "{" + "}";
        }
    }

    @Override
    public void setClockFromString(String s) {
       if (s.matches("\\{(\"\\d+\":\\d+,?)+}")) {
            clock.clear();
            Pattern pattern = Pattern.compile("\"\\d\"");
            Matcher matcher = pattern.matcher(s);
            ArrayList<String> y = new ArrayList<>();
            ArrayList<String> x = new ArrayList<>();
            while(matcher.find()) {
                y.add(s.substring(matcher.start() + 1, matcher.end() - 1));
            }
            pattern = Pattern.compile(":\\d+");
            matcher = pattern.matcher(s);
            while(matcher.find()) {
                x.add(s.substring(matcher.start() + 1, matcher.end()));
            }
            for(int i = 0; i < y.size(); i++) {
                clock.put(y.get(i), Integer.parseInt(x.get(i)));
            }
        }
        else if (s.matches("\\{}")) {
            clock.clear();
        }
    }

    @Override
    public int getTime(int p) {
        return clock.getOrDefault(Integer.toString(p), 0);
    }

    @Override
    public void addProcess(int p, int c) {

        clock.put(Integer.toString(p) , c);
    }
}
