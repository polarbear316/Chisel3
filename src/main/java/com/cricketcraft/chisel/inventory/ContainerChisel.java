package com.cricketcraft.chisel.inventory;

import com.cricketcraft.chisel.api.CarvingRegistry;
import com.cricketcraft.chisel.inventory.slots.SlotChiselInput;
import com.cricketcraft.chisel.inventory.slots.SlotChiselSelection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerChisel extends Container {
	public InventoryChiselSelection inventory;
	public InventoryPlayer playerInventory;
	int chiselSlot;
	public ItemStack chisel;
	public boolean finished = false;
	public CarvingRegistry carving;

    /**
     * Slot IDs:
     * 0-59: Chisel GUI
     * 60: Chisel Input
     * 61-87: Player Inventory
     * 88-96: Hotbar
     */
    public ContainerChisel(InventoryPlayer inventoryPlayer, InventoryChiselSelection chiselSelection) {
        inventory = chiselSelection;
        inventory.container = this;
        playerInventory = inventoryPlayer;
        chiselSlot = playerInventory.currentItem;

        for(int c = 0; c < 60; c++) {
            addSlotToContainer(new SlotChiselSelection(chiselSelection, c, 62 + ((c % 10) * 18), 8 + ((c / 10) * 18)));
        }

        addSlotToContainer(new SlotChiselInput(chiselSelection, 60, 24, 24));

        bindPlayerInventory(playerInventory);

        inventory.openInventory(playerInventory.player);
        //getHeldItemMainhand() to get currently held item
        chisel = playerInventory.player.getHeldItemMainhand();
    }

	/**
	 * Listen for container close event, call InventoryChiselSelection#closeInventory to
	 * save any items in chisel input
	 */
	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
	    super.onContainerClosed(playerIn);
	    inventory.closeInventory(playerIn);
	}

    private void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        int top = 120;
        int left = 71;
        // main inv
        for (int i = 0; i < 27; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i + 9, left + ((i % 9) * 18), top + (i / 9) * 18));
        }

        top += 58;
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i, left + ((i % 9) * 18), top + (i / 9) * 18));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack originalStack = null;
        Slot clickedSlot = inventorySlots.get(index);
        if(clickedSlot != null && clickedSlot.getHasStack()) {
            ItemStack transferStack = clickedSlot.getStack();
            originalStack = transferStack.copy();

            if(index < 61) {
            	//Player clicked a chisel output slot, transferStack = chiseled output stack

            	//Chisel items from chisel input to chiseled output(transferStack) 
            	player.inventory.setItemStack(transferStack.copy());
            	clickedSlot.onPickupFromSlot(player, transferStack);
            	transferStack = player.inventory.getItemStack();
            	player.inventory.setItemStack(null);

            	//Merge chiseled stack to the player's inventory
            	if(!mergeItemStack(transferStack, 62, 97, true))
            		return null;
            } else {
            	//Player clicked slot from their inventory, transferStack = chisel input stack
            	//Merge transferStack to the chisel input slot
            	if(!mergeItemStack(transferStack, 60, 61, false))
                    return null;
            }

            //trigger slot change event, triggers any Slot#onCrafting event overrides if needed 
            clickedSlot.onSlotChange(transferStack, originalStack);

            if(transferStack.stackSize == 0)
                clickedSlot.putStack(null);
            else
                clickedSlot.onSlotChanged();

            if(transferStack.stackSize == originalStack.stackSize)
                return null;

            if (index >= InventoryChiselSelection.normalSlots) {
            	clickedSlot.onPickupFromSlot(player, transferStack);
            }

            if (transferStack.stackSize == 0) {
            	clickedSlot.putStack(null);
            	return null;
            }
        }

        return originalStack;
    }

	public void onChiselSlotChanged() {
		ItemStack stack = playerInventory.mainInventory[chiselSlot];
		if (stack == null || !stack.isItemEqual(chisel))
			finished = true;

		if (finished) {
			finished = false;
			return;
		}

		playerInventory.mainInventory[chiselSlot] = chisel;
	}
}
