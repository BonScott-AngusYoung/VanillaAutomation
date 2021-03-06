package de.universallp.va.core.block;

import de.universallp.va.VanillaAutomation;
import de.universallp.va.client.gui.guide.EnumEntry;
import de.universallp.va.client.gui.screen.VisualRecipe;
import de.universallp.va.core.item.VAItems;
import de.universallp.va.core.network.PacketHandler;
import de.universallp.va.core.network.messages.MessagePlaySound;
import de.universallp.va.core.tile.TilePlacer;
import de.universallp.va.core.util.VAFakePlayer;
import de.universallp.va.core.util.libs.LibGuiIDs;
import de.universallp.va.core.util.libs.LibNames;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Random;

/**
 * Created by universallp on 19.03.2016 12:11.
 */
public class BlockPlacer extends BlockVA {

    private static VisualRecipe recipe;

    public BlockPlacer() {
        super(Material.ROCK, LibNames.BLOCK_PLACER);
        setCreativeTab(CreativeTabs.REDSTONE);
        setHardness(2);
    }

    public static boolean placeBlock(World worldObj, BlockPos pos, EnumFacing f, VAFakePlayer fakePlayer, ItemStack placeable) {
        fakePlayer.setItemInHand(placeable);
        final IBlockState blockS = worldObj.getBlockState(pos);
        final Block block = Block.getBlockFromItem(placeable.getItem());

        if (block != null) {
            IBlockState placeState = Block.getBlockFromItem(placeable.getItem()).getStateFromMeta(placeable.getItemDamage());
            BlockSnapshot bS;

            if (placeable.hasTagCompound())
                bS = new BlockSnapshot(worldObj, pos, placeState, placeable.getTagCompound());
            else
                bS = new BlockSnapshot(worldObj, pos, placeState);


            BlockEvent.PlaceEvent event = new BlockEvent.PlaceEvent(bS, blockS, fakePlayer);

            if (MinecraftForge.EVENT_BUS.post(event)) return false;

            boolean canPlace = block.canPlaceBlockAt(worldObj, pos);
            if (canPlace) {

                IBlockState s = block.onBlockPlaced(worldObj, pos, f, 0, 0, 0, placeable.getItemDamage(), fakePlayer);

                if (s.getProperties().containsKey(BlockDispenser.FACING)) // For full rotation
                    s = s.withProperty(BlockDispenser.FACING, f);
                if (s.getProperties().containsKey(BlockFurnace.FACING)) { // For horizontal only
                    if (f == EnumFacing.DOWN || f == EnumFacing.UP)
                        f = EnumFacing.NORTH;
                    s = s.withProperty(BlockFurnace.FACING, f);
                }

                worldObj.setBlockState(pos, s);
                block.onBlockAdded(worldObj, pos, s);
                block.onBlockPlaced(worldObj, pos, f, 0, 0, 0, block.getMetaFromState(s), fakePlayer);

                SoundType type = block.getSoundType();
                if (type != null)
                    PacketHandler.INSTANCE.sendToAllAround(new MessagePlaySound(type.getPlaceSound().getSoundName().toString(), pos, type.getPitch(), type.getPitch()), new NetworkRegistry.TargetPoint(fakePlayer.dimension, pos.getX(), pos.getY(), pos.getZ(), 64));
                return true;
            }
        } else {
            if (placeable.getItem().equals(VAItems.itemPokeStick)) {
                fakePlayer.rightClick(placeable, pos, f, 0, 0, 0);
                placeable.damageItem(1, fakePlayer);
            } else if (worldObj.isAirBlock(pos)) {
                fakePlayer.setItemInHand(placeable);
                placeable.getItem().onItemRightClick(placeable, worldObj, fakePlayer, EnumHand.MAIN_HAND);
                placeable.getItem().onItemUse(placeable, fakePlayer, worldObj, pos, EnumHand.MAIN_HAND, f, 0, 0, 0);
                placeable.getItem().onItemUseFirst(placeable, fakePlayer, worldObj, pos, f, 0, 0, 0, EnumHand.MAIN_HAND);
                placeable.getItem().onItemUseFinish(placeable, worldObj, fakePlayer);
            }
        }

        return false;
    }

    public static EnumFacing getFacingFromState(IBlockState s) {
        if (s.getProperties().containsKey(BlockDispenser.FACING))
            return (EnumFacing) s.getProperties().get(BlockDispenser.FACING);
        return null;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote)
            return true;
        else {
            playerIn.openGui(VanillaAutomation.instance, LibGuiIDs.GUI_PLACER, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
    }

    @Override
    public int tickRate(World worldIn) {
        return 4;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            TilePlacer tP = (TilePlacer) worldIn.getTileEntity(pos);
            int slot = tP.getNextPlaceable();

            if (slot == -1) {
                worldIn.playEvent(1001, pos, 0);
                super.updateTick(worldIn, pos, state, rand);
                return;
            }

            ItemStack placable = tP.getStackInSlot(slot);
            EnumFacing f = getFacingFromState(state);

            BlockPos dest;

            if (tP.useRedstone) {
                int redstoneStrength = worldIn.isBlockIndirectlyGettingPowered(pos);
                dest = pos.add(f.getFrontOffsetX() * redstoneStrength, f.getFrontOffsetY() * redstoneStrength, f.getFrontOffsetZ() * redstoneStrength);
            } else {
                dest = pos.add(f.getFrontOffsetX() * tP.reachDistance, f.getFrontOffsetY() * tP.reachDistance, f.getFrontOffsetZ() * tP.reachDistance);
            }

            EnumActionResult r = ForgeHooks.onPlaceItemIntoWorld(placable, VAFakePlayer.instance(worldIn), worldIn, dest, tP.placeFace, 0, 0, 0, EnumHand.MAIN_HAND);

            if (r == EnumActionResult.SUCCESS) {
                IBlockState s = worldIn.getBlockState(dest);
                if (s.getProperties().containsKey(BlockHorizontal.FACING)) {
                    if (tP.placeFace != EnumFacing.UP && tP.placeFace != EnumFacing.DOWN)
                        s = s.withProperty(BlockHorizontal.FACING, tP.placeFace);
                } else if (s.getProperties().containsKey(BlockDirectional.FACING)) {
                    s = s.withProperty(BlockDirectional.FACING, tP.placeFace);
                }
                worldIn.setBlockState(dest, s);

                if (tP.getStackInSlot(slot).stackSize == 0)
                    tP.decrStackSize(slot, 1); // Forge leaves stacks with 0 size so I'll get rid of it
            }
        }

        super.updateTick(worldIn, pos, state, rand);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        TilePlacer te = (TilePlacer) worldIn.getTileEntity(pos);
        boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up());
        boolean flag1 = te.isTriggered;

        if (flag && !flag1) {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
            te.isTriggered = true;
        } else if (!flag && flag1) {
            te.isTriggered = false;
        }
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(BlockDispenser.FACING, BlockPistonBase.getFacingFromEntity(pos, placer));
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TilePlacer();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockDispenser.FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BlockDispenser.FACING, EnumFacing.getFront(meta));
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(BlockDispenser.FACING, rot.rotate(state.getValue(BlockDispenser.FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockDispenser.FACING)));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(BlockDispenser.FACING).getIndex();

        return i;
    }

    @Override
    public VisualRecipe getRecipe() {
        if (recipe != null)
            return recipe;

        ItemStack cobbleStone = new ItemStack(Blocks.COBBLESTONE, 1);
        ItemStack piston = new ItemStack(Blocks.PISTON, 1);
        ItemStack chest  = new ItemStack(Blocks.CHEST, 1);

        recipe = new VisualRecipe(new ItemStack[] { cobbleStone, cobbleStone, cobbleStone,
                                                    cobbleStone, chest,       cobbleStone,
                cobbleStone, piston, cobbleStone }, new ItemStack(VABlocks.placer, 1), VisualRecipe.EnumRecipeType.SHAPED);

        return recipe;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof IInventory) {

            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) te);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public EnumEntry getEntry() {
        return EnumEntry.BLOCK_PLACER;
    }
}
