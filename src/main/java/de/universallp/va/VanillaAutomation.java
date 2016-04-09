package de.universallp.va;

import de.universallp.va.core.CommonProxy;
import de.universallp.va.core.handler.CrashReportHandler;
import de.universallp.va.core.util.libs.LibNames;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * Created by universallp on 19.03.2016 11:28.
 */
@Mod(modid = LibNames.MOD_ID, version = LibNames.MOD_VERSION)
public class VanillaAutomation {

    @Mod.Instance
    public static VanillaAutomation instance;

    @SidedProxy(clientSide = LibNames.CLIENT_PROXY, serverSide = LibNames.SERVER_PROXY)
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        CrashReportHandler.onServerStart(event);
    }
}
