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

package com.elytradev.marsenal.client;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;

public class MagmaBlastEmitter extends Emitter {
	private int ticksRemaining = 20;
	private Random random = new Random();
	private ArrayList<Star> stars = new ArrayList<>();
	
	@Override
	public void tick() {
		if (entity!=null && ticksRemaining>15) {
			int spawns = 32;
			if (Minecraft.getMinecraft().gameSettings.particleSetting==1) spawns=16;
			if (Minecraft.getMinecraft().gameSettings.particleSetting==2) spawns=8;
			
			for(int i=0; i<spawns; i++) {
				Vec3d lookVec = entity.getLookVec();
				lookVec = lookVec.addVector(random.nextGaussian()*0.5f, random.nextGaussian()*0.5f, random.nextGaussian()*0.5f).normalize(); //Fuzz
				lookVec = lookVec.scale(0.9f); //velocity magnitude
				
				Star star = new Star();
				star.width = 0.4f;
				star.taild2 = 0.5f*0.5f;
				//star.limit = 0.4f;
				star.move((float)entity.posX, (float)entity.posY+entity.getEyeHeight(), (float)entity.posZ);
				star.color = 0xccff3300;
				star.fuzzColor(0.6f, 0.6f, 0.3f);
				star.lifetime = 4;
				star.setVelocity(lookVec);
				stars.add(star);
			}
		}
		
		
		ticksRemaining--;
		if (ticksRemaining<=0) kill();
	}
	
	@Override
	public void draw(float partialFrameTime, double dx, double dy, double dz) {
		Emitter.drawStars(partialFrameTime, dx, dy, dz, stars, true);
	}

}
