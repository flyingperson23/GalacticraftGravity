package gcg;

import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.client.IGameScreen;
import micdoodle8.mods.galacticraft.api.galaxies.*;
import micdoodle8.mods.galacticraft.api.item.EnumExtendedInventorySlot;
import micdoodle8.mods.galacticraft.api.recipe.SchematicRegistry;
import micdoodle8.mods.galacticraft.api.world.AtmosphereInfo;
import micdoodle8.mods.galacticraft.api.world.EnumAtmosphericGas;
import micdoodle8.mods.galacticraft.core.*;
import micdoodle8.mods.galacticraft.core.advancement.GCTriggers;
import micdoodle8.mods.galacticraft.core.client.screen.GameScreenBasic;
import micdoodle8.mods.galacticraft.core.client.screen.GameScreenCelestial;
import micdoodle8.mods.galacticraft.core.datafix.GCCoreDataFixers;
import micdoodle8.mods.galacticraft.core.dimension.*;
import micdoodle8.mods.galacticraft.core.energy.EnergyConfigHandler;
import micdoodle8.mods.galacticraft.core.energy.grid.ChunkPowerHandler;
import micdoodle8.mods.galacticraft.core.energy.tile.TileCableIC2Sealed;
import micdoodle8.mods.galacticraft.core.entities.*;
import micdoodle8.mods.galacticraft.core.event.LootHandlerGC;
import micdoodle8.mods.galacticraft.core.network.ConnectionEvents;
import micdoodle8.mods.galacticraft.core.network.GalacticraftChannelHandler;
import micdoodle8.mods.galacticraft.core.schematic.SchematicAdd;
import micdoodle8.mods.galacticraft.core.schematic.SchematicMoonBuggy;
import micdoodle8.mods.galacticraft.core.schematic.SchematicRocketT1;
import micdoodle8.mods.galacticraft.core.tile.*;
import micdoodle8.mods.galacticraft.core.util.*;
import micdoodle8.mods.galacticraft.core.world.ChunkLoadingCallback;
import micdoodle8.mods.galacticraft.core.world.gen.OverworldGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class AsmHandler {
	public static void patchGC(FMLInitializationEvent event) {
		GalacticraftCore.galacticraftBlocksTab.setItemForTab(new ItemStack(Item.getItemFromBlock(GCBlocks.machineBase2)));
		GalacticraftCore.galacticraftItemsTab.setItemForTab(new ItemStack(GCItems.rocketTier1));

		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
		{
			GCBlocks.finalizeSort();
			GCItems.finalizeSort();
		}

		GalacticraftCore.proxy.init(event);

		GalacticraftCore.packetPipeline = GalacticraftChannelHandler.init();

		Star starSol = (Star) new Star("sol").setParentSolarSystem(GalacticraftCore.solarSystemSol).setTierRequired(-1);
		starSol.setBodyIcon(new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/sun.png"));
		GalacticraftCore.solarSystemSol.setMainStar(starSol);

		GalacticraftCore.planetOverworld.setBodyIcon(new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/earth.png"));
		GalacticraftCore.planetOverworld.setDimensionInfo(ConfigManagerCore.idDimensionOverworld, WorldProvider.class, false).setTierRequired(1);
		GalacticraftCore.planetOverworld.atmosphereComponent(EnumAtmosphericGas.NITROGEN).atmosphereComponent(EnumAtmosphericGas.OXYGEN).atmosphereComponent(EnumAtmosphericGas.ARGON).atmosphereComponent(EnumAtmosphericGas.WATER);
		GalacticraftCore.planetOverworld.addChecklistKeys("equip_parachute");

		GalacticraftCore.moonMoon.setDimensionInfo(ConfigManagerCore.idDimensionMoon, WorldProviderMoon.class).setTierRequired(1);
		GalacticraftCore.moonMoon.setBodyIcon(new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/moon.png"));
		GalacticraftCore.moonMoon.setAtmosphere(new AtmosphereInfo(false, false, false, 0.0F, 0.0F, 0.0F));
		GalacticraftCore.moonMoon.addMobInfo(new SpawnListEntry(EntityEvolvedZombie.class, 8, 2, 3));
		GalacticraftCore.moonMoon.addMobInfo(new SpawnListEntry(EntityEvolvedSpider.class, 8, 2, 3));
		GalacticraftCore.moonMoon.addMobInfo(new SpawnListEntry(EntityEvolvedSkeleton.class, 8, 2, 3));
		GalacticraftCore.moonMoon.addMobInfo(new SpawnListEntry(EntityEvolvedCreeper.class, 8, 2, 3));
		GalacticraftCore.moonMoon.addMobInfo(new SpawnListEntry(EntityEvolvedEnderman.class, 10, 1, 4));
		GalacticraftCore.moonMoon.addChecklistKeys("equip_oxygen_suit");

		//Satellites must always have a WorldProvider implementing IOrbitDimension
		GalacticraftCore.satelliteSpaceStation.setDimensionInfo(ConfigManagerCore.idDimensionOverworldOrbit, ConfigManagerCore.idDimensionOverworldOrbitStatic, WorldProviderOverworldOrbitG.class).setTierRequired(1);
		GalacticraftCore.satelliteSpaceStation.setBodyIcon(new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/space_station.png"));
		GalacticraftCore.satelliteSpaceStation.setAtmosphere(new AtmosphereInfo(false, false, false, 0.0F, 0.1F, 0.02F));
		GalacticraftCore.satelliteSpaceStation.addChecklistKeys("equip_oxygen_suit", "create_grapple");

		ForgeChunkManager.setForcedChunkLoadingCallback(GalacticraftCore.instance, new ChunkLoadingCallback());
		MinecraftForge.EVENT_BUS.register(new ConnectionEvents());

		SchematicRegistry.registerSchematicRecipe(new SchematicRocketT1());
		SchematicRegistry.registerSchematicRecipe(new SchematicMoonBuggy());
		SchematicRegistry.registerSchematicRecipe(new SchematicAdd());
		ChunkPowerHandler.initiate();
		EnergyConfigHandler.initGas();
		LootHandlerGC.registerAll();

		registerCreatures();
		registerOtherEntities();
		registerTileEntities();

		GalaxyRegistry.registerSolarSystem(GalacticraftCore.solarSystemSol);
		GalaxyRegistry.registerPlanet(GalacticraftCore.planetOverworld);
		GalaxyRegistry.registerMoon(GalacticraftCore.moonMoon);
		GalaxyRegistry.registerSatellite(GalacticraftCore.satelliteSpaceStation);
		GCDimensions.ORBIT = GalacticraftRegistry.registerDimension("Space Station", "_orbit", ConfigManagerCore.idDimensionOverworldOrbit, WorldProviderOverworldOrbitG.class, false);
		if (GCDimensions.ORBIT == null)
		{
			GCLog.severe("Failed to register space station dimension type with ID " + ConfigManagerCore.idDimensionOverworldOrbit);
		}
		GCDimensions.ORBIT_KEEPLOADED = GalacticraftRegistry.registerDimension("Space Station", "_orbit", ConfigManagerCore.idDimensionOverworldOrbitStatic, WorldProviderOverworldOrbitG.class, true);
		if (GCDimensions.ORBIT_KEEPLOADED == null)
		{
			GCLog.severe("Failed to register space station dimension type with ID " + ConfigManagerCore.idDimensionOverworldOrbitStatic);
		}
		GalacticraftRegistry.registerTeleportType(WorldProviderSurface.class, new TeleportTypeOverworld());
		GalacticraftRegistry.registerTeleportType(WorldProviderOverworldOrbitG.class, new TeleportTypeOrbit());
		GalacticraftRegistry.registerTeleportType(WorldProviderMoon.class, new TeleportTypeMoon());
		GalacticraftRegistry.registerRocketGui(WorldProviderOverworldOrbitG.class, new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/overworld_rocket_gui.png"));
		GalacticraftRegistry.registerRocketGui(WorldProviderSurface.class, new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/overworld_rocket_gui.png"));
		GalacticraftRegistry.registerRocketGui(WorldProviderMoon.class, new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/moon_rocket_gui.png"));
		GalacticraftRegistry.addDungeonLoot(1, new ItemStack(GCItems.schematic, 1, 0));
		GalacticraftRegistry.addDungeonLoot(1, new ItemStack(GCItems.schematic, 1, 1));

		if (ConfigManagerCore.enableCopperOreGen)
		{
			GameRegistry.registerWorldGenerator(new OverworldGenerator(GCBlocks.basicBlock, 5, 24, 0, 75, 7), 4);
		}

		if (ConfigManagerCore.enableTinOreGen)
		{
			GameRegistry.registerWorldGenerator(new OverworldGenerator(GCBlocks.basicBlock, 6, 22, 0, 60, 7), 4);
		}

		if (ConfigManagerCore.enableAluminumOreGen)
		{
			GameRegistry.registerWorldGenerator(new OverworldGenerator(GCBlocks.basicBlock, 7, 18, 0, 45, 7), 4);
		}

		if (ConfigManagerCore.enableSiliconOreGen)
		{
			GameRegistry.registerWorldGenerator(new OverworldGenerator(GCBlocks.basicBlock, 8, 3, 0, 25, 7), 4);
		}

		FMLInterModComms.sendMessage("OpenBlocks", "donateUrl", "http://www.patreon.com/micdoodle8");
		registerCoreGameScreens();

		GCFluids.registerLegacyFluids();
		GCFluids.registerDispenserBehaviours();
		if (CompatibilityManager.isBCraftEnergyLoaded()) GCFluids.registerBCFuel();

		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_MASK, EnumExtendedInventorySlot.MASK, GCItems.oxMask);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_GEAR, EnumExtendedInventorySlot.GEAR, GCItems.oxygenGear);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_TANK_LIGHT, EnumExtendedInventorySlot.LEFT_TANK, GCItems.oxTankLight);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_TANK_LIGHT, EnumExtendedInventorySlot.RIGHT_TANK, GCItems.oxTankLight);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_TANK_MEDIUM, EnumExtendedInventorySlot.LEFT_TANK, GCItems.oxTankMedium);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_TANK_MEDIUM, EnumExtendedInventorySlot.RIGHT_TANK, GCItems.oxTankMedium);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_TANK_HEAVY, EnumExtendedInventorySlot.LEFT_TANK, GCItems.oxTankHeavy);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_TANK_HEAVY, EnumExtendedInventorySlot.RIGHT_TANK, GCItems.oxTankHeavy);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_TANK_INFINITE, EnumExtendedInventorySlot.LEFT_TANK, GCItems.oxygenCanisterInfinite);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_OXYGEN_TANK_INFINITE, EnumExtendedInventorySlot.RIGHT_TANK, GCItems.oxygenCanisterInfinite);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_PARACHUTE, EnumExtendedInventorySlot.PARACHUTE, GCItems.parachute);
		GalacticraftRegistry.registerGear(Constants.GEAR_ID_FREQUENCY_MODULE, EnumExtendedInventorySlot.FREQUENCY_MODULE, new ItemStack(GCItems.basicItem, 1, 19));

		GalacticraftCore.proxy.registerFluidTexture(GCFluids.fluidOil, new ResourceLocation(Constants.ASSET_PREFIX, "textures/misc/underoil.png"));
		GalacticraftCore.proxy.registerFluidTexture(GCFluids.fluidFuel, new ResourceLocation(Constants.ASSET_PREFIX, "textures/misc/underfuel.png"));

		PermissionAPI.registerNode(Constants.PERMISSION_CREATE_STATION, DefaultPermissionLevel.ALL, "Allows players to create space stations");

		GCTriggers.registerTriggers();
	}

	private static void registerCoreGameScreens()
	{
		if (GCCoreUtil.getEffectiveSide() == Side.CLIENT)
		{
			IGameScreen rendererBasic = new GameScreenBasic();
			IGameScreen rendererCelest = new GameScreenCelestial();
			GalacticraftRegistry.registerScreen(rendererBasic);  //Type 0 - blank
			GalacticraftRegistry.registerScreen(rendererBasic);  //Type 1 - local satellite view
			GalacticraftRegistry.registerScreen(rendererCelest);  //Type 2 - solar system
			GalacticraftRegistry.registerScreen(rendererCelest);  //Type 3 - local planet
			GalacticraftRegistry.registerScreen(rendererCelest);  //Type 4 - render test
		}
		else
		{
			GalacticraftRegistry.registerScreensServer(5);
		}
	}

	private static void register(Class<? extends TileEntity> tileEntityClass, String key) {
		GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation("galacticraftcore", key));
	}

	private static void registerTileEntities()
	{
		register(TileEntityTreasureChest.class, "gc_treasure_chest");
		register(TileEntityOxygenDistributor.class, "gc_air_distributor");
		register(TileEntityOxygenCollector.class, "gc_air_collector");
		register(TileEntityFluidPipe.class, "gc_oxygen_pipe");
		register(TileEntityAirLock.class, "gc_air_lock_frame");
		register(TileEntityRefinery.class, "gc_refinery");
		register(TileEntityNasaWorkbench.class, "gc_nasa_workbench");
		register(TileEntityCompactNasaWorkbench.class, "gc_nasa_workbench_compact");
		register(TileEntityDeconstructor.class, "gc_deconstructor");
		register(TileEntityOxygenCompressor.class, "gc_air_compressor");
		register(TileEntityFuelLoader.class, "gc_fuel_loader");
		register(TileEntityLandingPadSingle.class, "gc_landing_pad");
		register(TileEntityLandingPad.class, "gc_landing_pad_full");
		register(TileEntitySpaceStationBase.class, "gc_space_station");
		register(TileEntityMulti.class, "gc_dummy_block");
		register(TileEntityOxygenSealer.class, "gc_air_sealer");
		register(TileEntityDungeonSpawner.class, "gc_dungeon_boss_spawner");
		register(TileEntityOxygenDetector.class, "gc_oxygen_detector");
		register(TileEntityBuggyFueler.class, "gc_buggy_fueler");
		register(TileEntityBuggyFuelerSingle.class, "gc_buggy_fueler_single");
		register(TileEntityCargoLoader.class, "gc_cargo_loader");
		register(TileEntityCargoUnloader.class, "gc_cargo_unloader");
		register(TileEntityParaChest.class, "gc_parachest_tile");
		register(TileEntitySolar.class, "gc_solar_panel");
		register(TileEntityDish.class, "gc_radio_telescope");
		register(TileEntityCrafting.class, "gc_magnetic_crafting_table");
		register(TileEntityEnergyStorageModule.class, "gc_energy_storage_module");
		register(TileEntityCoalGenerator.class, "gc_coal_generator");
		register(TileEntityElectricFurnace.class, "gc_electric_furnace");
		register(TileEntityAluminumWire.class, "gc_aluminum_wire");
		register(TileEntityAluminumWireSwitch.class, "gc_switchable_aluminum_wire");
		register(TileEntityFallenMeteor.class, "gc_fallen_meteor");
		register(TileEntityIngotCompressor.class, "gc_ingot_compressor");
		register(TileEntityElectricIngotCompressor.class, "gc_electric_ingot_compressor");
		register(TileEntityCircuitFabricator.class, "gc_circuit_fabricator");
		register(TileEntityAirLockController.class, "gc_air_lock_controller");
		register(TileEntityOxygenStorageModule.class, "gc_oxygen_storage_module");
		register(TileEntityOxygenDecompressor.class, "gc_oxygen_decompressor");
		register(TileEntityThruster.class, "gc_space_station_thruster");
		register(TileEntityArclamp.class, "gc_arc_lamp");
		register(TileEntityScreen.class, "gc_view_screen");
		register(TileEntityPanelLight.class, "gc_panel_lighting");
		register(TileEntityTelemetry.class, "gc_telemetry_unit");
		register(TileEntityPainter.class, "gc_painter");
		register(TileEntityFluidTank.class, "gc_fluid_tank");
		register(TileEntityPlayerDetector.class, "gc_player_detector");
		register(TileEntityPlatform.class, "gc_platform");
		register(TileEntityEmergencyBox.class, "gc_emergency_post");
		register(TileEntityNull.class, "gc_null_tile");
		if (CompatibilityManager.isIc2Loaded())
		{
			register(TileCableIC2Sealed.class, "gc_sealed_ic2_cable");
		}

		new GCCoreDataFixers().registerAll();
	}

	private static void registerCreatures()
	{
		GCCoreUtil.registerGalacticraftCreature(EntityEvolvedSpider.class, "evolved_spider", 3419431, 11013646);
		GCCoreUtil.registerGalacticraftCreature(EntityEvolvedZombie.class, "evolved_zombie", 44975, 7969893);
		GCCoreUtil.registerGalacticraftCreature(EntityEvolvedCreeper.class, "evolved_creeper", 894731, 0);
		GCCoreUtil.registerGalacticraftCreature(EntityEvolvedSkeleton.class, "evolved_skeleton", 12698049, 4802889);
		GCCoreUtil.registerGalacticraftCreature(EntitySkeletonBoss.class, "evolved_skeleton_boss", 12698049, 4802889);
		GCCoreUtil.registerGalacticraftCreature(EntityAlienVillager.class, "alien_villager", ColorUtil.to32BitColor(255, 103, 145, 181), 12422002);
		GCCoreUtil.registerGalacticraftCreature(EntityEvolvedEnderman.class, "evolved_enderman", 1447446, 0);
		GCCoreUtil.registerGalacticraftCreature(EntityEvolvedWitch.class, "evolved_witch", 3407872, 5349438);
	}

	private static void registerOtherEntities()
	{
		GCCoreUtil.registerGalacticraftNonMobEntity(EntityTier1Rocket.class, "rocket_t1", 150, 1, false);
		GCCoreUtil.registerGalacticraftNonMobEntity(EntityMeteor.class, "meteor", 150, 5, true);
		GCCoreUtil.registerGalacticraftNonMobEntity(EntityBuggy.class, "buggy", 150, 5, true);
		GCCoreUtil.registerGalacticraftNonMobEntity(EntityFlag.class, "gcflag", 150, 5, true);
		GCCoreUtil.registerGalacticraftNonMobEntity(EntityParachest.class, "para_chest", 150, 5, true);
		GCCoreUtil.registerGalacticraftNonMobEntity(EntityLander.class, "lander", 150, 5, false);
		GCCoreUtil.registerGalacticraftNonMobEntity(EntityMeteorChunk.class, "meteor_chunk", 150, 5, true);
		GCCoreUtil.registerGalacticraftNonMobEntity(EntityCelestialFake.class, "celestial_screen", 150, 5, false);
		GCCoreUtil.registerGalacticraftNonMobEntity(EntityHangingSchematic.class, "hanging_schematic", 150, 5, false);
	}
}
