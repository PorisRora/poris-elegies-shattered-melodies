package com.poris.elegies.melodies;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/** Tracks confirmed playback, by instance rather than sound ID. Only notifications cross the audio thread boundary. */
final class ExternalMusicTracker<T> {
    enum State {ENDED,SILENT,AUDIBLE,OWNED}
    static final int RELEASE_GRACE=80; // Four seconds cover a record change or a short streaming reconnect.

    private record Started<T>(long generation,T sound) {}
    private final ConcurrentLinkedQueue<Started<T>> starts=new ConcurrentLinkedQueue<>();
    private final AtomicLong generation=new AtomicLong();
    private final Set<T> active=Collections.newSetFromMap(new IdentityHashMap<>());
    private long observedGeneration;
    private int grace,audible;

    /** Called only after the sound has started on an audio channel, never just on a play request. */
    void started(T sound){starts.add(new Started<>(generation.get(),sound));}

    /** May be called from a resource/audio reload thread; the client applies it before its next decision. */
    void invalidate(){generation.incrementAndGet();}

    /** Once per unpaused client music tick. Pausing must freeze the release grace as well as playback. */
    boolean tick(Function<T,State> state) {
        long currentGeneration=generation.get();
        if(observedGeneration!=currentGeneration) {
            active.clear();grace=audible=0;observedGeneration=currentGeneration;
        }
        Started<T> notice;
        while((notice=starts.poll())!=null)
            if(notice.generation()==currentGeneration)active.add(notice.sound());
        audible=0;
        for(var it=active.iterator();it.hasNext();) {
            switch(state.apply(it.next())) {
                case ENDED -> it.remove();
                case AUDIBLE -> audible++;
                case SILENT -> {} // Keep distant/moving sources so walking back into range works without a new play event.
                case OWNED -> {} // A direct Poris lease owns this channel; retain it in case ownership is released before playback ends.
            }
        }
        if(audible>0)grace=RELEASE_GRACE;
        else if(grace>0)grace--;
        return audible>0 || grace>0;
    }

    int audibleCount(){return audible;}
    int graceTicks(){return grace;}
    int trackedCount(){return active.size();}
}
