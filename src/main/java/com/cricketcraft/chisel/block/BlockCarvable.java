package com.cricketcraft.chisel.block;

import com.cricketcraft.chisel.util.BlockVariant;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockCarvable extends Block {

//    public static final PropertyBool CONNECTS_NORTH = PropertyBool.create("north");
//    public static final PropertyBool CONNECTS_EAST = PropertyBool.create("east");
//    public static final PropertyBool CONNECTS_SOUTH = PropertyBool.create("south");
//    public static final PropertyBool CONNECTS_WEST = PropertyBool.create("west");
//    public static final PropertyBool CONNECTS_UP = PropertyBool.create("up");
//    public static final PropertyBool CONNECTS_DOWN = PropertyBool.create("down");

    public BlockCarvable(Material material, MapColor mapColor) {
        super(material, mapColor);
        setHardness(2.0F);
        setResistance(5.0F);
    }

    public BlockCarvable(Material material) {
        super(material);
        setHardness(2.0F);
        setResistance(5.0F);
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this), 1, getMetaFromState(world.getBlockState(pos)));
    }

    @Nullable
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return new ItemStack(this, 1, getMetaFromState(state)).getItem();
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this, 1, getMetaFromState(state));
    }
}
