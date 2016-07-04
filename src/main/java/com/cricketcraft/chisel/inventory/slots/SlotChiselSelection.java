package com.cricketcraft.chisel.inventory.slots;

import com.cricketcraft.chisel.api.Statistics;
import com.cricketcraft.chisel.config.Configurations;
import com.cricketcraft.chisel.inventory.InventoryChiselSelection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotChiselSelection extends Slot {
    private InventoryChiselSelection inventory;

    public SlotChiselSelection(InventoryChiselSelection inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.inventory = inventory;
    }

	/**
	 * Prevents user from adding item stack into output slots
	 */
    @Override
    public boolean isItemValid(ItemStack stack) {
    	return false;
    }

    /**
     * Prevent user from taking item from output slot until previous item stack merge/transfer is done
     */
    @Override
    public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
    	if (inventory.container.finished){
    		return false;
    	}

    	return par1EntityPlayer.inventory.getItemStack() == null;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        super.onPickupFromSlot(player, stack);

        ItemStack heldStack = player.inventory.getItemStack();
        ItemStack inputStack = inventory.getStackInSlot(60);

        if (heldStack == null) {
        	inventory.decrStackSize(InventoryChiselSelection.inputSlotIndex, 1);
        } else {
        	putStack(stack.copy());

        	player.inventory.setItemStack(null);

        	if (inventory.getStackInSlot(InventoryChiselSelection.inputSlotIndex) == null)
        		return;

        	player.inventory.setItemStack(new ItemStack(stack.getItem(), inputStack.stackSize, stack.getItemDamage()));
        	inventory.setInventorySlotContents(InventoryChiselSelection.inputSlotIndex, null);
        }

        inventory.updateItems();

        if (Configurations.allowChiselDamage) {
        	inventory.chisel.damageItem(1, player);
        	if (inventory.chisel.stackSize <= 0) {
        		player.inventory.mainInventory[player.inventory.currentItem] = null;
        	}
        }

        if (player.worldObj.isRemote) {
        	//String sound = Carving.chisel.getVariationSound(crafted.getItem(), crafted.getItemDamage());
        	//GeneralClient.playChiselSound(player.worldObj, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ), sound);
        } else {
        	player.addStat(Statistics.blocksChiseled, inputStack.stackSize);
        }
    }
}
