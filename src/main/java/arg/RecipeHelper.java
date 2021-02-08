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

import static arg.ARG.argLog;
import static net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeHelper {

    //static String[] oreDictNames;
    static HashMap<ArrayList<ItemStack>, String> oreDictMappings = new HashMap<ArrayList<ItemStack>, String>();

    public static ItemStack[] getRecipeArray(IRecipe irecipe) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
        if (irecipe.getRecipeSize() > 9) {
            argLog.warning("IRecipe " + irecipe.getClass() + " had a size higher than 9 (" + irecipe.getRecipeSize() + "), which ARG does not support..");
            return null;
        }

        if ((irecipe instanceof ShapedRecipes)) {
            return getShapedRecipeResult((ShapedRecipes) irecipe);
        } else if ((irecipe instanceof ShapelessRecipes)) {
            return getShapelessRecipeResult((ShapelessRecipes) irecipe);
        } else if ((irecipe instanceof ShapedOreRecipe)) {
            return getShapedOreRecipeResult((ShapedOreRecipe) irecipe);
        } else if ((irecipe instanceof ShapelessOreRecipe)) {
            return getShapelessOreRecipeResult((ShapelessOreRecipe) irecipe);
        }

        argLog.warning("Unknown Type: " + irecipe.getClass().getSimpleName());
        return null;
    }

    public static ItemStack[] getShapedOreRecipeResult(ShapedOreRecipe shapedOreRecipe) {
        final Object[] recipeInput = shapedOreRecipe.getInput();
        final ItemStack[] recipeArray = new ItemStack[10];
        recipeArray[0] = shapedOreRecipe.getRecipeOutput();
        // We have to use reflection to get the dimensions of a ShapedOreRecipe. Is this really necessary???
        final int reflectionWidth = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, shapedOreRecipe, "width");
        final int reflectionHeight = ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, shapedOreRecipe, "height");
        // TODO Make this better
        int slot = -1;

        for (int height = 0; height < reflectionHeight; height++) {
            for (int width = 0; width < reflectionWidth; width++) {
                slot++;
                Object recipeSlot = recipeInput[slot];

                if (recipeSlot == null) {
                    continue;
                }

                if (recipeSlot instanceof ArrayList) {
                    @SuppressWarnings("unchecked")
                    final ArrayList<ItemStack> list = (ArrayList<ItemStack>) recipeSlot;

                    if ((!ARG.displaySingleOreDictEntries) && (list.size() == 1)) {
                        recipeSlot = list.get(0).copy();
                    } else {
                        recipeSlot = itemstackListToWildcardItemstack(list);
                    }
                }

                if (recipeSlot instanceof ItemStack) {
                    ItemStack item = (ItemStack) recipeSlot;

                    if ((item != null) && (item.getItemDamage() == WILDCARD_VALUE)) {
                        item = item.copy();
                        item.setItemDamage(0);
                    }

                    recipeArray[(height * 3) + width + 1] = item;
                } else {
                    argLog.severe("Slot " + (slot + 1) + " is type " + recipeSlot.getClass().getSimpleName());
                    return null;
                }
            }
        }

        return recipeArray;
    }

    public static ItemStack[] getShapedRecipeResult(ShapedRecipes shapedRecipe) {
        final ItemStack[] recipeInput = shapedRecipe.recipeItems;
        final ItemStack[] recipeArray = new ItemStack[10];
        recipeArray[0] = shapedRecipe.getRecipeOutput();

        for (int slot = 0; slot < recipeInput.length; slot++) {
            ItemStack item = recipeInput[slot];

            if ((item != null) && (item.getItemDamage() == WILDCARD_VALUE)) {
                item = item.copy();
                item.setItemDamage(0);
            }

            final int x = slot % shapedRecipe.recipeWidth;
            final int y = slot / shapedRecipe.recipeWidth;
            recipeArray[(x + (y * shapedRecipe.recipeWidth)) + 1] = item;
        }

        return recipeArray;
    }

    public static ItemStack[] getShapelessOreRecipeResult(ShapelessOreRecipe shapelessOreRecipe) {
        final List<?> recipeInput = shapelessOreRecipe.getInput();
        final ItemStack[] recipeArray = new ItemStack[10];
        recipeArray[0] = shapelessOreRecipe.getRecipeOutput();

        for (int slot = 0; slot < recipeInput.size(); slot++) {
            Object recipeSlot = recipeInput.get(slot);

            if (recipeSlot == null) {
                continue;
            }

            if (recipeSlot instanceof ArrayList) {
                @SuppressWarnings("unchecked")
                final ArrayList<ItemStack> list = (ArrayList<ItemStack>) recipeSlot;

                if ((!ARG.displaySingleOreDictEntries) && (list.size() == 1)) {
                    recipeSlot = list.get(0).copy();
                } else {
                    recipeSlot = itemstackListToWildcardItemstack(list);
                }
            }

            if (recipeSlot instanceof ItemStack) {
                ItemStack item = (ItemStack) recipeSlot;

                if ((item != null) && (item.getItemDamage() == WILDCARD_VALUE)) {
                    item = item.copy();
                    item.setItemDamage(0);
                }

                recipeArray[slot + 1] = item;
            } else {
                argLog.warning("Slot " + (slot + 1) + " is type " + recipeSlot.getClass().getSimpleName());
                return null;
            }
        }

        return recipeArray;
    }

    public static ItemStack[] getShapelessRecipeResult(ShapelessRecipes shapelessRecipe) {
        @SuppressWarnings("unchecked")
        final List<ItemStack> recipeInput = shapelessRecipe.recipeItems;
        final ItemStack[] recipeArray = new ItemStack[10];
        recipeArray[0] = shapelessRecipe.getRecipeOutput();

        for (int slot = 0; slot < recipeInput.size(); slot++) {
            ItemStack item = recipeInput.get(slot);

            if ((item != null) && (item.getItemDamage() == WILDCARD_VALUE)) {
                item = item.copy();
                item.setItemDamage(0);
            }

            recipeArray[slot + 1] = item;
        }

        return recipeArray;
    }

    public static ItemStack itemstackListToWildcardItemstack(ArrayList<ItemStack> list) {
        final String oreDictName = oreDictMappings.get(list);

        if (oreDictName != null) {
            final ItemStack oreDictItem;
            final String appendToName;

            if (list.size() == 1) {
                oreDictItem = list.get(0).copy();

                if (oreDictItem.getDisplayName().replaceAll("\\s", "").equalsIgnoreCase(oreDictName)) {
                    appendToName = oreDictItem.getDisplayName();
                } else {
                    appendToName = oreDictItem.getDisplayName() + " (" + oreDictName + ")";
                }
            } else {
                oreDictItem = new ItemStack(ARG.wildcardItem);
                appendToName = oreDictName;
            }

            oreDictItem.setStackDisplayName("Any type of " + appendToName);
            return oreDictItem;
        } else {
            return list.get(0).copy();
        }
    }

    /* This should only be called when every other mod has finished initialising. */
    public static void setupInfo() {
        final String[] oreDictNames = OreDictionary.getOreNames();

        for (final String oreDictName : oreDictNames) {
            oreDictMappings.put(OreDictionary.getOres(oreDictName), oreDictName);
        }
    }

}
