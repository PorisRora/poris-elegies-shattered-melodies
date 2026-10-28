package com.poris.elegies.melodies;

/** Source policy and MusicManager ownership, independent of Minecraft so the arbitration can be tested. */
final class MusicHandoff<T> {
    enum Action {PASS,SUPPRESS_VANILLA,KEEP_EXTERNAL}
    record Decision(Director.Yield yield,Action action) {}

    private T managed;
    private boolean managedExternal;
    private String previousExternalChoice;

    static boolean accepts(String id,String source) {
        return (source.equals("MUSIC") || source.equals("RECORDS"))
                && !id.startsWith(ShatteredMelodies.MOD_ID+":")
                && !id.startsWith("minecraft:block.note_block.");
    }

    /** A spatial source uses its declared attenuation radius; relative and non-attenuated themes are global. */
    static boolean inRange(boolean relative,boolean attenuated,double distanceSquared,double radius) {
        return relative || !attenuated || distanceSquared<radius*radius;
    }

    /**
     * Observe BEFORE changing SelectMusicEvent. The previous choice identifies a newly started MusicManager song,
     * including another mod selecting a vanilla recording for one tick. Its ownership lasts until that instance ends.
     */
    void observe(T playing,String playingId,boolean externalChoice,String selectedId) {
        if(playing!=managed) {
            managed=playing;
            managedExternal=playing!=null && (playingId.equals(previousExternalChoice)
                    || !playingId.startsWith("minecraft:music."));
        }
        if(playing!=null && externalChoice && playingId.equals(selectedId))managedExternal=true;
        previousExternalChoice=externalChoice?selectedId:null;
    }

    boolean vanillaManaged(T sound){return sound!=null && sound==managed && !managedExternal;}
    boolean externalManaged(){return managed!=null && managedExternal;}
    void managedEnded(){managedExternal=false;}

    static Decision decide(Director.Yield selectionYield,boolean occupied,boolean externalManagedActive) {
        if(selectionYield!=Director.Yield.NONE)return new Decision(selectionYield,Action.PASS);
        if(externalManagedActive)return new Decision(Director.Yield.OTHER,Action.KEEP_EXTERNAL);
        // A directly played boss theme is independent of MusicManager. Still suppress vanilla, or it starts underneath it.
        return new Decision(occupied?Director.Yield.OTHER:Director.Yield.NONE,Action.SUPPRESS_VANILLA);
    }

    /** Overlap changes our own decision, not the external player's MusicManager ownership. */
    static Decision decide(Director.Yield selectionYield,boolean occupied,boolean externalManagedActive,boolean overlap) {
        var decision=decide(selectionYield,occupied,externalManagedActive);
        return overlap?new Decision(Director.Yield.NONE,decision.action()):decision;
    }

    void reset(){managed=null;managedExternal=false;previousExternalChoice=null;}
}
