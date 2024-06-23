package gcg;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import net.minecraft.launchwrapper.IClassTransformer;

import static org.objectweb.asm.Opcodes.*;

public class ClassTransformer implements IClassTransformer {
	final String asmHandler = "gcg/AsmHandler";

	public static int transformations = 0;

	public ClassTransformer() {
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (transformedName.equals("micdoodle8.mods.galacticraft.core.GalacticraftCore")) {
			transformations++;
			return transformGC(basicClass);
		}

		return basicClass;
	}

	private byte[] transformGC(byte[] basicClass) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);

		MethodNode init = null;

		for (MethodNode mn : classNode.methods) {
			if (mn.name.equals("init")) {
				init = mn;
			}
		}

		if (init != null) {
			AbstractInsnNode ain = init.instructions.get(0);

			InsnList toInsert = new InsnList();
			toInsert.add(new VarInsnNode(ALOAD, 1));
			toInsert.add(new MethodInsnNode(INVOKESTATIC, asmHandler, "patchGC", "(Lnet/minecraftforge/fml/common/event/FMLInitializationEvent;)V", false));
			toInsert.add(new InsnNode(RETURN));
			init.instructions.insertBefore(ain, toInsert);

		}

		CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);

		return writer.toByteArray();
	}
}
