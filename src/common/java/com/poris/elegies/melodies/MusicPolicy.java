package com.poris.elegies.melodies;

import java.util.List;
import java.util.regex.Pattern;

/** Immutable policy snapshot shared with audio request listeners; no Minecraft or mutable config access. */
record MusicPolicy(boolean enabled,Mode mode,List<String> whitelist) {
    enum Mode {NORMAL,YIELD,EXCLUSIVE,WHITELIST}
    static final MusicPolicy DISABLED=new MusicPolicy(false,Mode.YIELD,List.of());
    static final List<String> DEFAULT_WHITELIST=List.of();
    private static final Pattern ENTRY=Pattern.compile("[a-z0-9_.-]+:(\\*|[a-z0-9_./-]+)");

    MusicPolicy {whitelist=List.copyOf(whitelist);}

    static boolean validEntry(Object entry){return entry instanceof String s && ENTRY.matcher(s).matches();}

    boolean whitelisted(String id) {
        if(id==null)return false;
        for(String entry:whitelist) {
            if(!validEntry(entry))continue;
            if(entry.endsWith(":*")) {
                if(id.startsWith(entry.substring(0,entry.length()-1)))return true;
            } else if(id.equals(entry))return true;
        }
        return false;
    }

    boolean blocks(String id,String source) {
        return restrictsExternal() && MusicHandoff.accepts(id,source) && !(mode==Mode.WHITELIST && whitelisted(id));
    }

    boolean restrictsExternal(){return enabled && (mode==Mode.EXCLUSIVE || mode==Mode.WHITELIST);}
    boolean overlapsExternal(){return enabled && mode==Mode.NORMAL;}

    /** Exclusive mode ignores bare silence requests, which may belong to a theme we have just blocked. */
    Director.Yield selectionYield(Director.Yield requested,String chosenId) {
        if(!restrictsExternal())return requested;
        // Explicitly whitelisting a vanilla theme also allows it instead of our replacement.
        return mode==Mode.WHITELIST && whitelisted(chosenId)?Director.Yield.OTHER:Director.Yield.NONE;
    }
}
