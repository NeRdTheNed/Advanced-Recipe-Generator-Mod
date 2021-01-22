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
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeHelper {
    public static ItemStack[][] getRecipeArrays(IRecipe irecipe) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
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

    public static ItemStack[][] getShapedOreRecipeResult(ShapedOreRecipe shapedOreRecipe) {
        final Object[] recipeInput = shapedOreRecipe.getInput();
        int recipeOutputs = 1;

        for (final Object obj : recipeInput) {
            if (obj instanceof ArrayList) {
                recipeOutputs *= ((ArrayList<?>) obj).size();
            }
        }

        final ItemStack[][] recipeArrays = new ItemStack[recipeOutputs][10];

        for (final ItemStack[] recipeArray : recipeArrays) {
            recipeArray[0] = shapedOreRecipe.getRecipeOutput();
        }

        for (int slot = 0; slot < recipeInput.length; slot++) {
            final Object recipeSlot = recipeInput[slot];

            if (recipeSlot == null) {
                continue;
            }

            if (recipeSlot instanceof ArrayList) {
                @SuppressWarnings("unchecked")
                final ArrayList<ItemStack> list = (ArrayList<ItemStack>) recipeSlot;

                for (int i = 0; i < recipeArrays.length; /* This space left intentionally blank */) {
                    for (ItemStack item : list) {
                        if ((item != null) && (item.getItemDamage() == WILDCARD_VALUE)) {
                            item = item.copy();
                            item.setItemDamage(0);
                        }

                        recipeArrays[i][slot + 1] = item;
                        i++;
                    }
                }
            } else if (recipeSlot instanceof ItemStack) {
                ItemStack item = (ItemStack) recipeSlot;

                if ((item != null) && (item.getItemDamage() == WILDCARD_VALUE)) {
                    item = item.copy();
                    item.setItemDamage(0);
                }

                for (final ItemStack[] recipeArray : recipeArrays) {
                    recipeArray[slot + 1] = item;
                }
            } else {
                argLog.warning("Slot " + (slot + 1) + " is type " + recipeSlot.getClass().getSimpleName());
                return null;
            }
        }

        return recipeArrays;
    }

    public static ItemStack[][] getShapedRecipeResult(ShapedRecipes shapedRecipe) {
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

        return new ItemStack[][] {recipeArray};
    }

    public static ItemStack[][] getShapelessOreRecipeResult(ShapelessOreRecipe shapelessOreRecipe) {
        final List<?> recipeInput = shapelessOreRecipe.getInput();
        final ItemStack[] recipeArray = new ItemStack[10];
        recipeArray[0] = shapelessOreRecipe.getRecipeOutput();

        for (int slot = 0; slot < recipeInput.size(); slot++) {
            Object recipeSlot = recipeInput.get(slot);

            if (recipeSlot == null) {
                continue;
            }

            if (recipeSlot instanceof ArrayList) {
                final ArrayList<?> list = (ArrayList<?>) recipeSlot;

                if (list.size() > 1) {
                    argLog.warning("Unhandled OreDictionary recipe: Slot-Array " + (slot + 1) + " has more then one item: " + list);
                }

                recipeSlot = list.get(0);
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

        return new ItemStack[][] {recipeArray};
    }

    public static ItemStack[][] getShapelessRecipeResult(ShapelessRecipes shapelessRecipe) {
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

        return new ItemStack[][] {recipeArray};
    }

}
