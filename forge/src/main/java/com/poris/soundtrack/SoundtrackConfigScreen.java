package com.poris.soundtrack;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Forge 1.20.1 has no built-in config editor. All seven settings remain editable without another mod. */
final class SoundtrackConfigScreen extends Screen {
    private static final String KEY=PorisSoundtrack.MOD_ID+".configuration.";
    private final Screen parent;
    private boolean menu=SoundtrackConfig.menuMusic(),inGame=SoundtrackConfig.inGameMusic();
    private boolean toast=SoundtrackConfig.nowPlaying(),debug=SoundtrackConfig.debugOverlay();
    private Frequency frequency=SoundtrackConfig.frequency();
    private ConflictMode mode=SoundtrackConfig.CONFLICT_MODE.get();
    private String whitelist=String.join(", ",SoundtrackConfig.MUSIC_WHITELIST.get());
    private EditBox list;
    private Button done;
    private int left,rowWidth,listY;

    SoundtrackConfigScreen(Screen parent) {
        super(Component.translatable(KEY+"title"));
        this.parent=parent;
    }

    private static Component label(String key,Component value){return Component.translatable(KEY+key).append(": ").append(value);}
    private static Component toggle(String key,boolean value){return label(key,value?CommonComponents.OPTION_ON:CommonComponents.OPTION_OFF);}
    private void button(int y,String key,Component text,Button.OnPress action) {
        addRenderableWidget(Button.builder(text,action).bounds(left,y,rowWidth,20)
                .tooltip(Tooltip.create(Component.translatable(KEY+key+".tooltip"))).build());
    }

    @Override protected void init() {
        rowWidth=Math.min(360,width-24);left=(width-rowWidth)/2;
        int top=Math.max(28,(height-206)/2);
        button(top,"menuMusic",toggle("menuMusic",menu),b->{menu=!menu;b.setMessage(toggle("menuMusic",menu));});
        button(top+22,"inGameMusic",toggle("inGameMusic",inGame),b->{inGame=!inGame;b.setMessage(toggle("inGameMusic",inGame));});
        button(top+44,"frequency",label("frequency",frequency.getTranslatedName()),b->{frequency=Frequency.values()[(frequency.ordinal()+1)%Frequency.values().length];b.setMessage(label("frequency",frequency.getTranslatedName()));});
        button(top+66,"conflictMode",label("conflictMode",mode.getTranslatedName()),b->{mode=ConflictMode.values()[(mode.ordinal()+1)%ConflictMode.values().length];b.setMessage(label("conflictMode",mode.getTranslatedName()));});
        button(top+88,"nowPlaying",toggle("nowPlaying",toast),b->{toast=!toast;b.setMessage(toggle("nowPlaying",toast));});
        button(top+110,"debugOverlay",toggle("debugOverlay",debug),b->{debug=!debug;b.setMessage(toggle("debugOverlay",debug));});
        listY=top+146;
        list=new EditBox(font,left,listY,rowWidth,20,Component.translatable(KEY+"musicWhitelist"));
        list.setMaxLength(4096);list.setValue(whitelist);
        list.setTooltip(Tooltip.create(Component.translatable(KEY+"musicWhitelist.tooltip")
                .append("\n\n").append(Component.translatable(KEY+"musicWhitelist.commaHint"))));
        list.setResponder(s->{whitelist=s;validate();});addRenderableWidget(list);
        done=addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,b->save()).bounds(left+rowWidth/2+4,top+174,rowWidth/2-4,20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL,b->onClose()).bounds(left,top+174,rowWidth/2-4,20).build());
        validate();
    }

    private List<String> entries(){return Arrays.stream(whitelist.split(",",-1)).map(String::trim).filter(s->!s.isEmpty()).toList();}
    private void validate() {
        boolean valid=entries().stream().allMatch(MusicPolicy::validEntry);
        if(done!=null)done.active=valid;
        if(list!=null)list.setTextColor(valid?0xFFFFFF:0xFF7777);
    }
    private void save() {
        if(!entries().stream().allMatch(MusicPolicy::validEntry))return;
        SoundtrackConfig.MENU_MUSIC.set(menu);SoundtrackConfig.IN_GAME_MUSIC.set(inGame);
        SoundtrackConfig.FREQUENCY.set(frequency);SoundtrackConfig.CONFLICT_MODE.set(mode);
        SoundtrackConfig.NOW_PLAYING.set(toast);SoundtrackConfig.DEBUG_OVERLAY.set(debug);
        SoundtrackConfig.MUSIC_WHITELIST.set(entries());SoundtrackConfig.SPEC.save();onClose();
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font,title,width/2,12,0xFFFFFF);
        graphics.drawString(font,Component.translatable(KEY+"musicWhitelist"),left,listY-11,0xFFFFFF);
        super.render(graphics,mouseX,mouseY,partialTick);
    }
}
