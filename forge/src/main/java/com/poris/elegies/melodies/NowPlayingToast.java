package com.poris.elegies.melodies;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Top-right card with the title of the track that just started, shown for five seconds. */
final class NowPlayingToast implements Toast {
    private static final ResourceLocation BACKGROUND=new ResourceLocation("textures/gui/toasts.png");
    private static final ItemStack ICON=new ItemStack(Items.MUSIC_DISC_OTHERSIDE);
    private final Component label=Component.translatable(ShatteredMelodies.MOD_ID+".toast.now_playing");
    private final Component title;
    private final int width;

    NowPlayingToast(TrackList.Track track) {
        title=TrackTitles.name(track);
        var font=Minecraft.getInstance().font;
        width=Math.max(160,40+Math.max(font.width(label),font.width(title)));
    }

    @Override public int width(){return width;}

    @Override public Visibility render(GuiGraphics graphics,ToastComponent toasts,long shownFor) {
        var font=toasts.getMinecraft().font;
        // Preserve the vanilla 1.20.1 system-toast border while extending the title area.
        graphics.blit(BACKGROUND,0,0,0,64,20,height());
        for(int x=20;x<width-20;x+=4)graphics.blit(BACKGROUND,x,0,20,64,Math.min(4,width-20-x),height());
        graphics.blit(BACKGROUND,width-20,0,140,64,20,height());
        graphics.renderFakeItem(ICON,8,8);
        graphics.drawString(font,label,30,7,0xFFC8B4FF,false);
        graphics.drawString(font,title,30,18,0xFFFFFFFF,false);
        return shownFor>=5000*toasts.getNotificationDisplayTimeMultiplier()?Visibility.HIDE:Visibility.SHOW;
    }
}
