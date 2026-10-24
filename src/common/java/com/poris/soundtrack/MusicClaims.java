package com.poris.soundtrack;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

/** Explicit, instance-scoped ownership. Synchronized because source-start callbacks run on the audio executor. */
final class MusicClaims<T> {
    static final int START_TIMEOUT=600;
    private static final class Claim {int references=1,waiting;boolean started;}
    private final Map<T,Claim> claims=new IdentityHashMap<>();

    synchronized Runnable acquire(T sound) {
        var existing=claims.get(sound);
        if(existing!=null)existing.references++;
        else claims.put(sound,new Claim());
        var claim=claims.get(sound);
        return new Runnable() {
            private boolean released;
            @Override public void run() {
                synchronized(MusicClaims.this) {
                    if(released)return;
                    released=true;
                    if(claims.get(sound)==claim && --claim.references==0)claims.remove(sound);
                }
            }
        };
    }

    synchronized void started(T sound){var c=claims.get(sound);if(c!=null)c.started=true;}
    synchronized boolean contains(T sound){return claims.containsKey(sound);}
    synchronized void clear(){claims.clear();}

    /** Claims reserve before async decode; failed starts time out, but confirmed long/looped songs never do. */
    synchronized boolean tick(Predicate<T> live,Predicate<T> stopped) {
        claims.entrySet().removeIf(e->{
            var c=e.getValue();
            return stopped.test(e.getKey()) || (c.started?!live.test(e.getKey()):++c.waiting>=START_TIMEOUT);
        });
        return !claims.isEmpty();
    }
}
