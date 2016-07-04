package com.cricketcraft.chisel.item.chisel;

import com.cricketcraft.chisel.api.CarvingRegistry;
import com.cricketcraft.chisel.api.ICarvingRecipe;
import com.cricketcraft.chisel.api.IChiselItem;
import com.cricketcraft.chisel.api.IChiselMode;
import com.google.common.base.Strings;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ChiselController {
    public static final ChiselController INSTANCE = new ChiselController();

    private static final String MODE_KEY = "chiselMode";

    private long lastTickClick = 0;

    private ChiselController() {

    }

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.LeftClickBlock event) {
    	//Only process if world is "server", and if interaction from the main hand
    	if(event.getWorld().isRemote || event.getHand() == EnumHand.OFF_HAND) return;

        EntityPlayer player = event.getEntityPlayer();
        ItemStack held = player.getHeldItemMainhand();

        //Only continue if held item was the chisel
        if (held == null || !(held.getItem() instanceof IChiselItem)) {
            return;
        }
        if(player.capabilities.isCreativeMode) {
            event.setCanceled(true);
        }

        IBlockState state = event.getWorld().getBlockState(event.getPos());
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);

        ItemStack stack = new ItemStack(block, 1, meta);
        getMode(held).chisel(player, event.getWorld(), event.getPos(), event.getFace(), CarvingRegistry.getRecipeFromItemStack(stack));
        held.damageItem(1, player);
    }

    /**
     * @param recipe
     * @param originalBlockStack The block that is being chiseled in the world.
     */
    @SuppressWarnings("deprecation")
    public static boolean chiselBlockInWorld(ICarvingRecipe recipe, ItemStack originalBlockStack, BlockPos pos, World world, boolean isShifting) {
    	IBlockState targetState = world.getBlockState(pos);
    	ItemStack targetStack = new ItemStack(targetState.getBlock(), 1, targetState.getBlock().getMetaFromState(targetState));

    	//Only continue if recipe is valid, and original and target block are same type 
    	if( recipe == null || recipe.getChiselResults() == null || !originalBlockStack.isItemEqual(targetStack) ){
    		return false;
    	}

        int var = 0;
        ItemStack out;
        ItemStack[] results = recipe.getChiselResults();

        for(int i=0; i<results.length; i++){
        	if(results[i].isItemEqual(originalBlockStack)){
        		var = isShifting ? (i-1<0 ? results.length-1 : i-1) : (i+1)%results.length;
        		break;
        	}
        }

        out = results[var];

        Block block = Block.getBlockFromItem(out.getItem());
        IBlockState state = block.getStateFromMeta(out.getItemDamage());

        if(block.getMaterial(state) == Material.WOOD) {
            //world.playSound((EntityPlayer) null, pos.getX(), pos.getY(), pos.getZ(), ChiselSound.chiselWood, SoundCategory.NEUTRAL, 1, 1);
        } else {
            //world.playSound((EntityPlayer) null, pos.getX(), pos.getY(), pos.getZ(), ChiselSound.chiselFallback, SoundCategory.NEUTRAL, 1, 1);
        }

        return world.setBlockState(pos, state);
    }

    /**
     * Get the mode of the chisel
     * @param chisel
     * @return IChiselMode
     */
    public static IChiselMode getMode(ItemStack chisel) {
    	if(chisel == null) return ChiselMode.SINGLE;

    	if(chisel.getTagCompound() == null){
    		chisel.setTagCompound(new NBTTagCompound());
    	}

    	if(Strings.isNullOrEmpty(chisel.getTagCompound().getString(MODE_KEY))) {
            chisel.getTagCompound().setString(MODE_KEY, ChiselMode.SINGLE.name());
        }

        return Enum.valueOf(ChiselMode.class, chisel.getTagCompound().getString(MODE_KEY));
    }

    /**
     * Set the mode of the chisel
     * @param chisel
     * @param name The name of the chisel mode
     */
    public static void setMode(ItemStack chisel, String name) {
        chisel.getTagCompound().setString(MODE_KEY, name);
    }

    /**
     * Get the ItemStack that was saved from SlotChiselInput
     * @param chisel
     * @return ItemStack
     */
    public static ItemStack loadChiselInput(ItemStack chisel) {
    	NBTTagCompound tagCompound = chisel.getTagCompound();
    	//Create new NBT if one wasn't already created
    	if(tagCompound == null){
    		tagCompound = new NBTTagCompound();
    		chisel.setTagCompound(tagCompound);
    	}
    	NBTTagList tagList = tagCompound.getTagList("Item", 10);
    	return ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(0));
    }

	/**
     * Save the ItemStack that is in SlotChiselInput
     * @param chisel
     * @param chiselInput
     */
    public static void saveChiselInput(ItemStack chisel, ItemStack chiselInput) {
    	NBTTagList tags = new NBTTagList();
    	NBTTagCompound data = new NBTTagCompound();
    	if(chiselInput != null){
    		chiselInput.writeToNBT(data);
    	}
    	tags.appendTag(data);
    	chisel.getTagCompound().setTag("Item", tags);
    }

    public enum ChiselMode implements IChiselMode {
        SINGLE {
            @Override
            public void chisel(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, ICarvingRecipe recipe) {
                ItemStack stack = new ItemStack(world.getBlockState(pos).getBlock(), 1, world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)));
                chiselBlockInWorld(recipe, stack, pos, world, player.isSneaking());
            }

            @Override
            public IChiselMode next() {
                return PANEL;
            }
        },
        PANEL {
            @Override
            public void chisel(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, ICarvingRecipe recipe) {
                IBlockState state = world.getBlockState(pos);
                ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
                int xPos = pos.getX();
                int yPos = pos.getY();
                int zPos = pos.getZ();

                for(int x = -1; x <= 1; x++) {
                    for(int y = -1; y <= 1; y++) {
                        if(facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
                            chiselBlockInWorld(recipe, stack, new BlockPos(xPos + x, yPos, zPos + y), world, player.isSneaking());
                        } else if(facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
                            chiselBlockInWorld(recipe, stack, new BlockPos(xPos, yPos + y, zPos + x), world, player.isSneaking());
                        } else {
                            chiselBlockInWorld(recipe, stack, new BlockPos(xPos + x, yPos + y, zPos), world, player.isSneaking());
                        }
                    }
                }
            }

            @Override
            public IChiselMode next() {
                return COLUMN;
            }
        },
        COLUMN {
            @Override
            public void chisel(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, ICarvingRecipe recipe) {
                IBlockState state = world.getBlockState(pos);
                ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
                int xPos = pos.getX();
                int yPos = pos.getY();
                int zPos = pos.getZ();

                for(int c = -1; c <= 1; c++) {
                    if(facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
                        chiselBlockInWorld(recipe, stack, new BlockPos(xPos, yPos + c, zPos), world, player.isSneaking());
                    } else {
                        if(facing.getIndex() == 0 || facing.getIndex() == 2) {
                            chiselBlockInWorld(recipe, stack, new BlockPos(xPos, yPos, zPos + c), world, player.isSneaking());
                        } else {
                            chiselBlockInWorld(recipe, stack, new BlockPos(xPos + c, yPos, zPos), world, player.isSneaking());
                        }
                    }
                }
            }

            @Override
            public IChiselMode next() {
                return ROW;
            }
        },
        ROW {
            @Override
            public void chisel(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, ICarvingRecipe recipe) {
                IBlockState state = world.getBlockState(pos);
                ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
                int xPos = pos.getX();
                int yPos = pos.getY();
                int zPos = pos.getZ();

                for(int c = -1; c <= 1; c++) {
                    if(facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
                        if(facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
                            chiselBlockInWorld(recipe, stack, new BlockPos(xPos, yPos, zPos + c), world, player.isSneaking());
                        } else {
                            chiselBlockInWorld(recipe, stack, new BlockPos(xPos + c, yPos, zPos), world, player.isSneaking());
                        }
                    } else {
                        if(facing.getIndex() == 0 || facing.getIndex() == 2) {
                            chiselBlockInWorld(recipe, stack, new BlockPos(xPos + c, yPos, zPos), world, player.isSneaking());
                        } else {
                            chiselBlockInWorld(recipe, stack, new BlockPos(xPos, yPos, zPos + c), world, player.isSneaking());
                        }
                    }
                }
            }

            @Override
            public IChiselMode next() {
                return SINGLE;
            }
        }
    }
}
