package com.poris.elegies.melodies;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * /poris melodies play, here, next, stop, now and list, plus unbound keys for here, next and now (BGM design 6.4 rule 8).
 * Client commands, so they work in multiplayer and without cheats; the main mod's server-side /poris cast and /poris ritual
 * still reach the server, because NeoForge forwards client commands it cannot parse. A track is named by its number,
 * the part of its id before the first underscore ("01", "02c", "30b"), or by its full id.
 */
final class SoundtrackCommands {
    private static final String KEY=ShatteredMelodies.MOD_ID+".command.";
    private static final KeyMapping HERE=key("here"),NEXT=key("next"),NOW=key("now");

    private static KeyMapping key(String name) {
        return new KeyMapping("key."+ShatteredMelodies.MOD_ID+"."+name,InputConstants.UNKNOWN.getValue(),"key.categories."+ShatteredMelodies.MOD_ID);
    }

    static void registerKeys(RegisterKeyMappingsEvent event){event.register(HERE);event.register(NEXT);event.register(NOW);}

    static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("poris").then(subcommands(Commands.literal("melodies"))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> subcommands(LiteralArgumentBuilder<CommandSourceStack> root) {
        return root
                .then(Commands.literal("play").then(Commands.argument("track",StringArgumentType.word())
                        .suggests((c,builder)->SharedSuggestionProvider.suggest(
                                SoundtrackClient.tracks().tracks().keySet().stream().map(TrackList::number),builder))
                        .executes(SoundtrackCommands::play)))
                .then(Commands.literal("here").executes(c->here(ok(c),c.getSource()::sendFailure)))
                .then(Commands.literal("next").executes(c->here(ok(c),c.getSource()::sendFailure)))
                .then(Commands.literal("stop").executes(SoundtrackCommands::stop))
                .then(Commands.literal("now").executes(c->{ok(c).accept(now());return 1;}))
                .then(Commands.literal("list").executes(SoundtrackCommands::list));
    }

    /** The keys answer in the action bar instead of the chat. */
    static void tick(ClientTickEvent.Post event) {
        var player=Minecraft.getInstance().player;
        if(player==null)return;
        Consumer<Component> bar=message->player.displayClientMessage(message,true);
        while(HERE.consumeClick())here(bar,bar);
        while(NEXT.consumeClick())here(bar,bar);
        while(NOW.consumeClick())bar.accept(now());
    }

    private static Consumer<Component> ok(CommandContext<CommandSourceStack> c){return message->c.getSource().sendSuccess(()->message,false);}

    private static TrackList.Track find(String name) {
        String wanted=name.toLowerCase(Locale.ROOT);
        for(var t:SoundtrackClient.tracks().tracks().values())
            if(t.id().equals(wanted) || TrackList.number(t.id()).equals(wanted))return t;
        return null;
    }

    private static Component title(TrackList.Track t) {
        return Component.literal(TrackList.number(t.id())+"  ").append(TrackTitles.name(t));
    }

    /** The director when it can take a request; otherwise null, and the reason goes to {@code fail}. */
    private static Director accepting(Consumer<Component> fail) {
        var director=SoundtrackClient.director();
        var refusal=director==null?Director.Refusal.OFF:director.refusal();
        if(refusal==null)return director;
        fail.accept(Component.translatable(KEY+(refusal==Director.Refusal.BUSY
                ?SoundtrackClient.firstPartyMusicActive()?"busy.first_party":"busy":"off")));
        return null;
    }

    private static int play(CommandContext<CommandSourceStack> c) {
        String name=StringArgumentType.getString(c,"track");
        var track=find(name);
        if(track==null){c.getSource().sendFailure(Component.translatable(KEY+"unknown",name));return 0;}
        var director=accepting(c.getSource()::sendFailure);
        if(director==null)return 0;
        director.play(track);
        ok(c).accept(Component.translatable(KEY+"playing",title(track)));
        return 1;
    }

    private static int here(Consumer<Component> ok,Consumer<Component> fail) {
        var director=accepting(fail);
        if(director==null)return 0;
        var track=director.playHere();
        if(track==null){fail.accept(Component.translatable(KEY+"silent"));return 0;}
        ok.accept(Component.translatable(KEY+"playing",title(track)));
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> c) {
        var director=accepting(c.getSource()::sendFailure);
        if(director==null)return 0;
        if(!director.stopSong()){c.getSource().sendFailure(Component.translatable(KEY+"nothing"));return 0;}
        ok(c).accept(Component.translatable(KEY+"stopped"));
        return 1;
    }

    /** What is playing and how far in, or what the soundtrack is waiting for. */
    private static Component now() {
        var director=SoundtrackClient.director();
        if(SoundtrackClient.firstPartyMusicActive())return Component.translatable(KEY+"now.first_party");
        if(director==null || director.status().phase()==Director.Phase.DISABLED)return Component.translatable(KEY+"now.disabled");
        var s=director.status();
        if(s.phase()==Director.Phase.YIELDED || director.refusal()==Director.Refusal.BUSY)return SoundtrackClient.yieldStatus();
        if(director.refusal()==Director.Refusal.OFF)return Component.translatable(KEY+"off");
        return switch(s.phase()) {
            case PLAYING -> Component.translatable(KEY+(s.requested()?"now.requested":"now.playing"),title(s.track()),
                    SoundtrackClient.clock(s.playedTicks()),s.track().seconds()>0?SoundtrackClient.clock(s.track().seconds()*20):"?");
            case FADING -> Component.translatable(KEY+"now.fading",title(s.track()));
            case BREATH -> Component.translatable(KEY+"now.breath");
            default -> SoundtrackClient.tracks().pool(s.context()).ids().isEmpty()?Component.translatable(KEY+"silent")
                    :Component.translatable(KEY+"now.idle",SoundtrackClient.clock(Math.max(0,s.delay())));
        };
    }

    private static int list(CommandContext<CommandSourceStack> c) {
        var tracks=SoundtrackClient.tracks().tracks().values();
        ok(c).accept(Component.translatable(KEY+"list",tracks.size()));
        for(var t:tracks)ok(c).accept(title(t));
        return tracks.size();
    }

    private SoundtrackCommands(){}
}
