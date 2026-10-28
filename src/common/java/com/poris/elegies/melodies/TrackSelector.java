package com.poris.elegies.melodies;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.random.RandomGenerator;

/** Weighted selection with history shared by every biome, including pools that overlap. */
final class TrackSelector {
    private final TrackList tracks;
    private final RandomGenerator random;
    private final Deque<String> recent=new ArrayDeque<>();
    private String pendingPool,pendingId;

    TrackSelector(TrackList tracks,RandomGenerator random){this.tracks=tracks;this.random=random;}

    /** Keep the preview stable: transition checks must not redraw a different song every tick. */
    TrackList.Track peek(String context) {
        var pool=tracks.pool(context);
        if(pendingId!=null && pool.key().equals(pendingPool) && pool.ids().contains(pendingId))return tracks.track(pendingId);
        pendingPool=pool.key();pendingId=null;
        double total=0;
        for(String id:pool.ids())total+=weight(pool,id);
        if(total==0)return null;
        double draw=random.nextDouble()*total;
        for(String id:pool.ids()) {
            double weight=weight(pool,id);
            if(weight==0)continue;
            pendingId=id;
            if((draw-=weight)<0)break;
        }
        return tracks.track(pendingId);
    }

    TrackList.Track draw(String context) {
        var next=peek(context);
        pendingPool=null;pendingId=null;
        if(next!=null)played(next.id());
        return next;
    }

    /** Remembers a song as heard; one chosen with /bgm counts too, so the next draw avoids it. */
    void played(String id) {
        recent.addFirst(id);
        while(recent.size()>3)recent.removeLast();
        pendingPool=null;pendingId=null;
    }

    private double weight(TrackList.Pool pool,String id) {
        // A one-song theme may repeat after its normal gap; never exclude every candidate.
        if(pool.ids().size()>1 && id.equals(recent.peekFirst()))return 0;
        return tracks.weight(pool.key(),id)*(recent.contains(id)?0.25:1.0);
    }
}
