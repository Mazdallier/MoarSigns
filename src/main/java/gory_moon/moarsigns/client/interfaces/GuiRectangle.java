package gory_moon.moarsigns.client.interfaces;

import java.util.Arrays;

public class GuiRectangle {

    protected int x;
    protected int y;
    protected int w;
    protected int h;

    public GuiRectangle(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean inRect(int mouseX, int mouseY) {
        return x <= mouseX && mouseX < x + w && y <= mouseY && mouseY < y + h;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void draw(GuiBase gui, int srcX, int srcY) {
        gui.drawTexturedModalRect(x, y, srcX, srcY, w, h);
    }

    public void drawString(GuiBase gui, int mouseX, int mouseY, String str) {
        if (inRect(mouseX, mouseY)) {
            String[] list = str.split("\n");
            for (String s: list) s = s.trim();
            gui.drawHoveringText(Arrays.asList(list), mouseX, mouseY, gui.getFontRenderer());
        }
    }

}
