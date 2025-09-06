package dev.mark.loader.service;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.spongepowered.asm.mixin.Mixins;
import dev.mark.loader.url.byteStreamHandler;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ConnectionService {
    private static final ConcurrentHashMap<String, byte[]> classes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, byte[]> raw = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, byte[]> resources = new ConcurrentHashMap<>();

    private static volatile boolean archiveLoaded = false;

    public static void download() {
        if (archiveLoaded) return;
        synchronized (ConnectionService.class) {
            if (archiveLoaded) return;

            try (Socket socket = new Socket("127.0.0.1", 9999);
                 DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                output.writeUTF("HELLO");
                output.flush();

                processArchive(input);
                archiveLoaded = true;
            } catch (IOException e) {
                throw new RuntimeException("Failed to download archive", e);
            }
        }
    }

    private static void processArchive(DataInputStream input) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                byte[] data = readFully(zip);

                if (name.endsWith(".class")) {
                    String className = name.replace('/', '.').replace(".class", "");
                    classes.put(className, data);
                    raw.put("/" + name, data);
                } else {
                    resources.put(name, data);
                    if (!name.startsWith("/")) {
                        resources.put("/" + name, data);
                    }
                }
            }
        }
    }

    public static void setupMixins() {
        download();
        try {
            Mixins.addConfiguration("test-mod.mixins.json");
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to setup mixins", e);
        }
    }

    public static void loadClasses() {
        download();
        try {
            ClassLoader loader = FabricLauncherBase.getLauncher().getTargetClassLoader();
            addToClasspath(loader);
            initializeMain();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load classes", e);
        }
    }

    private static void addToClasspath(ClassLoader loader) throws Throwable {
        Class<?> clazz = loader.getClass();
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
        MethodHandle handle = lookup.findVirtual(clazz, "addUrlFwd", MethodType.methodType(void.class, URL.class)).bindTo(loader); /// addUrlFwd
        URI uri = URI.create("classloader:/");
        URL url = URL.of(uri, new byteStreamHandler(raw));
        handle.invokeExact(url);
    }

    private static void initializeMain() throws Exception {
        String mainClass = findMainClass();
        if (mainClass == null) {
            System.out.println("Main class not found");
            return;
        }

        ClassLoader loader = FabricLauncherBase.getLauncher().getTargetClassLoader();
        Class<?> clazz = loader.loadClass(mainClass);

        if (!ModInitializer.class.isAssignableFrom(clazz)) {
            System.out.println("Main class doesn't implement ModInitializer: " + mainClass);
            return;
        }

        ModInitializer instance = (ModInitializer) clazz.getDeclaredConstructor().newInstance();
        instance.onInitialize();
        System.out.println("Initialized: " + mainClass);
    }

    private static String findMainClass() {
        return classes.keySet().stream()
                .filter(name -> name.substring(name.lastIndexOf('.') + 1).contains("Main")).findFirst()
                .orElseGet(() -> classes.entrySet().stream().filter
                        (entry -> hasModInitializer(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null));
    }

    private static boolean hasModInitializer(byte[] classBytes) {
        String content = new String(classBytes);
        return content.contains("net/fabricmc/api/ModInitializer") || content.contains("Lnet/fabricmc/api/ModInitializer;");
    }

    @Deprecated
    public static void load() {
        download();
        setupMixins();
        loadClasses();
    }

    private static byte[] readFully(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int bytesRead;

        while ((bytesRead = stream.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        return buffer.toByteArray();
    }

    public static byte[] getClass(String name) {
        return classes.get(name);
    }

    public static byte[] getResource(String name) {
        return resources.get(name);
    }
}