package com.poris.elegies.melodies;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Decides what plays and when (BGM design doc, sections 6.4 and 6.5). Pure logic, fed once per client tick;
 * the game side only carries out {@link Output} calls, so every rule can be unit-tested.
 */
public final class Director {
    /** The game side: one song at a time. */
    public interface Output {
        void start(TrackList.Track track);
        /** 0..1 fade multiplier for the current song. */
        void level(float level);
        void stop();
        /** False once the song ended by itself, or the game stopped every sound (world or dimension change). */
        boolean playing();
    }

    /** Whether another mod took the music this tick: SILENCE asks for quiet (the Crown song does), OTHER plays its own. */
    public enum Yield {NONE,SILENCE,OTHER}

    /**
     * One tick as the director sees it. {@code context} is the vanilla music event Minecraft would choose
     * ("minecraft:music.game", a biome event, menu, end, dragon or credits); {@code dimension} is null on the title screen.
     * The gap between two tracks is drawn from gapMin..gapMax ticks (the frequency setting).
     */
    public record Situation(String context,String dimension,Yield yield,boolean audible,int gapMin,int gapMax) {}

    public enum Phase {IDLE,PLAYING,FADING,BREATH,YIELDED,DISABLED}
    /** {@code requested}: the song was chosen with /bgm and plays on wherever the player goes. */
    public record Status(Phase phase,TrackList.Track track,String context,int playedTicks,int delay,int here,int cooldown,boolean requested) {}
    /** Why a /bgm request cannot run now: this scene's music is off or muted, or another mod's music has the scene. */
    public enum Refusal {OFF,BUSY}

    public static final String MENU="minecraft:music.menu",END="minecraft:music.end",DRAGON="minecraft:music.dragon",
            CREDITS="minecraft:music.credits",UNDER_WATER="minecraft:music.under_water",DEEP_DARK="minecraft:music.overworld.deep_dark";
    // In ticks. Section 6.5 calls these starting values, to be tuned in playtests.
    static final int DWELL=400,DEEP_DARK_DWELL=60,PLACE_FADE=100,HARD_FADE=50,YIELD_FADE=20,BREATH=60,COOLDOWN=1800,
            NEAR_END=600,AFTER_YIELD=1200,RETURN_RAMP=20,FADE_IN=60,END_FADE=100;

    private enum After {PLACE,HARD,YIELD,REQUEST,STOP}

    private final TrackList tracks;
    private final RandomGenerator random;
    private final Output out;
    private final TrackSelector selector;
    private Phase phase=Phase.IDLE;
    private After after;
    private TrackList.Track current,requested;
    private String currentContext,context,dimension;
    private Situation last;
    private boolean entered,arrived,manual;
    private boolean enabled=true;
    private int played,delay,here,cooldown,fadeLeft,breathLeft,resumeQuiet;
    private float level=1,shown=1,fadeStep,rampStep;

    public Director(TrackList tracks,RandomGenerator random,Output out){
        this.tracks=tracks;this.random=random;this.out=out;this.selector=new TrackSelector(tracks,random);
    }

    public Status status(){return new Status(phase,current,context,played,delay,here,cooldown,manual && current!=null);}

    /** After the sound engine restarted: whatever played is gone, start again shortly. */
    public void reset(){out.stop();current=requested=null;manual=false;phase=enabled?Phase.IDLE:Phase.DISABLED;level=1;delay=40;}

    /** Hand the scene back to Minecraft immediately, without leaving a Poris song overlapping its music. */
    public void setEnabled(boolean value) {
        if(enabled==value)return;
        enabled=value;
        out.stop();current=requested=null;currentContext=null;context=null;dimension=null;
        phase=value?Phase.IDLE:Phase.DISABLED;after=null;
        entered=false;arrived=false;manual=false;
        played=delay=here=cooldown=fadeLeft=breathLeft=resumeQuiet=0;
        level=shown=1;
    }

    public void tick(Situation s) {
        if(!enabled)return;
        last=s;
        here=s.context().equals(context)?here+1:0;
        if(here==0)arrived=true;
        context=s.context();
        if(cooldown>0)cooldown--;
        if(resumeQuiet>0)resumeQuiet--;
        if(!entered || !Objects.equals(s.dimension(),dimension))enter(s.dimension());
        if(s.yield()!=Yield.NONE){yieldTo();return;}
        if(phase==Phase.YIELDED) {
            phase=Phase.IDLE;
            // External playback already has its release buffer. An ongoing battle must resume promptly.
            delay=DRAGON.equals(context)?0:AFTER_YIELD+random.nextInt(601);
            resumeQuiet=delay;arrived=false;
        }
        if(!s.audible()) {
            if(current!=null){out.stop();current=null;phase=Phase.IDLE;}
            delay=Math.max(delay,100);
            return;
        }
        switch(phase) {
            case IDLE -> idle(s);
            case PLAYING -> playing(s);
            case FADING -> fading();
            case BREATH -> {if(--breathLeft<=0)start(s);}
            case YIELDED,DISABLED -> {}
        }
    }

    /** Title screen, joining a world or changing dimension. The game has already stopped every sound by now. */
    private void enter(String dim) {
        boolean fromMenu=entered && dimension==null;
        entered=true;dimension=dim;
        out.stop();current=requested=null;manual=false;phase=Phase.IDLE;level=1;
        resumeQuiet=0;
        delay=dim==null?10+random.nextInt(30):fromMenu?100+random.nextInt(501):200+random.nextInt(401);
    }

    private void idle(Situation s) {
        if(MENU.equals(context) && dimension!=null)return;   // still loading into a world
        if(here==0 && hard(context))delay=Math.min(delay,hardDelay());
        if(arrived && here>=DWELL && resumeQuiet==0 && !hard(context)) {   // arriving somewhere epic starts its music early
            var next=selector.peek(context);
            if(next!=null && next.energy()==Energy.FULL)delay=0;
        }
        if(--delay<=0)start(s);
    }

    private void playing(Situation s) {
        if(!out.playing()){current=null;phase=Phase.IDLE;delay=gap(s);return;}
        played++;
        if(level<1)level=Math.min(1,level+rampStep);   // fading in, or back up after walking back
        shown=remaining()<END_FADE?Math.min(level,Math.max(0,remaining())/(float)END_FADE):level;   // every track fades out at its end
        out.level(shown);
        if(fits(context))return;
        if(hard(context)){fade(HARD_FADE,After.HARD);return;}
        if(manual)return;   // a requested song plays on wherever the player goes
        if(DEEP_DARK.equals(context)) {   // leave room for the sculk and the warden
            if(current.energy()!=Energy.STILL && here>=DEEP_DARK_DWELL)fade(PLACE_FADE,After.PLACE);
            return;
        }
        if(UNDER_WATER.equals(currentContext) || cooldown>0 || here<DWELL || remaining()<NEAR_END)return;
        var next=selector.peek(context);
        if(next!=null && (next.energy()==Energy.FULL)!=(current.energy()==Energy.FULL))fade(PLACE_FADE,After.PLACE);
    }

    private void fading() {
        if(after==After.PLACE && fits(context)){phase=Phase.PLAYING;rampStep=1F/RETURN_RAMP;return;}   // walked back before the fade finished
        if(!out.playing()){finishFade();return;}
        level=Math.max(0,level-fadeStep);out.level(level);
        if(--fadeLeft<=0)finishFade();
    }

    private void fade(int ticks,After then){phase=Phase.FADING;after=then;fadeLeft=ticks;level=shown;fadeStep=level/ticks;}

    private void finishFade() {
        out.stop();current=null;level=1;
        switch(after) {
            case PLACE -> {phase=Phase.BREATH;breathLeft=BREATH;cooldown=COOLDOWN;}
            case HARD -> {phase=Phase.IDLE;delay=hardDelay();}
            case YIELD -> phase=Phase.YIELDED;
            case REQUEST -> playRequested();
            case STOP -> {phase=Phase.IDLE;delay=gap(last);}
        }
    }

    private void yieldTo() {
        requested=null;
        switch(phase) {
            case PLAYING -> fade(YIELD_FADE,After.YIELD);
            case FADING -> {if(after!=After.YIELD)fade(YIELD_FADE,After.YIELD);}
            case IDLE,BREATH -> phase=Phase.YIELDED;
            case YIELDED,DISABLED -> {}
        }
        if(phase==Phase.FADING)fading();
    }

    private void start(Situation s) {
        var next=selector.draw(context);
        if(next==null){phase=Phase.IDLE;delay=gap(s);return;}   // a silent place: look again after a gap
        begin(next);
    }

    private void begin(TrackList.Track next) {
        current=next;currentContext=context;played=0;level=shown=0;rampStep=1F/FADE_IN;arrived=false;phase=Phase.PLAYING;manual=false;
        out.start(next);out.level(0);
    }

    /** Why a /bgm request cannot run now (BGM design 6.4 rule 8), or null when it can. */
    public Refusal refusal() {
        if(!enabled || last==null || context==null || !last.audible())return Refusal.OFF;
        return phase==Phase.YIELDED || phase==Phase.FADING && after==After.YIELD || last.yield()!=Yield.NONE?Refusal.BUSY:null;
    }

    /** Plays this song after a short fade and keeps it until it ends; only a hard scene or other music takes over. */
    public void play(TrackList.Track track) {
        selector.played(track.id());
        request(track);
    }

    /** This place's next song by the usual weights and history, so never the one playing now; null where the place is silent. */
    public TrackList.Track playHere() {
        var next=selector.draw(context);
        if(next!=null)request(next);
        return next;
    }

    /** Fades the current song out; the next one follows after the usual gap. False when nothing is playing. */
    public boolean stopSong() {
        requested=null;
        if(current==null || phase==Phase.FADING && after==After.YIELD)return false;
        if(phase==Phase.FADING)after=After.STOP;
        else fade(HARD_FADE,After.STOP);
        return true;
    }

    private void request(TrackList.Track track) {
        requested=track;
        switch(phase) {
            case PLAYING -> fade(HARD_FADE,After.REQUEST);
            case FADING -> {if(after!=After.YIELD)after=After.REQUEST;}   // finish this fade, then play the request
            default -> playRequested();
        }
    }

    private void playRequested() {
        var track=requested;requested=null;
        if(track==null){phase=Phase.IDLE;delay=gap(last);return;}
        begin(track);
        manual=true;
    }

    /** The current song still belongs here: same place, or this place's pool contains it too. */
    private boolean fits(String ctx){return ctx.equals(currentContext) || tracks.pool(ctx).ids().contains(current.id());}

    private int remaining(){return current.seconds()<=0?Integer.MAX_VALUE:current.seconds()*20-played;}

    private int gap(Situation s) {
        if(DRAGON.equals(context) || CREDITS.equals(context))return 0;   // vanilla loops these two
        if(MENU.equals(context))return 20+random.nextInt(581);
        return s.gapMin()+random.nextInt(s.gapMax()-s.gapMin()+1);
    }

    private int hardDelay() {
        if(DRAGON.equals(context) || CREDITS.equals(context))return 0;
        return MENU.equals(context)?10+random.nextInt(30):200+random.nextInt(401);
    }

    static boolean hard(String ctx){return MENU.equals(ctx) || END.equals(ctx) || DRAGON.equals(ctx) || CREDITS.equals(ctx);}

}
