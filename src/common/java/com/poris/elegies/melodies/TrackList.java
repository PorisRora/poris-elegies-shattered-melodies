package com.poris.elegies.melodies;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** tracks.json plus the generated lengths: the tracks with audio, and which of them each vanilla music event plays. */
public final class TrackList {
    public record Track(String id,String titleJa,String titleZh,Energy energy,float volume,int seconds) {}
    /** The tracks to draw from for one music event; {@code key} names its weight table. */
    public record Pool(String key,List<String> ids) {}

    public static final TrackList EMPTY=new TrackList(Map.of(),Map.of(),List.of());
    static final String FALLBACK="fallback";
    private final Map<String,Track> tracks;
    private final Map<String,List<String>> pools;
    private final List<String> fallback;
    private final Map<String,Map<String,Integer>> weights;

    TrackList(Map<String,Track> tracks,Map<String,List<String>> pools,List<String> fallback) {
        this(tracks,pools,fallback,Map.of());
    }

    TrackList(Map<String,Track> tracks,Map<String,List<String>> pools,List<String> fallback,
              Map<String,Map<String,Integer>> weights) {
        // Keep the file's order: /poris bgm list shows the tracks by number. Map.copyOf would scramble it.
        this.tracks=Collections.unmodifiableMap(new LinkedHashMap<>(tracks));this.pools=Map.copyOf(pools);this.fallback=List.copyOf(fallback);
        var copy=new LinkedHashMap<String,Map<String,Integer>>();
        weights.forEach((key,value)->copy.put(key,Map.copyOf(value)));
        this.weights=Map.copyOf(copy);
    }

    /**
     * Keys starting with "_" are notes for people. Tracks failing {@code playable} (no audio yet) are dropped;
     * pools may still name them, so a pool left without audio falls back like a missing one.
     */
    public static TrackList parse(Reader tracksJson,Reader lengthsJson,Predicate<String> playable) {
        JsonObject root=JsonParser.parseReader(tracksJson).getAsJsonObject();
        JsonObject lengths=lengthsJson==null?new JsonObject():JsonParser.parseReader(lengthsJson).getAsJsonObject();
        var declared=new LinkedHashMap<String,Track>();
        for(var e:root.getAsJsonObject("tracks").entrySet()) {
            if(e.getKey().startsWith("_"))continue;
            JsonObject t=e.getValue().getAsJsonObject();
            declared.put(e.getKey(),new Track(e.getKey(),text(t,"title_ja"),text(t,"title_zh"),Energy.parse(text(t,"energy")),
                    t.has("volume")?t.get("volume").getAsFloat():1F,lengths.has(e.getKey())?(int)lengths.get(e.getKey()).getAsDouble():0));
        }
        var pools=new LinkedHashMap<String,List<String>>();
        for(var e:root.getAsJsonObject("pools").entrySet())
            if(!e.getKey().startsWith("_"))pools.put(e.getKey(),ids(e.getValue().getAsJsonArray(),declared,e.getKey()));
        var fallback=ids(root.getAsJsonArray("fallback"),declared,"fallback");
        var weights=new LinkedHashMap<String,Map<String,Integer>>();
        if(root.has("weights"))for(var e:root.getAsJsonObject("weights").entrySet()) {
            if(e.getKey().startsWith("_"))continue;
            var pool=FALLBACK.equals(e.getKey())?fallback:pools.get(e.getKey());
            if(pool==null)throw new IllegalArgumentException("Unknown weighted pool: "+e.getKey());
            var values=new LinkedHashMap<String,Integer>();
            for(var entry:e.getValue().getAsJsonObject().entrySet()) {
                double value=entry.getValue().getAsDouble();
                if(!pool.contains(entry.getKey()) || !Double.isFinite(value) || value<1 || value>100 || value!=(int)value)
                    throw new IllegalArgumentException("Invalid weight in "+e.getKey()+": "+entry);
                values.put(entry.getKey(),(int)value);
            }
            weights.put(e.getKey(),values);
        }
        var tracks=new LinkedHashMap<String,Track>();
        declared.forEach((id,t)->{if(playable.test(id))tracks.put(id,t);});
        return new TrackList(tracks,pools,fallback,weights);
    }

    private static String text(JsonObject o,String key){return o.has(key)?o.get(key).getAsString():"";}

    private static List<String> ids(JsonArray array,Map<String,Track> declared,String where) {
        var ids=new ArrayList<String>();
        for(var e:array) {
            String id=e.getAsString();
            if(!declared.containsKey(id))throw new IllegalArgumentException(where+" names an unknown track: "+id);
            if(ids.contains(id))throw new IllegalArgumentException(where+" repeats a track: "+id);
            ids.add(id);
        }
        return ids;
    }

    /** The short name /bgm uses: the id up to its first underscore ("02c" for "02c_worldshefoughtfor"). */
    public static String number(String id){int cut=id.indexOf('_');return cut<0?id:id.substring(0,cut);}

    public Track track(String id){return tracks.get(id);}
    public Map<String,Track> tracks(){return tracks;}
    public Map<String,List<String>> pools(){return pools;}
    public List<String> fallback(){return fallback;}
    public int weight(String pool,String id){return weights.getOrDefault(pool,Map.of()).getOrDefault(id,1);}

    /** An explicitly empty pool means silence (warped forest); a missing pool, or one without audio yet, uses the fallback. */
    public Pool pool(String context) {
        var declared=pools.get(context);
        if(declared!=null && declared.isEmpty())return new Pool(context,List.of());
        var own=declared==null?List.<String>of():declared.stream().filter(tracks::containsKey).toList();
        return own.isEmpty()?new Pool(FALLBACK,fallback.stream().filter(tracks::containsKey).toList()):new Pool(context,own);
    }
}
