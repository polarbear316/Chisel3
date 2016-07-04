package com.cricketcraft.chisel;

import com.cricketcraft.chisel.api.CarvingRegistry;
import com.cricketcraft.chisel.api.ChiselAPI;
import com.cricketcraft.chisel.api.Statistics;
import com.cricketcraft.chisel.config.Configurations;
import com.cricketcraft.chisel.init.ChiselBlocks;
import com.cricketcraft.chisel.init.ChiselItems;
import com.cricketcraft.chisel.init.ChiselRecipes;
import com.cricketcraft.chisel.init.ChiselTabs;
import com.cricketcraft.chisel.item.chisel.ChiselController;
import com.cricketcraft.chisel.network.ChiselGuiHandler;
import com.cricketcraft.chisel.network.PacketHandler;
import com.cricketcraft.chisel.proxy.CommonProxy;
import com.cricketcraft.chisel.world.GeneratorChisel;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = Chisel.MOD_ID, name = Chisel.MOD_NAME, version = Chisel.VERSION, guiFactory = "com.cricketcraft.chisel.client.gui.GuiFactory", dependencies = "after:ForgeMultipart;after:Thaumcraft;after:appliedenergistics2;after:Railcraft;after:AWWayofTime;after:TwilightForest")
public class Chisel {
	public static final String MOD_ID = "chisel";
	public static final String MOD_NAME = "Chisel 3";
	public static final String VERSION = "@VERSION@";
	public static final Logger logger = LogManager.getLogger(MOD_NAME);

	@Instance(MOD_ID)
	public static Chisel instance;

	/**
	 * TODO: I need to make a few things act like panes
	 * Glass pane, Stained glass panes, and iron bars
	 * I also need to make sure the glass renders like glass
	 * Connected texture work will being once I am done working on making sure all the blocks load
	 */
	public Chisel() {
		ChiselAPI.MOD_ID = MOD_ID;
	}

	@SidedProxy(clientSide = "com.cricketcraft.chisel.proxy.ClientProxy", serverSide = "com.cricketcraft.chisel.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void missingMapping(FMLMissingMappingsEvent event) {
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		File configFile = event.getSuggestedConfigurationFile();
		Configurations.configExists = configFile.exists();
		Configurations.config = new Configuration(configFile);
		Configurations.config.load();
		Configurations.refreshConfig();

		ChiselBlocks.preInit();
		ChiselItems.preInit();
		ChiselTabs.preInit();
		PacketHandler.preInit();
		ChiselController.INSTANCE.preInit();
		CarvingRegistry.preInit();

		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ChiselItems.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new ChiselGuiHandler());
		GeneratorChisel.INSTANCE.addFeature(ChiselBlocks.marble.getDefaultState(), 32, Configurations.marbleAmount);
		GeneratorChisel.INSTANCE.addFeature(ChiselBlocks.limestone.getDefaultState(), 32, Configurations.marbleAmount);
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		CarvingRegistry.init();
		ChiselRecipes.init();
		Statistics.init();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void onIMC(IMCEvent event) {
		for (IMCMessage msg : event.getMessages()) {
			// IMCHandler.INSTANCE.handleMessage(msg);
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals("chisel")) {
			Configurations.refreshConfig();
		}
	}

	@EventHandler
	public void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
		event.player.getEntityData().setBoolean("chiselDarkMode", false);
	}
}
