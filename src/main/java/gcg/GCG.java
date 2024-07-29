package gcg;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "gcg", name = "Galacticraft Gravity", version = "1.1", dependencies = "required: galacticraftcore")
public class GCG {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e){
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e){
    }
}

