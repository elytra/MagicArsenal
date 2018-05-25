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

package com.elytradev.marsenal.tile;

import java.util.Map;

import com.elytradev.marsenal.MagicArsenal;
import com.elytradev.marsenal.block.ArsenalBlocks;
import com.elytradev.marsenal.block.BlockChaosResonator;
import com.elytradev.marsenal.capability.impl.DeepEnergyStorage;
import com.elytradev.marsenal.capability.impl.ShallowEnergyStorage;
import com.elytradev.marsenal.compat.EnergyCompat;
import com.elytradev.marsenal.compat.ProbeDataCompat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

public class TileEntityChaosResonator  extends TileEntity implements ITickable, IAuxNetworkParticipant {
	public static final int SCAN_PERIOD = 20*30;
	public static final int SCAN_DISTANCE = 40;
	public static long BASE_MAX      = 10_000L;
	public static long BASE_TRANSFER = 1_000L;
	
	protected boolean outputMode = false;
	protected ShallowEnergyStorage storage = new ShallowEnergyStorage();
	protected BlockPos orb = null;
	//protected long lastOrbPing = 0L;
	protected int scanTimer = 0;
	
	protected BlockPos controller = null;
	protected long lastControllerPing = 0L;
	protected BlockPos beamTo = null;
	
	public TileEntityChaosResonator() {
		storage.setMax(BASE_MAX);
		storage.setTransferLimit(BASE_TRANSFER);
		storage.listen(this::markDirty);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (MagicArsenal.CAPABILITY_TESLA_HOLDER!=null && capability==MagicArsenal.CAPABILITY_TESLA_HOLDER) return true;
		if (MagicArsenal.CAPABILITY_TESLA_PRODUCER!=null && capability==MagicArsenal.CAPABILITY_TESLA_PRODUCER) return true;
		if (MagicArsenal.CAPABILITY_TESLA_CONSUMER!=null && capability==MagicArsenal.CAPABILITY_TESLA_CONSUMER) return true;
		
		if (capability==CapabilityEnergy.ENERGY) return true;
		
		if (MagicArsenal.CAPABILITY_PROBEDATA!=null && capability==MagicArsenal.CAPABILITY_PROBEDATA) return true;
		
		return super.hasCapability(capability, facing);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (MagicArsenal.CAPABILITY_TESLA_HOLDER!=null && capability==MagicArsenal.CAPABILITY_TESLA_HOLDER) return (T) storage;
		if (MagicArsenal.CAPABILITY_TESLA_PRODUCER!=null && capability==MagicArsenal.CAPABILITY_TESLA_PRODUCER) return (T) storage;
		if (MagicArsenal.CAPABILITY_TESLA_CONSUMER!=null && capability==MagicArsenal.CAPABILITY_TESLA_CONSUMER) return (T) storage;
		
		if (capability==CapabilityEnergy.ENERGY) return (T) storage;
		
		if (MagicArsenal.CAPABILITY_PROBEDATA!=null && capability==MagicArsenal.CAPABILITY_PROBEDATA) return (T) ProbeDataCompat.getProvider(this);
		
		return super.getCapability(capability, facing);
	}
	
	@Override
	public void update() {
		if (world==null || world.isRemote) return;
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock()!=ArsenalBlocks.CHAOS_RESONATOR) return;
		EnumFacing front = state.getValue(BlockChaosResonator.FACING);
		BlockChaosResonator.EnumMode mode = state.getValue(BlockChaosResonator.MODE);
		switch(mode) {
		case INSERT:
			//Don't push any power out. We'll insert power from ourselves into the orb when we get an orb ping
			break;
		case EXTRACT:
			//Push energy out to consumers. We'll extract power from the orb and put it in ourselves when we get an orb ping.
			if (storage.getLevel()>0) pushEnergyOut(storage.getTransferLimit(), front);
			break;
		case BALANCE:
			//Try to keep power at 50% by pulling from the orb or pushing to consumers
			long halfPower = storage.getCapacity()/2;
			if (storage.getLevel()>halfPower) {
				pushEnergyOut(storage.getLevel()-halfPower, front);
			} else if (storage.getLevel()<halfPower) {
				//Do nothing - we're pulling from the orb
			}
			break;
		}
		
		//Speaking of which, where's the orb these days?
		if (orb!=null) {
			//should we invalidate it?
			if (world.isAreaLoaded(orb, 0)) {
				TileEntity te = world.getTileEntity(orb);
				if (te==null || !(te instanceof TileEntityChaosOrb)) orb = null;
			} else {
				orb = null;
			}
		} else {
			//Should we search for it?
			scanTimer--;
			if (scanTimer<=0) {
				scanTimer = SCAN_PERIOD;
				
				BlockPos scanTarget = pos;
				for(int i=0; i<SCAN_DISTANCE; i++) {
					if (!world.isAreaLoaded(scanTarget, 0)) continue;
					Chunk chunk = world.getChunkFromBlockCoords(scanTarget);
					
					for(Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
						if (isInLine(pos, entry.getKey(), front)) {
							if (entry.getValue() instanceof TileEntityChaosOrb) {
								//We found our orb
								orb = entry.getKey();
								((TileEntityChaosOrb)entry.getValue()).pingFromResonator(pos);
								System.out.println("Connected to ORB at "+orb);
								
								return;
							}
						}
					}
					
					scanTarget = scanTarget.offset(front, 16);
				}
				//System.out.println("No ORB found.");
			}
		}
	}
	
	public static boolean isInLine(BlockPos a, BlockPos b, EnumFacing facing) {
		if (a.getY()!=b.getY()) return false; //can't face up or down
		
		switch(facing) {
		case NORTH:
			return a.getX()==b.getX() && a.getZ() > b.getZ();
		case SOUTH:
			return a.getX()==b.getX() && a.getZ() < b.getZ();
		case EAST:
			return a.getZ()==b.getZ() && a.getX() < b.getX();
		case WEST:
			return a.getZ()==b.getZ() && a.getX() > b.getX();
		default:
			return false;
		}
	}
	
	public ShallowEnergyStorage getEnergyStorage() {
		return storage;
	}
	
	public void pushEnergyOut(long amount, EnumFacing exclude) {
		
		long toTransferTotal = Math.min(amount, storage.getTransferLimit());
		long toTransferRemaining = toTransferTotal;
		
		for(EnumFacing facing : EnumFacing.VALUES) {
			if (exclude!=null && facing.equals(exclude)) continue;
			
			//Look for an energy consumer
			EnergyCompat.IFusionEnergy dest = EnergyCompat.getEnergyWrapper(world, pos.offset(facing), facing.getOpposite());
			//System.out.println("Trying to push to block "+world.getBlockState(pos.offset(facing)).getBlock().getLocalizedName()+" at "+(pos.offset(facing)));
			//if (dest==EnergyCompat.DUMMY_ENERGY) System.out.println("Dest is a dummy energy cap");
			
			long transferred = EnergyCompat.tryPushEnergy(storage, dest, toTransferRemaining);
			
			//if (transferred>0) System.out.println(""+transferred+" transferred.");
			
			toTransferRemaining -= transferred;
			if (toTransferRemaining<=0L) break;
		}
	}
	
	/**
	 * Called by the orb in a kind of cooperative multitasking
	 */
	public void transferFromOrb(DeepEnergyStorage orbStorage) {
		if (world==null || world.isRemote) return;

		//this.lastOrbPing = world.getTotalWorldTime();
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock()!=ArsenalBlocks.CHAOS_RESONATOR) return;
		BlockChaosResonator.EnumMode mode = state.getValue(BlockChaosResonator.MODE);
		
		switch(mode) {
			case INSERT: {
				long amount = storage.getLevel();
				long inserted = orbStorage.insert(amount, false);
				storage.setLevel(storage.getLevel()-inserted);
				break;
			}
			case EXTRACT: {
				if (storage.getLevel()>=storage.getMax()) return;
				long amount = storage.getMax()-storage.getLevel();
				long extracted = orbStorage.extract(amount, false);
				storage.setLevel(amount+extracted);
				break;
			}
			case BALANCE: {
				long halfPower = storage.getCapacity()/2;
				if (storage.getLevel()>halfPower) {
					//Push energy back towards the orb
					long amount = storage.getLevel() - halfPower;
					long pushed = orbStorage.insert(amount, false);
					//System.out.println("Pushed "+pushed+" out of desired "+amount+" to Chaos ORB");
					
					storage.setLevel(storage.getLevel()-pushed);
				} else if (storage.getLevel()<halfPower) {
					//Extract energy from the orb
					long amount = halfPower - storage.getLevel();
					long pulled = orbStorage.extract(amount, false);
					storage.setLevel(storage.getLevel()+pulled);
				}
				break;
			}
		}
	}
	
	/*
	 * #### IAUXNETWORKPARTICIPANT IMPL ####
	 */
	
	@Override
	public boolean canJoinNetwork(BlockPos controller) {
		if (this.controller==null || this.controller.equals(controller)) return true;
		
		long now = world.getTotalWorldTime();
		return now-lastControllerPing > 20*5; //Has it been five seconds since the last poll?
	}
	
	@Override
	public void joinNetwork(BlockPos controller, BlockPos beamTo) {
		System.out.println("Resonator joined an altar network.");
		this.controller = controller;
		this.beamTo = beamTo;
	}
	
	@Override
	public void pollNetwork(BlockPos controller, BlockPos beamTo) {
		if (world!=null) lastControllerPing = world.getTotalWorldTime();
		this.beamTo = beamTo;
	}
	
	@Override
	public BlockPos getBeamTo() {
		return beamTo;
	}
	
	@Override
	public String getParticipantType() {
		return "chaosresonator";
	}
	
	@Override
	public void pollAuxRadiance(int radiance) {
		this.storage.setMax(BASE_MAX + radiance*10);
		storage.setTransferLimit(BASE_TRANSFER + radiance);
		
		if (orb!=null) {
			TileEntity te = world.getTileEntity(orb);
			if (te!=null && te instanceof TileEntityChaosOrb) {
				((TileEntityChaosOrb)te).updateRadiance(radiance);
			}
		}
	}
}
