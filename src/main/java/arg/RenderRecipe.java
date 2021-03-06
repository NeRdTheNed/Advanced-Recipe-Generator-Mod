/**
 * Copyright (C) 2013 Flow86
 *
 * AdvancedRecipeGenerator is open-source.
 *
 * It is distributed under the terms of my Open Source License.
 * It grants rights to read, modify, compile or run the code.
 * It does *NOT* grant the right to redistribute this software or its
 * modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package arg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

class RenderRecipe extends GuiContainer {
    private final HashMap<String, ItemStack> incredientList = new HashMap<String, ItemStack>();
    public String name;

    @SuppressWarnings("rawtypes")
    public RenderRecipe(String name) {
        super(new ContainerCraft());
        this.name = name;
        mc = Minecraft.getMinecraft();
        fontRendererObj = mc.fontRenderer;
        final TileEntityRendererDispatcher tileEntityRendererDispatcher = TileEntityRendererDispatcher.instance;
        TextureManager renderEngine = tileEntityRendererDispatcher.field_147553_e;

        if (renderEngine == null) {
            renderEngine = mc.renderEngine;
            final Iterator iterator = tileEntityRendererDispatcher.mapSpecialRenderers.values().iterator();

            while (iterator.hasNext()) {
                final TileEntitySpecialRenderer tileentityspecialrenderer = (TileEntitySpecialRenderer) iterator.next();
                tileentityspecialrenderer.func_147497_a(tileEntityRendererDispatcher);
            }
        }

        xSize = 176;
        ySize = 155;
        width = xSize * 3;
        height = ySize * 3;
    }

    public void draw(String subFolder) {
        final File dir = new File(Minecraft.getMinecraft().mcDataDir, "recipes/" + subFolder);

        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("The recipes directory could not be created: " + dir);
        }

        name = name.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "");
        int fileCheckCounter = 1;
        File file = new File(Minecraft.getMinecraft().mcDataDir, "recipes/" + subFolder + "/" + name + fileCheckCounter + ".png");

        while (file.exists()) {
            file = new File(Minecraft.getMinecraft().mcDataDir, "recipes/" + subFolder + "/" + name + fileCheckCounter + ".png");
            fileCheckCounter++;
        }

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glViewport(0, 0, width, height);
        GL11.glOrtho(0.0D, xSize, ySize, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);

        try {
            drawScreen(0, 0, 0);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final ByteBuffer fb = ByteBuffer.allocateDirect(width * height * 3);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, fb);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
        GL11.glPopClientAttrib();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        try {
            Display.swapBuffers();
        } catch (final LWJGLException e1) {
            e1.printStackTrace();
        }

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final int i = (x + (width * y)) * 3;
                final int r = fb.get(i) & 0xFF;
                final int g = fb.get(i + 1) & 0xFF;
                final int b = fb.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(image, "png", file);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawBackground(int par1) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.renderEngine.bindTexture(new ResourceLocation("arg", "textures/gui/crafting.png"));
        drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);
        // GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int i, int j) {
        super.drawGuiContainerForegroundLayer(i, j);
        final float scale = 3 / 4F;
        // since we scale by 1/2, we have to start on *2
        final int[] baseX = { (int) (10 * (1F / scale)), (int) (100 * (1F / scale)) };
        final int baseY = (int) (76 * (1F / scale));
        GL11.glScalef(scale, scale, 1.0F);
        final String title = getCraftingContainer().craftResult.getStackInSlot(0).getDisplayName();
        fontRendererObj.drawString(title, (getCenteredOffset(title, (int) (xSize * (1F / scale)))), (int) (7 * (1F / scale)), 0x404040);
        int item = 0;
        int y = baseY;
        final ArrayList<String> itemNames = new ArrayList<String>(incredientList.keySet());
        Collections.sort(itemNames, String.CASE_INSENSITIVE_ORDER);

        for (final String itemName : itemNames) {
            final int x = baseX[incredientList.size() < 5 ? 0 : (item < (incredientList.size() / 2) ? 0 : 1)];

            if (incredientList.size() < 5 ? false : (item == (incredientList.size() / 2))) {
                y = baseY;
            }

            drawItemStackAtPosition(incredientList.get(itemName), x + 6, y - 4);
            @SuppressWarnings("unchecked")
            final
            List<String> itemNameLines = fontRendererObj.listFormattedStringToWidth(itemName, (int) ((176 - 10 - 18) * (1F / scale)));

            for (final Iterator<String> iterator = itemNameLines.iterator(); iterator.hasNext(); y += fontRendererObj.FONT_HEIGHT) {
                final String itemNameLine = iterator.next();
                fontRendererObj.drawString(itemNameLine, x + 26, y, 0x404040);
            }

            y += 9;
            item++;
        }

        GL11.glScalef(1F / scale, 1F / scale, 1.0F);
    }

    /**
     * Draws an itemstack at a specific position
     */
    protected void drawItemStackAtPosition(ItemStack itemstack, int x, int y) {
        if (itemstack == null) {
            return;
        }

        zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        String s = null;

        if (itemstack.stackSize > 1) {
            s = "" + itemstack.stackSize;
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, itemstack, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, itemstack, x, y, s);
        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3) {
        drawDefaultBackground();
        final int k = guiLeft;
        final int l = guiTop;
        drawGuiContainerBackgroundLayer(par3, par1, par2);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef(k, l, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        final short short1 = 240;
        final short short2 = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // crafting result
        drawSlotInventory((Slot) inventorySlots.inventorySlots.get(0));
        incredientList.clear();

        for (int j1 = 1; j1 < inventorySlots.inventorySlots.size(); ++j1) {
            final Slot slot = (Slot) inventorySlots.inventorySlots.get(j1);
            drawSlotInventory(slot);
        }

        drawGuiContainerForegroundLayer(par1, par2);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
    }

    /**
     * Draws an inventory slot
     */
    protected void drawSlotInventory(Slot slot) {
        final ItemStack itemstack = slot.getStack();

        if (itemstack == null) {
            return;
        }

        itemstack.stackSize = 1;

        if (!incredientList.containsKey(itemstack.getDisplayName())) {
            incredientList.put(itemstack.getDisplayName(), itemstack);
        }

        drawItemStackAtPosition(itemstack, slot.xDisplayPosition, slot.yDisplayPosition);
    }

    protected int getCenteredOffset(String string, int xWidth) {
        return (xWidth - fontRendererObj.getStringWidth(string)) / 2;
    }

    public ContainerCraft getCraftingContainer() {
        return (ContainerCraft) inventorySlots;
    }
}
