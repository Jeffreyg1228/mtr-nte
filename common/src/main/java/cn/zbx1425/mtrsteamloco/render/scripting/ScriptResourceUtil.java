package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.BuildConfig;
import cn.zbx1425.mtrsteamloco.mixin.ClientCacheAccessor;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mtr.client.ClientData;
import mtr.mappings.Utilities;
import mtr.mappings.UtilitiesClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
#if MC_VERSION >= "11903"
import net.minecraft.core.registries.BuiltInRegistries;
#else
import net.minecraft.core.Registry;
#endif
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

@SuppressWarnings("unused")
public class ScriptResourceUtil {

    protected static List<Map.Entry<ResourceLocation, String>> scriptsToExecute;
    protected static ResourceLocation relativeBase;

    public static ResourceManager manager() {
        return MtrModelRegistryUtil.resourceManager;
    }

    public static ResourceLocation identifier(String textForm) {
        return new ResourceLocation(textForm);
    }

    public static ResourceLocation idRelative(String textForm) {
        if (relativeBase == null) throw new RuntimeException("Cannot use idRelative in functions.");
        return ResourceUtil.resolveRelativePath(relativeBase, textForm, null);
    }

    public static InputStream readStream(ResourceLocation identifier) throws IOException {
        final List<Resource> resources = UtilitiesClient.getResources(manager(), identifier);
        if (resources.isEmpty()) throw new FileNotFoundException(identifier.toString());
        return Utilities.getInputStream(resources.get(0));
    }

    public static String readString(ResourceLocation identifier) {
        try {
            return ResourceUtil.readResource(manager(), identifier);
        } catch (IOException e) {
            return null;
        }
    }

    public static void includeScript(ResourceLocation identifier) throws IOException {
        scriptsToExecute.add(new AbstractMap.SimpleEntry<>(identifier, ResourceUtil.readResource(manager(), identifier)));
    }

    public static Font getBuiltinFont(boolean supportCjk, boolean serif) {
        ClientCacheAccessor clientCache = (ClientCacheAccessor) ClientData.DATA_CACHE;
        if (clientCache.getFont() == null || clientCache.getFontCjk() == null) {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            try {
                clientCache.setFont(Font.createFont(Font.TRUETYPE_FONT, Utilities.getInputStream(resourceManager.getResource(
                        new ResourceLocation(mtr.MTR.MOD_ID, "font/noto-sans-semibold.ttf")))));
                clientCache.setFontCjk(Font.createFont(Font.TRUETYPE_FONT, Utilities.getInputStream(resourceManager.getResource(
                        new ResourceLocation(mtr.MTR.MOD_ID, "font/noto-serif-cjk-tc-semibold.ttf")))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (serif || supportCjk) ? clientCache.getFontCjk() : clientCache.getFont();
    }

    private static final FontRenderContext FONT_CONTEXT = new FontRenderContext(new AffineTransform(), true, false);

    public static FontRenderContext getFontRenderContext() {
        return FONT_CONTEXT;
    }

    public static BufferedImage readBufferedImage(ResourceLocation identifier) throws IOException {
        try (InputStream is = readStream(identifier)) {
            return ImageIO.read(is);
        }
    }

    public static Font readFont(ResourceLocation identifier) throws IOException, FontFormatException {
        try (InputStream is = readStream(identifier)) {
            return Font.createFont(Font.TRUETYPE_FONT, is);
        }
    }

    public static int getParticleTypeId(ResourceLocation identifier) {
#if MC_VERSION >= "11903"
        Optional<ParticleType<?>> particleType = BuiltInRegistries.PARTICLE_TYPE.getOptional(identifier);
        return particleType.map(BuiltInRegistries.PARTICLE_TYPE::getId).orElse(-1);
#else
        Optional<ParticleType<?>> particleType = Registry.PARTICLE_TYPE.getOptional(identifier);
        return particleType.map(Registry.PARTICLE_TYPE::getId).orElse(-1);
#endif
    }

    public static CompoundTag parseNbtString(String text) throws CommandSyntaxException {
        return TagParser.parseTag(text);
    }

    public static String getMTRVersion() {
        String mtrModVersion;
        try {
            mtrModVersion = (String) mtr.Keys.class.getField("MOD_VERSION").get(null);
        } catch (ReflectiveOperationException ignored) {
            mtrModVersion = "0.0.0-0.0.0";
        }
        return mtrModVersion;
    }

    public static String getNTEVersion() {
        return BuildConfig.MOD_VERSION;
    }

    public static int getNTEVersionInt() {
        int[] components = Arrays.stream(BuildConfig.MOD_VERSION.split("\\+", 2)[0].split("\\.", 3))
                .mapToInt(Integer::parseInt).toArray();
        return components[0] * 10000 + components[1] * 100 + components[2];
    }

    public static int getNTEProtoVersion() {
        return BuildConfig.MOD_PROTOCOL_VERSION;
    }
}
