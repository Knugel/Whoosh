package knugel.whoosh.gui;

import cofh.api.core.ISecurable;
import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.GuiTextList;
import cofh.core.gui.element.ElementButton;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabSecurity;
import cofh.core.util.helpers.SecurityHelper;
import knugel.whoosh.item.ItemTransporter;
import knugel.whoosh.util.TeleportPosition;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GuiTransporter extends GuiContainerCore {

    static final String TEXTURE_PATH = "whoosh:textures/gui/transporter.png";
    static final ResourceLocation TEXTURE = new ResourceLocation(TEXTURE_PATH);

    static final int TB_HEIGHT = 12;

    boolean secure;
    EntityPlayer player;
    int selected = -1;

    GuiTextField tbName;
    GuiTextList taPoints;
    GuiTextList taInfo;
    ElementButton addPoint;
    ElementButton removePoint;
    ElementButton pUp;
    ElementButton pDown;
    ElementButton pSelect;
    ElementButton pDeselect;
    ElementEnergyItem energy;
    ElementFluidItem tank;

    int tbNameX = 0;
    int tbNameY = 0;

    int taPX = 0;
    int taPY = 0;

    int taIX = 0;
    int taIY = 0;

    public GuiTransporter(EntityPlayer player, ContainerTransporter container) {

        super(container, TEXTURE);

        secure = SecurityHelper.isSecure(container.getContainerStack());
        this.player = player;

        name = "gui.whoosh.transporter";
        drawInventory = false;
        ySize = 109;
        xSize = 192;

        generateInfo("tab.whoosh.transporter");
    }

    @Override
    public void initGui() {

        super.initGui();
        addTab(new TabInfo(this, myInfo));

        if(secure) {
            addTab(new TabSecurity(this, (ISecurable)inventorySlots, SecurityHelper.getID(player)));
        }

        tbNameX = guiLeft + 10;
        tbNameY = guiTop + 24;

        taPX = guiLeft + 8;
        taPY = guiTop + 39;

        taIX = guiLeft + 96;
        taIY = guiTop + 44;

        String temp = "";
        if (tbName != null) { // Stops GUI resize deleting text.
            temp = tbName.getText();
        }

        tbName = new GuiTextField(0, this.fontRenderer, tbNameX, tbNameY, 83, TB_HEIGHT);
        tbName.setMaxStringLength(12);
        tbName.setText(temp);
        tbName.setEnableBackgroundDrawing(false);

        taPoints = new GuiTextList(this.fontRenderer, taPX, taPY, 83, 6);
        taPoints.textLines = getPoints();
        taPoints.drawBackground = false;
        taPoints.drawBorder = false;
        taPoints.highlightSelectedLine = true;

        taInfo = new GuiTextList(this.fontRenderer, taIX, taIY, 65, 5);
        taInfo.drawBackground = false;
        taInfo.drawBorder = false;

        addPoint = new ElementButton(this, 95, 20, "SetPoint", 208, 128, 208, 144, 208, 160, 16, 16, TEXTURE_PATH);
        removePoint = new ElementButton(this, 111, 20, "RemovePoint", 224, 128, 224, 144, 224, 160, 16, 16, TEXTURE_PATH);

        pUp = new ElementButton(this, 130, 20, "PointsUp", 208, 64, 208, 80, 208, 96, 16, 16, TEXTURE_PATH);
        pDown = new ElementButton(this, 146, 20, "PointsDown", 224, 64, 224, 80, 224, 96, 16, 16, TEXTURE_PATH);

        pSelect = new ElementButton(this, 165, 18, "SelectPoint", 208, 192, 208, 212, 208, 232, 20, 20, TEXTURE_PATH);
        pSelect.setDisabled();

        pDeselect = new ElementButton(this, 165, 18, "DeselectPoint", 228, 192, 228, 212, 228, 232, 20, 20, TEXTURE_PATH);
        pDeselect.setDisabled();
        pDeselect.setVisible(false);

        energy = new ElementEnergyItem(this, 166, 42, ((ContainerTransporter)inventorySlots).getContainerStack());
        tank = new ElementFluidItem(this, 175, 43, ((ContainerTransporter)inventorySlots).getContainerStack());

        addElement(addPoint);
        addElement(removePoint);
        addElement(pUp);
        addElement(pDown);
        addElement(pSelect);
        addElement(pDeselect);
        addElement(energy);
        addElement(tank);

        updateButtons();
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {

        super.drawGuiContainerBackgroundLayer(f, x, y);
        mc.renderEngine.bindTexture(TEXTURE);

        tbName.drawTextBox();
        taPoints.drawText();
        taInfo.drawText();
    }

    @Override
    public void onGuiClosed() {

        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {

        tbName.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char i, int j) {

        this.tbName.textboxKeyTyped(i, j);
        if (j == 1) { // esc
            this.mc.player.closeScreen();
            return;
        }
        updateButtons();
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {

        switch (buttonName) {
            case "SetPoint":
                ((ContainerTransporter)inventorySlots)
                        .appendPoint(new TeleportPosition(player.getPosition(), player.world.provider.getDimension(), tbName.getText()));
                tbName.setText("");

                break;
            case "RemovePoint":
                ContainerTransporter container = (ContainerTransporter)inventorySlots;

                int index = getPoints().indexOf(taPoints.textLines.get(taPoints.selectedLine));
                if (index == selected) {
                    taInfo.textLines = Collections.emptyList();
                }

                if (container.getSelected() == index) {
                    container.setSelected(-1);
                }

                if (index != -1) {
                    container.removePoint(index);
                }
                break;
            case "SelectPoint":
                if(selected != -1) {
                    ((ContainerTransporter)inventorySlots).setSelected(selected);
                }
                break;
            case "DeselectPoint":
                ((ContainerTransporter)inventorySlots).setSelected(-1);
                break;
            case "PointsUp":
                taPoints.scrollDown();
                break;
            case "PointsDown":
                taPoints.scrollUp();
                break;
        }

        playClickSound(0.7F);
        updateButtons();
    }

    @Override
    protected void mouseClicked(int mX, int mY, int mButton) throws IOException {

        int textAreaX = taPoints.xPos - guiLeft;
        int textAreaY = taPoints.yPos - guiTop;

        if (textAreaX <= mouseX && mouseX < textAreaX + taPoints.width && mouseY >= textAreaY && mouseY < textAreaY + taPoints.height) {
            if (!taPoints.mouseClicked(mouseX, mouseY, mButton, textAreaY).equalsIgnoreCase(tbName.getText())) {
                String sel = taPoints.mouseClicked(mouseX, mouseY, mButton, textAreaY);
                int index = taPoints.textLines.indexOf(sel);

                if(index != -1) {
                    taPoints.selectLine(index);
                    List<TeleportPosition> positions = ItemTransporter.getPositions(((ContainerTransporter)inventorySlots).getContainerStack());
                    setInfo(positions.get(index));
                    selected = index;
                    tbName.setFocused(false);
                }
            }
        } else if (tbNameX - guiLeft <= mouseX && mouseX < tbNameX - guiLeft + tbName.getWidth() && mouseY >= tbNameY - guiTop && mouseY < tbNameY - guiTop + 12) {
            taPoints.selectedLine = -1;
            selected = -1;
            tbName.setFocused(true);
        } else {
            super.mouseClicked(mX, mY, mButton);
        }

        updateButtons();
    }

    private void setInfo(TeleportPosition pos) {
        List<String> info = new ArrayList<>();
        info.add("X: " + pos.position.getX());
        info.add("Y: " + pos.position.getY());
        info.add("Z: " + pos.position.getZ());
        String name = pos.getDimensionName();
        if(name.length() > 10) {
            String partOne = name.substring(0, 10);
            String partTwo = name.substring(10);
            info.add(partOne + "-");
            info.add(partTwo);
        }
        else {
            info.add(name);
        }
        taInfo.textLines = info;
    }

    public void updateButtons() {

        if (canScrollUpPoints()) {
            pUp.setActive();
        } else {
            pUp.setDisabled();
        }
        if (canScrollDownPoints()) {
            pDown.setActive();
        } else {
            pDown.setDisabled();
        }

        if(selected == -1) {
            taInfo.textLines = Collections.emptyList();
        }

        if (taPoints.selectedLine != -1) {
            addPoint.setDisabled();

            if(!taPoints.textLines.isEmpty()) {
                removePoint.setActive();
                int sel = ((ContainerTransporter)inventorySlots).getSelected();
                if(selected != sel) {
                    pSelect.setVisible(true);
                    pSelect.setActive();
                    pDeselect.setVisible(false);
                    pDeselect.setDisabled();
                }
                else {
                    pSelect.setVisible(false);
                    pSelect.setDisabled();
                    pDeselect.setVisible(true);
                    pDeselect.setActive();
                }
            }
            else {
                removePoint.setDisabled();
                pSelect.setDisabled();
                pSelect.setVisible(true);
                pDeselect.setDisabled();
                pDeselect.setVisible(false);
            }
        } else {
            if(taPoints.textLines.contains(tbName.getText()) || tbName.getText().equals("")) {
                addPoint.setDisabled();
            }
            else
                addPoint.setActive();


            removePoint.setDisabled();
            pSelect.setDisabled();
        }

        taPoints.textLines = getPoints();
    }

    @Override
    public void handleMouseInput() throws IOException {

        super.handleMouseInput();

        int textAreaX = taPoints.xPos - guiLeft;
        int textAreaY = taPoints.yPos - guiTop;

        if (textAreaX <= mouseX && mouseX < textAreaX + taPoints.width && mouseY >= textAreaY && mouseY < textAreaY + taPoints.height) {
            int wheelDir = Mouse.getEventDWheel();

            if (wheelDir < 0) {
                taPoints.scrollUp();
            }

            if (wheelDir > 0) {
                taPoints.scrollDown();
            }
        }
    }

    private boolean canScrollUpPoints() {

        return taPoints.startLine != 0;
    }

    private boolean canScrollDownPoints() {

        return taPoints.textLines.size() > taPoints.displayLines && taPoints.startLine < taPoints.textLines.size() - taPoints.displayLines;
    }

    private List<String> getPoints() {

        return ((ContainerTransporter)inventorySlots).getPoints()
                .stream()
                .map(x -> x.name)
                .collect(Collectors.toList());
    }
}
