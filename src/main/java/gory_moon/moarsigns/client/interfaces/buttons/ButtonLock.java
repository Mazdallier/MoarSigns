package gory_moon.moarsigns.client.interfaces.buttons;

import gory_moon.moarsigns.client.interfaces.GuiBase;
import gory_moon.moarsigns.client.interfaces.GuiMoarSign;
import gory_moon.moarsigns.util.Localization;

import java.util.Iterator;

public class ButtonLock extends GuiButtonToggleable  {

    public ButtonLock(int x, int y, int srcX) {
        super(x, y, 16, 16, srcX);
    }

    @Override
    public String getButtonInfo(GuiBase gui) {
        return Localization.GUI.BUTTONS.LOCK.translateTitles() + newLine + Localization.GUI.BUTTONS.LOCK.translateDescriptions(newLine);
    }

    @Override
    public void action(GuiBase gui) {
        setState(!getState());

        GuiMoarSign guiMoarSign = (GuiMoarSign) gui;

        if (getState()) {
            for (GuiButton textPosSizeButton : guiMoarSign.textPosSizeButtons) {
                textPosSizeButton.isDisabled = true;
            }
        } else {
            for (GuiButton textPosSizeButton : guiMoarSign.textPosSizeButtons) {
                textPosSizeButton.isDisabled = false;
            }
        }
    }

    @Override
    public void drawButton(GuiBase gui, int mouseX, int mouseY) {
        super.draw(gui, (getState() ? srcX + 16: srcX), (inRect(mouseX, mouseY) ? srcX + 16: srcX));
    }

    public void unlock(GuiBase gui) {
        GuiMoarSign guiMoarSign = (GuiMoarSign) gui;
        setState(false);

        for (GuiButton textPosSizeButton : guiMoarSign.textPosSizeButtons) {
            textPosSizeButton.isDisabled = false;
        }
    }
}
