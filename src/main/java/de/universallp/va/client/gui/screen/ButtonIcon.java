package de.universallp.va.client.gui.screen;

import de.universallp.va.core.util.libs.LibResources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Created by universallp on 31.03.2016 16:25.
 */
public class ButtonIcon extends GuiButton {

    private IconType icon;

    public ButtonIcon(int buttonId, int x, int y, IconType type) {
        super(buttonId, x, y, 18, 20, "");
        this.icon = type;
    }

    public ButtonIcon(int buttonId, int x, int y, IconType type, String text) {
        super(buttonId, x, y, 18, 20, text);
        this.icon = type;
        width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) + icon.widht + 4;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        if (this.visible) {
            icon.draw(this);
        }
    }

    public IconType getIcon() {
        return icon;
    }

    public void setIcon(IconType icon) {
        this.icon = icon;
    }

    public enum IconType {
        BLACKLIST(16, 0),
        WHITELIST(0, 0),
        CHECKED(32, 0),
        UNCHECKED(48, 0);

        private int x, y, widht, height;

        IconType(int x, int y) {
            this.x = x;
            this.y = y;
            this.widht = 16;
            this.height = 16;
        }

        public void draw(GuiButton parent) {
            GlStateManager.color(1, 1, 1);
            int xPos = parent.xPosition + 2;
            if (parent.displayString.equals(""))
                xPos = parent.xPosition + parent.width / 2 - this.widht / 2;
            int yPos = parent.yPosition + parent.height / 2 - this.widht / 2;
            Minecraft.getMinecraft().getTextureManager().bindTexture(LibResources.GUI_ICONS);
            parent.drawTexturedModalRect(xPos, yPos, x, y, widht, height);
        }

        public int getWidht() {
            return widht;
        }

        public int getHeight() {
            return height;
        }

        public IconType toggle() {
            if (ordinal() % 2 == 0)
                return IconType.values()[ordinal() + 1];
            else
                return IconType.values()[ordinal() - 1];
        }
    }
}
