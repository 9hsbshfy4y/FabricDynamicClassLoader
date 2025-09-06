package dev.mark.loader.service;

import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot;
import org.spongepowered.asm.service.IMixinService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class MixinService extends MixinServiceKnot {
    @Override
    public byte[] getClassBytes(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        String className = name.replace("/", ".");
        byte[] classBytes = ConnectionService.getClass(className);

        if (classBytes != null && isMixin(classBytes)) {
            return classBytes;
        }

        return super.getClassBytes(name, runTransformers);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        byte[] data = ConnectionService.getResource(name);
        if (data != null) {
            return new ByteArrayInputStream(data);
        }
        return super.getResourceAsStream(name);
    }

    private boolean isMixin(byte[] classBytes) {
        String content = new String(classBytes);
        return content.contains("Lorg/spongepowered/asm/mixin/Mixin;") || content.contains("org/spongepowered/asm/mixin/Mixin");
    }

    public static void replace(IMixinService service) throws Exception {
        Field instanceField = org.spongepowered.asm.service.MixinService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        Object mixinInstance = instanceField.get(null);

        Field serviceField = mixinInstance.getClass().getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(mixinInstance, service);
    }
}