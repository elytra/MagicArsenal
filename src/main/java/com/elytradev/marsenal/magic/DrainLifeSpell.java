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

package com.elytradev.marsenal.magic;

import com.elytradev.marsenal.ArsenalConfig;
import com.elytradev.marsenal.SpellEvent;
import com.elytradev.marsenal.capability.IMagicResources;
import com.elytradev.marsenal.network.SpawnParticleEmitterMessage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;

public class DrainLifeSpell implements ISpellEffect {
	TargetData targets;
	private int counter = 5;
	
	@Override
	public void activate(EntityLivingBase caster, IMagicResources res) {
		targets = new TargetData(caster);
		//Find a victim
		if (res.getGlobalCooldown()<=0) {
			targets.targetEntity(8);
			if (targets.getTargets().isEmpty()) return;
			if (!(targets.getTargets().get(0) instanceof EntityLiving)) {
				//Don't try to activate for inert entities
				targets.getTargets().clear();
				return;
			}
			SpellEvent event = new SpellEvent.CastOnEntity("drainLife", targets.caster, targets.getTargets().get(0), EnumElement.UNDEATH, EnumElement.NATURE);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				targets.getTargets().clear();
				return;
			}
			
			int spent = res.spend(IMagicResources.RESOURCE_STAMINA, ArsenalConfig.get().spells.drainLife.cost, ArsenalConfig.get().resources.maxStamina, true);
			if (spent<=0) {
				targets.getTargets().clear();
				return;
			}
			
			res.setGlobalCooldown(ArsenalConfig.get().spells.drainLife.cooldown);
		} else {
			//Fizz?
		}
	}

	@Override
	public int tick() {
		if (targets.getTargets().isEmpty()) return 0;
		
		for(Entity target : targets.getTargets()) {
			if (target instanceof EntityLiving) {
				new SpawnParticleEmitterMessage("drainLife").at(target).from(targets.caster).sendToAllWatchingAndSelf(target);
				
				EntityLiving living = (EntityLiving)target;
				boolean success = living.attackEntityFrom(new SpellDamageSource(targets.caster, "drain_life", EnumElement.UNDEATH,  EnumElement.NATURE), ArsenalConfig.get().spells.drainLife.potency);
				if (success) {
					new SpawnParticleEmitterMessage("infuseLife").at(targets.caster).sendToAllWatchingAndSelf(targets.caster);
					targets.caster.heal(ArsenalConfig.get().spells.drainLife.potency/2f);
				}
			}
		}
		
		counter--;
		return (counter<=0) ? 0 : 20*2;
	}
}
