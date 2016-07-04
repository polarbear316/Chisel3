package com.cricketcraft.chisel.network.message;

import com.cricketcraft.chisel.api.IChiselItem;
import com.cricketcraft.chisel.api.IChiselMode;
import com.cricketcraft.chisel.item.chisel.ChiselController;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChiselMode implements IMessage {

		public MessageChiselMode(){
		}

		private String mode;

		public MessageChiselMode(IChiselMode iChiselMode) {
			this.mode = iChiselMode.name();
		}

		public void toBytes(ByteBuf buf) {
			ByteBufUtils.writeUTF8String(buf, mode);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			this.mode = ByteBufUtils.readUTF8String(buf);
		}

		public static class Handler implements IMessageHandler<MessageChiselMode, IMessage> {
			@Override
			public IMessage onMessage(MessageChiselMode message, MessageContext ctx) {
				ItemStack stack = ctx.getServerHandler().playerEntity.getHeldItemMainhand();
				if (stack != null && stack.getItem() instanceof IChiselItem) {
					ChiselController.setMode(stack, message.mode);
				}
				return null;
			}
		}

}
