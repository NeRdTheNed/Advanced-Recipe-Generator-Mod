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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeFireworks;
import net.minecraft.item.crafting.RecipesArmorDyes;
import net.minecraft.item.crafting.RecipesMapCloning;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = ARG.MOD_ID, guiFactory = "arg.ARG")
public class ARG implements IModGuiFactory {

    public static final class Config extends GuiConfig {
        public Config(GuiScreen g) {
            super(g, new ConfigElement<ConfigCategory>(ARG.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), MOD_ID, false, false, /* GuiConfig.getAbridgedConfigPath(MoreBows.config.toString()) */ MOD_ID);
        }
    }

    public static final String MOD_ID = "arg";

    public static Logger argLog = Logger.getLogger(MOD_ID);

    public static Configuration config;

    public static boolean displaySingleOreDictEntries;

    public static boolean mapGenerated = false;
    public static int[] mapLoaded = { 0, 0 };

    public static final Item wildcardItem = new WildcardItem();

    private static final void syncConfig() {
        displaySingleOreDictEntries = config.get(Configuration.CATEGORY_GENERAL, "displaySingleOreDictEntries", false).getBoolean();
        config.save();
    }

    @SubscribeEvent
    public final void configChange(OnConfigChangedEvent event) {
        if (event.modID.equals(MOD_ID)) {
            syncConfig();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void createRecipeImages(TextureStitchEvent.Post evt) {
        mapLoaded[evt.map.getTextureType()]++;

        if (!mapGenerated && (mapLoaded[0] > 0) && (mapLoaded[0] == mapLoaded[1])) {
            mapGenerated = true;
            argLog.info("Generating Recipes ...");
            final TextureManager tm = Minecraft.getMinecraft().getTextureManager();
            // save since we get a ConcurrentModificationException in TextureManager.func_110549_a otherwise
            final Map mapTextureObjects = ObfuscationReflectionHelper.getPrivateValue(TextureManager.class, tm, "mapTextureObjects", "field_110585_a");
            final Map new_mapTextureObjects = Maps.newHashMap();
            new_mapTextureObjects.putAll(mapTextureObjects);
            ObfuscationReflectionHelper.setPrivateValue(TextureManager.class, tm, new_mapTextureObjects, "mapTextureObjects", "field_110585_a");
            final HashMap<UniqueIdentifier, ArrayList<IRecipe>> recipeMap = new HashMap();

            for (final Object orecipe : CraftingManager.getInstance().getRecipeList()) {
                final IRecipe irecipe = (IRecipe) orecipe;

                if ((irecipe instanceof RecipesArmorDyes) || (irecipe instanceof RecipeFireworks) || (irecipe instanceof RecipesMapCloning)) {
                    continue;
                }

                if (irecipe.getRecipeOutput() == null) {
                    System.out.println("Skip recipe without output: " + irecipe.getClass().getSimpleName());
                    continue;
                }

                final UniqueIdentifier itemIdentifier = getUniqueIdentifier(irecipe.getRecipeOutput());
                final ArrayList<IRecipe> existingEntry = recipeMap.get(itemIdentifier);

                if (existingEntry == null) {
                    final ArrayList<IRecipe> singleList = new ArrayList<IRecipe>();
                    singleList.add(irecipe);
                    recipeMap.put(itemIdentifier, singleList);
                } else {
                    existingEntry.add(irecipe);
                }
            }

            RecipeHelper.setupInfo();

            for (final Map.Entry<UniqueIdentifier, ArrayList<IRecipe>> recipeEntry : recipeMap.entrySet()) {
                for (final IRecipe irecipe : recipeEntry.getValue()) {
                    ItemStack[] recipeInput = null;

                    try {
                        recipeInput = RecipeHelper.getRecipeArray(irecipe);

                        if (recipeInput == null) {
                            continue;
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }

                    String subFolder;

                    if (recipeEntry.getKey() != null) {
                        subFolder = recipeEntry.getKey().modId;
                    } else {
                        subFolder = "UnkownMod";
                    }

                    final RenderRecipe render = new RenderRecipe(irecipe.getRecipeOutput().getDisplayName());

                    try {
                        for (int i = 0; i < (recipeInput.length - 1); ++i) {
                            render.getCraftingContainer().craftMatrix.setInventorySlotContents(i, recipeInput[i + 1]);
                        }

                        render.getCraftingContainer().craftResult.setInventorySlotContents(0, recipeInput[0]);
                        render.draw(subFolder + "/" + recipeEntry.getKey().name.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", ""));
                    } catch (final Exception e) {
                        argLog.severe("Exception thrown when trying to draw recipe for " + irecipe.getRecipeOutput().getDisplayName() + " of type" + irecipe.getClass().getName() + ":");
                        e.printStackTrace();
                    }
                }
            }

            // restore map since we get a ConcurrentModificationException in TextureManager.func_110549_a otherwise
            ObfuscationReflectionHelper.setPrivateValue(TextureManager.class, tm, mapTextureObjects, "mapTextureObjects", "field_110585_a");
            argLog.info("Finished Generation of Recipes in " + Minecraft.getMinecraft().mcDataDir + "/recipes/");
        }
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement a) {
        return null;
    }

    private UniqueIdentifier getUniqueIdentifier(ItemStack itemStack) {
        if ((itemStack == null) || (itemStack.getItem() == null)) {
            return null;
        }

        if (itemStack.getItem() instanceof ItemBlock) {
            final Block block = Block.getBlockFromItem((itemStack.getItem()));
            return GameRegistry.findUniqueIdentifierFor(block);
        } else {
            return GameRegistry.findUniqueIdentifierFor(itemStack.getItem());
        }
    }

    @Override
    public void initialize(Minecraft a) {
        /* This space left intentionally blank */
    }

    @EventHandler
    public void load(FMLInitializationEvent evt) {
        argLog.info("Starting Advanced Recipe Generator\nCopyright (c) Flow86, 2012-2014");
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        final File file = new File(Minecraft.getMinecraft().mcDataDir, "recipes/");

        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (final IOException e) {
                argLog.severe("Could not delete previously generated recipes folder!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return Config.class;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();
        GameRegistry.registerItem(wildcardItem, "WildcardItem");
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
