package gcg;

import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IExitHeight;
import micdoodle8.mods.galacticraft.api.world.IOrbitDimension;
import micdoodle8.mods.galacticraft.api.world.ISolarLevel;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.CloudRenderer;
import micdoodle8.mods.galacticraft.core.client.SkyProviderOrbit;
import micdoodle8.mods.galacticraft.core.dimension.GCDimensions;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderSpaceStation;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.RoomTreasure;
import net.minecraft.block.Block;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class WorldProviderOverworldOrbitG extends WorldProviderSpaceStation implements IOrbitDimension, ISolarLevel, IExitHeight
{
    @Override
    public DimensionType getDimensionType()
    {
        return GCDimensions.ORBIT;
    }

    @Override
    public Vector3 getFogColor()
    {
        return new Vector3(0, 0, 0);
    }

    @Override
    public Vector3 getSkyColor()
    {
        return new Vector3(0, 0, 0);
    }

    @Override
    public boolean hasSunset()
    {
        return false;
    }

    @Override
    public long getDayLength()
    {
        return 24000L;
    }

    @Override
    public boolean isDaytime()
    {
        final float a = this.world.getCelestialAngle(0F);
        //TODO: adjust this according to size of planet below
        return a < 0.42F || a > 0.58F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float par1)
    {
        final float var2 = this.world.getCelestialAngle(par1);
        float var3 = 1.0F - (MathHelper.cos(var2 * Constants.twoPI) * 2.0F + 0.25F);

        if (var3 < 0.0F)
        {
            var3 = 0.0F;
        }

        if (var3 > 1.0F)
        {
            var3 = 1.0F;
        }

        return var3 * var3 * 0.5F + 0.3F;
    }

    @Override
    public boolean isSkyColored()
    {
        return false;
    }

    @Override
    public double getHorizon()
    {
        return 44.0D;
    }

    @Override
    public int getAverageGroundLevel()
    {
        return 64;
    }

    @Override
    public boolean canCoordinateBeSpawn(int var1, int var2)
    {
        return true;
    }

    @Override
    public CelestialBody getCelestialBody()
    {
        return GalacticraftCore.satelliteSpaceStation;
    }

    @Override
    public float getGravity()
    {
        return 0.075F;
    }

    @Override
    public double getMeteorFrequency()
    {
        return 0;
    }

    @Override
    public double getFuelUsageMultiplier()
    {
        return 0.5D;
    }

    @Override
    @Deprecated
    public String getPlanetToOrbit()
    {
        return "overworld";
    }

    @Override
    public int getPlanetIdToOrbit() {
        return 0;
    }

    @Override
    public int getYCoordToTeleportToPlanet()
    {
        return 30;
    }

    @Override
    public String getSaveFolder()
    {
        return "DIM_SPACESTATION" + this.getDimension();
    }

    @Override
    public double getSolarEnergyMultiplier()
    {
        return ConfigManagerCore.spaceStationEnergyScalar;
    }

    @Override
    public double getYCoordinateToTeleport()
    {
        return 750;
    }

    @Override
    public boolean canSpaceshipTierPass(int tier)
    {
        return tier > 0;
    }

    @Override
    public float getFallDamageModifier()
    {
        return 0.4F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setSpinDeltaPerTick(float angle)
    {
        SkyProviderOrbit skyProvider = ((SkyProviderOrbit)this.getSkyRenderer());
        if (skyProvider != null)
            skyProvider.spinDeltaPerTick = angle;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getSkyRotation()
    {
        SkyProviderOrbit skyProvider = ((SkyProviderOrbit)this.getSkyRenderer());
        return skyProvider.spinAngle;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void createSkyProvider()
    {
        this.setSkyRenderer(new SkyProviderOrbit(new ResourceLocation(Constants.ASSET_PREFIX, "textures/gui/celestialbodies/earth.png"), true, true));
        this.setSpinDeltaPerTick(this.getSpinManager().getSpinRate());

        if (this.getCloudRenderer() == null)
            this.setCloudRenderer(new CloudRenderer());
    }

    @Override
    public int getDungeonSpacing()
    {
        return 0;
    }

    @Override
    public ResourceLocation getDungeonChestType()
    {
        return RoomTreasure.MOONCHEST;
    }

    @Override
    public List<Block> getSurfaceBlocks()
    {
        return null;
    }
}