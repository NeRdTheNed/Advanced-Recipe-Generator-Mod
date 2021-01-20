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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeHelper {
    @SuppressWarnings("rawtypes")
    public static ItemStack[] getRecipeArray(IRecipe irecipe) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
        if (irecipe.getRecipeSize() > 9) {
            return null;
        }

        final ItemStack[] recipeArray = new ItemStack[10];
        recipeArray[0] = irecipe.getRecipeOutput();

        if ((irecipe instanceof ShapedRecipes)) {
            final ShapedRecipes shapedRecipe = (ShapedRecipes) irecipe;
            final ItemStack[] recipeInput = shapedRecipe.recipeItems;

            for (int slot = 0; slot < recipeInput.length; slot++) {
                ItemStack item = recipeInput[slot];

                if ((item != null) && ((item.getItemDamage() == -1) || (item.getItemDamage() == 32767))) {
                    item = item.copy();
                    item.setItemDamage(0);
                }

                final int x = slot % shapedRecipe.recipeWidth;
                final int y = slot / shapedRecipe.recipeWidth;
                recipeArray[(x + (y * shapedRecipe.recipeWidth)) + 1] = item;
            }
        } else if ((irecipe instanceof ShapelessRecipes)) {
            final ShapelessRecipes shapelessRecipe = (ShapelessRecipes) irecipe;
            final List recipeInput = shapelessRecipe.recipeItems;

            for (int slot = 0; slot < recipeInput.size(); slot++) {
                ItemStack item = (ItemStack) recipeInput.get(slot);

                if ((item != null) && (item.getItemDamage() == -1)) {
                    item = item.copy();
                    item.setItemDamage(0);
                }

                recipeArray[slot + 1] = item;
            }
        } else if ((irecipe instanceof ShapedOreRecipe)) {
            final ShapedOreRecipe shapedOreRecipe = (ShapedOreRecipe) irecipe;
            final Object[] recipeInput = shapedOreRecipe.getInput();

            for (int slot = 0; slot < recipeInput.length; slot++) {
                Object recipeSlot = recipeInput[slot];

                if (recipeSlot == null) {
                    continue;
                }

                if (recipeSlot instanceof ArrayList) {
                    final ArrayList list = (ArrayList) recipeSlot;

                    if (list.size() > 1) {
                        argLog.warning("ERROR: Slot-Array " + (slot + 1) + " has more then one item: " + list);
                        return null;
                    }

                    recipeSlot = list.get(0);
                }

                if (recipeSlot instanceof ItemStack) {
                    ItemStack item = (ItemStack) recipeSlot;

                    if ((item != null) && (item.getItemDamage() == -1)) {
                        item = item.copy();
                        item.setItemDamage(0);
                    }

                    recipeArray[slot + 1] = item;
                } else {
                    argLog.warning("Slot " + (slot + 1) + " is type " + recipeSlot.getClass().getSimpleName());
                    return null;
                }
            }
        } else if ((irecipe instanceof ShapelessOreRecipe)) {
            final ShapelessOreRecipe shapelessOreRecipe = (ShapelessOreRecipe) irecipe;
            final List recipeInput = shapelessOreRecipe.getInput();

            for (int slot = 0; slot < recipeInput.size(); slot++) {
                Object recipeSlot = recipeInput.get(slot);

                if (recipeSlot == null) {
                    continue;
                }

                if (recipeSlot instanceof ArrayList) {
                    final ArrayList list = (ArrayList) recipeSlot;

                    if (list.size() > 1) {
                        argLog.warning("ERROR: Slot-Array " + (slot + 1) + " has more then one item: " + list);
                        return null;
                    }

                    recipeSlot = list.get(0);
                }

                if (recipeSlot instanceof ItemStack) {
                    ItemStack item = (ItemStack) recipeSlot;

                    if ((item != null) && (item.getItemDamage() == -1)) {
                        item = item.copy();
                        item.setItemDamage(0);
                    }

                    recipeArray[slot + 1] = item;
                } else {
                    argLog.warning("Slot " + (slot + 1) + " is type " + recipeSlot.getClass().getSimpleName());
                    return null;
                }
            }
        } else {
            argLog.warning("Unknown Type: " + irecipe.getClass().getSimpleName());
            return null;
        }

        return recipeArray;
    }
}
