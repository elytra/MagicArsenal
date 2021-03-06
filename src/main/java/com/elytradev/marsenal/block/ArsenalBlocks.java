/*
 * MIT License
 *
 * Copyright (c) 2018 Isaac Ellingson (Falkreon) and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.marsenal.block;

import java.util.ArrayList;
import java.util.List;

import com.elytradev.marsenal.tile.TileEntityBerkanoStele;
import com.elytradev.marsenal.tile.TileEntityChaosOrb;
import com.elytradev.marsenal.tile.TileEntityChaosResonator;
import com.elytradev.marsenal.tile.TileEntityFehuStele;
import com.elytradev.marsenal.tile.TileEntityJeraStele;
import com.elytradev.marsenal.tile.TileEntityKenazStele;
import com.elytradev.marsenal.tile.TileEntityRadiantBeacon;
import com.elytradev.marsenal.tile.TileEntityRaidhoStele;
import com.elytradev.marsenal.tile.TileEntityRunicAltar;
import com.elytradev.marsenal.tile.TileEntityWunjoStele;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class ArsenalBlocks {
	private static List<Block> blocksForItems = new ArrayList<>();
	
	public static BlockPoisonDoublePlant CROP_WOLFSBANE  = null;
	public static BlockPoisonDoublePlant CROP_NIGHTSHADE = null;
	public static BlockRunestone         RUNESTONE1      = null;
	public static BlockRunestone         RUNESTONE2      = null;
	public static BlockRunicAltar        RUNIC_ALTAR     = null;
	public static BlockSimple            STELE_UNCARVED  = null;
	public static BlockKenazStele        STELE_KENAZ     = null;
	public static BlockRaidhoStele       STELE_RAIDHO    = null;
	public static BlockWunjoStele        STELE_WUNJO     = null;
	public static BlockBerkanoStele      STELE_BERKANO   = null;
	public static BlockFehuStele         STELE_FEHU      = null;
	public static BlockJeraStele         STELE_JERA      = null;
	
	public static BlockRosettaStone      ROSETTA_STONE   = null;
	public static BlockChaosOrb          CHAOS_ORB       = null;
	public static BlockChaosResonator    CHAOS_RESONATOR = null;
	public static BlockRadiantBeacon     RADIANT_BEACON  = null;
	
	@SubscribeEvent
	public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> r = event.getRegistry();
		
		CROP_WOLFSBANE  = blockNoItem(r, new BlockPoisonDoublePlant("wolfsbane"));
		CROP_NIGHTSHADE = blockNoItem(r, new BlockPoisonDoublePlant("nightshade"));
		RUNESTONE1 = block(r, new BlockRunestone("1", EnumRuneCarving.FIRST_16));
		RUNESTONE2 = block(r, new BlockRunestone("2", EnumRuneCarving.SECOND_16));
		RUNIC_ALTAR    = block(r, new BlockRunicAltar());
		STELE_UNCARVED = block(r, new BlockSimple("stele.uncarved"));
		STELE_RAIDHO   = block(r, new BlockRaidhoStele());
		STELE_KENAZ    = block(r, new BlockKenazStele());
		STELE_WUNJO    = block(r, new BlockWunjoStele());
		STELE_BERKANO  = block(r, new BlockBerkanoStele());
		STELE_FEHU     = block(r, new BlockFehuStele());
		STELE_JERA     = block(r, new BlockJeraStele());
		
		ROSETTA_STONE  = block(r, new BlockRosettaStone());
		CHAOS_ORB      = block(r, new BlockChaosOrb());
		CHAOS_RESONATOR= block(r, new BlockChaosResonator());
		RADIANT_BEACON = block(r, new BlockRadiantBeacon());
		
		GameRegistry.registerTileEntity(TileEntityRunicAltar.class,     "magicarsenal.altar");
		GameRegistry.registerTileEntity(TileEntityRaidhoStele.class,    "magicarsenal.stele.raidho");
		GameRegistry.registerTileEntity(TileEntityKenazStele.class,     "magicarsenal.stele.kenaz");
		GameRegistry.registerTileEntity(TileEntityWunjoStele.class,     "magicarsenal.stele.wunjo");
		GameRegistry.registerTileEntity(TileEntityBerkanoStele.class,   "magicarsenal.stele.berkano");
		GameRegistry.registerTileEntity(TileEntityFehuStele.class,      "magicarsenal.stele.fehu");
		GameRegistry.registerTileEntity(TileEntityJeraStele.class,      "magicarsenal.stele.jera");
		
		GameRegistry.registerTileEntity(TileEntityChaosOrb.class,       "magicarsenal.chaosorb");
		GameRegistry.registerTileEntity(TileEntityChaosResonator.class, "magicarsenal.chaosresonator");
		GameRegistry.registerTileEntity(TileEntityRadiantBeacon.class,  "magicarsenal.radiantbeacon");
		
		//No, you can't carry the crops around.
		FMLInterModComms.sendMessage("charset", "removeCarry", CROP_WOLFSBANE.getRegistryName());
		FMLInterModComms.sendMessage("charset", "removeCarry", CROP_NIGHTSHADE.getRegistryName());
	}
	
	public static Iterable<Block> blocksForItems() {
		return blocksForItems;
	}
	
	public static <T extends Block> T block(IForgeRegistry<Block> registry, T t) {
		registry.register(t);
		blocksForItems.add(t);
		return t;
	}
	
	public static <T extends Block> T blockNoItem(IForgeRegistry<Block> registry, T t) {
		registry.register(t);
		return t;
	}
}
