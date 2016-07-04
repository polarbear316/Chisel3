package com.cricketcraft.chisel.inventory;

import com.cricketcraft.chisel.api.CarvingRegistry;
import com.cricketcraft.chisel.api.ChiselRecipe;
import com.cricketcraft.chisel.item.chisel.ChiselController;
import com.cricketcraft.chisel.item.chisel.ItemChisel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.FMLLog;

public class InventoryChiselSelection implements IInventory {
    public ItemStack chisel = null;

    public static final int normalSlots = 60;
    public static final int inputSlotIndex = 60;

    public int activeVariations = 0;
    ItemStack[] inventory;
    public ContainerChisel container;

    public InventoryChiselSelection(ItemStack c) {
        super();
        inventory = new ItemStack[normalSlots + 1];
        chisel = c;
    }

    /**
     * onInventorySlotRemoved
     * @param slot
     */
    public void onInventoryUpdate(int slot) {

    }

    @Override
    public int getSizeInventory() {
        return normalSlots + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    public void updateInventoryState(int slot) {
        onInventoryUpdate(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] != null) {
            ItemStack stack;
            if (inventory[slot].stackSize <= amount) {
                stack = inventory[slot];
                inventory[slot] = null;
                updateInventoryState(slot);
                return stack;
            } else {
                stack = inventory[slot].splitStack(amount);
                if (inventory[slot].stackSize == 0)
                    inventory[slot] = null;
                updateInventoryState(slot);
                return stack;
            }
        } else
            return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory[index] = stack;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    	/* Get any saved input items */
    	inventory[inputSlotIndex] = ChiselController.loadChiselInput(chisel);
    	updateItems();
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    	/* Save any input items */
    	ChiselController.saveChiselInput(chisel,inventory[inputSlotIndex]);
    }

    public void clearItems() {
        for (int c = 0; c < activeVariations; c++) {
            inventory[c] = null;
        }
        activeVariations = 0;
    }

    public void updateItems() {
    	ItemStack chiselInput = inventory[inputSlotIndex];
    	clearItems();

		if (chiselInput == null) {
			container.onChiselSlotChanged();
			return;
		}

		ChiselRecipe recipe = CarvingRegistry.getRecipeFromItemStack(chiselInput);

		//If no associated recipe or there are no variations nothing to do so return
		if(recipe == null || recipe.getChiselResults().length == 0) return;

		ItemStack[] variations = recipe.getChiselResults();
		for(int i=0; i<variations.length; i++){
			inventory[i] = variations[i];
		}
		activeVariations = variations.length;

		container.onChiselSlotChanged();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
    	//ItemTool, ItemChisel are not valid inputs, and any slot other than input slot do not receive items 
    	if (stack != null && (stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemChisel || slot != inputSlotIndex)) {
            return false;
        }

        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }
}
