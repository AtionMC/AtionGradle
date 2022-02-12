/**
 * Copyright (C) 2022 Enaium
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ationmc.ationgradle.gradle.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.ationmc.ationgradle.gradle.AtionGradleExtension;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Enaium
 */
public class ClientUtil {
    public static File getMinecraftDir() {
        File minecraftFolder;
        if (getOsName().contains("win")) {
            minecraftFolder = new File(System.getenv("APPDATA"), File.separator + ".minecraft");
        } else if (getOsName().contains("mac")) {
            minecraftFolder = new File(System.getProperty("user.home"), File.separator + "Library" + File.separator + "Application Support" + File.separator + "minecraft");
        } else {
            minecraftFolder = new File(System.getProperty("user.home"), File.separator + ".minecraft");
        }
        return minecraftFolder;
    }

    private static String getOsName() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT);
    }

    public static String getJson(AtionGradleExtension extension) {
        String jsonUrl = "";
        for (JsonElement jsonElement : new Gson().fromJson(DownloadUtil.readString(extension.minecraft.manifest), JsonObject.class).get("versions").getAsJsonArray()) {
            if (jsonElement.getAsJsonObject().get("id").getAsString().equals(extension.minecraft.version)) {
                jsonUrl = jsonElement.getAsJsonObject().get("url").getAsString();
            }
        }
        return DownloadUtil.readString(jsonUrl);
    }


    public static List<String> getLibraries(AtionGradleExtension extension) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        for (JsonElement jsonElement : new Gson().fromJson(getJson(extension), JsonObject.class).get("libraries").getAsJsonArray()) {
            if (jsonElement.getAsJsonObject().has("natives")) {
                continue;
            }
            String name = jsonElement.getAsJsonObject().get("name").getAsString();
            list.put(name.substring(0, name.lastIndexOf(":")), name.substring(name.lastIndexOf(":")));
        }
        List<String> libraries = new ArrayList<>();
        for (Map.Entry<String, String> entry : list.entrySet()) {
            libraries.add(entry.getKey() + entry.getValue());
        }
        return libraries;
    }

    public static List<String> getNatives(AtionGradleExtension extension) {
        List<String> libraries = new ArrayList<>();

        for (JsonElement jsonElement : new Gson().fromJson(getJson(extension), JsonObject.class).get("libraries").getAsJsonArray()) {
            JsonObject downloads = jsonElement.getAsJsonObject().get("downloads").getAsJsonObject();
            if (downloads.has("classifiers")) {
                String name = "natives-linux";
                if (getOsName().contains("win")) {
                    name = "natives-windows";
                } else if (getOsName().contains("mac")) {
                    name = "natives-macos";
                }
                JsonObject classifiers = downloads.get("classifiers").getAsJsonObject();
                if (classifiers.has(name)) {
                    libraries.add(downloads.get("classifiers").getAsJsonObject().get(name).getAsJsonObject().get("url").getAsString());
                }
            }
        }
        return libraries;
    }

    public static File getLocalJar(String version) {
        return new File(getMinecraftDir(), "versions" + File.separator + version + File.separator + version + ".jar");
    }

    public static File getClientNativeDir(AtionGradleExtension extension) {
        File file = new File(getGameDir(extension), extension.minecraft.version + File.separator + extension.minecraft.version + "-native");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static File getNativeJarDir(AtionGradleExtension extension) {
        File nativeJarDir = new File(ClientUtil.getClientNativeDir(extension), "jars");
        if (!nativeJarDir.exists()) {
            nativeJarDir.mkdir();
        }
        return nativeJarDir;
    }

    public static File getNativeFileDir(AtionGradleExtension extension) {
        File nativeFileDir = new File(ClientUtil.getClientNativeDir(extension), "natives");
        if (!nativeFileDir.exists()) {
            nativeFileDir.mkdir();
        }
        return nativeFileDir;
    }

    public static File getClientFile(AtionGradleExtension extension) {
        return new File(getGameDir(extension), extension.minecraft.version + File.separator + extension.minecraft.version + "-client.jar");
    }

    public static File getServerFile(AtionGradleExtension extension) {
        return new File(getGameDir(extension), extension.minecraft.version + File.separator + extension.minecraft.version + "-server.jar");
    }

    public static File getClientCleanFile(AtionGradleExtension extension) {
        return new File(getGameDir(extension), extension.minecraft.version + File.separator + extension.minecraft.version + "-client-clean.jar");
    }

    public static File getServerCleanFile(AtionGradleExtension extension) {
        return new File(getGameDir(extension), extension.minecraft.version + File.separator + extension.minecraft.version + "-server-clean.jar");
    }

    public static File getClientCleanSourceFile(AtionGradleExtension extension) {
        return new File(getGameDir(extension), extension.minecraft.version + File.separator + extension.minecraft.version + "-client-clean-source.jar");
    }

    public static File getClientServerSourceFile(AtionGradleExtension extension) {
        return new File(getGameDir(extension), extension.minecraft.version + File.separator + extension.minecraft.version + "-client-server-source.jar");
    }

    public static File getMappingDir(AtionGradleExtension extension) {
        File mapping = new File(extension.getUserCache(), "mapping");
        if (!mapping.exists()) {
            mapping.mkdir();
        }
        return mapping;
    }

    public static File getClientMappingFile(AtionGradleExtension extension) {
        File file = new File(getMappingDir(extension), extension.minecraft.version + "-client.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static File getServerMappingFile(AtionGradleExtension extension) {
        File file = new File(getMappingDir(extension), extension.minecraft.version + "-server.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    // game and minecraft
    public static File getGameDir(AtionGradleExtension extension) {
        File game = new File(extension.getUserCache(), "minecraft");
        if (!game.exists()) {
            game.mkdir();
        }
        return game;
    }

    public static JsonObject getDownloadsJson(AtionGradleExtension extension) {
        return new Gson().fromJson(getJson(extension), JsonObject.class).get("downloads").getAsJsonObject();
    }

    public static String getClientJarSha1(AtionGradleExtension extension) {
        return getDownloadsJson(extension).getAsJsonObject().get("client").getAsJsonObject().get("sha1").getAsString();
    }

    public static String getServerJarSha1(AtionGradleExtension extension) {
        return getDownloadsJson(extension).getAsJsonObject().get("server").getAsJsonObject().get("sha1").getAsString();
    }

    public static byte[] getClientJar(AtionGradleExtension extension) {
        return DownloadUtil.readFile(getDownloadsJson(extension).getAsJsonObject().get("client").getAsJsonObject().get("url").getAsString());
    }

    public static byte[] getServerJar(AtionGradleExtension extension) {
        return DownloadUtil.readFile(getDownloadsJson(extension).getAsJsonObject().get("server").getAsJsonObject().get("url").getAsString());
    }

    public static String getClientMapping(AtionGradleExtension extension) {
        return DownloadUtil.readString(getDownloadsJson(extension).getAsJsonObject().get("client_mappings").getAsJsonObject().get("url").getAsString());
    }

    public static String getServerMapping(AtionGradleExtension extension) {
        return DownloadUtil.readString(getDownloadsJson(extension).getAsJsonObject().get("server_mappings").getAsJsonObject().get("url").getAsString());
    }

    public static String getClientMappingSha1(AtionGradleExtension extension) {
        return getDownloadsJson(extension).getAsJsonObject().get("client_mappings").getAsJsonObject().get("sha1").getAsString();
    }

    public static String getServerMappingSha1(AtionGradleExtension extension) {
        return getDownloadsJson(extension).getAsJsonObject().get("server_mappings").getAsJsonObject().get("sha1").getAsString();
    }

    public static File getClientAssetDir(AtionGradleExtension extension) {
        File assets = new File(extension.getUserCache(), "assets");
        if (!assets.exists()) {
            assets.mkdir();
        }
        return assets;
    }

    public static File getClientIndexDir(AtionGradleExtension extension) {
        File index = new File(getClientAssetDir(extension), "indexes");
        if (!index.exists()) {
            index.mkdir();
        }
        return index;
    }

    public static File getClientIndexFile(AtionGradleExtension extension) {
        File file = new File(ClientUtil.getClientIndexDir(extension), extension.minecraft.version + ".json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static File getClientObjectFile(AtionGradleExtension extension, String name) {
        File file = new File(ClientUtil.getClientObjectDir(extension), name.substring(0, 2));
        if (!file.exists()) {
            file.mkdir();
        }
        return new File(file, name);
    }

    public static File getLocalClientObjectFile(String name) {
        File file = new File(ClientUtil.getLocalClientObjectDir(), name.substring(0, 2));
        if (!file.exists()) {
            file.mkdir();
        }
        return new File(file, name);
    }


    public static File getClientObjectDir(AtionGradleExtension extension) {
        File index = new File(getClientAssetDir(extension), "objects");
        if (!index.exists()) {
            index.mkdir();
        }
        return index;
    }

    public static File getLocalClientObjectDir() {
        return new File(getMinecraftDir(), "assets" + File.separator + "objects");
    }

    public static File getClientSkinDir(AtionGradleExtension extension) {
        File index = new File(getClientAssetDir(extension), "skins");
        if (!index.exists()) {
            index.mkdir();
        }
        return index;
    }

    public static String getClientAsset(AtionGradleExtension extension) {
        return DownloadUtil.readString(new Gson().fromJson(getJson(extension), JsonObject.class).get("assetIndex").getAsJsonObject().get("url").getAsString());
    }

    public static String getClientAssetSha1(AtionGradleExtension extension) {
        return new Gson().fromJson(getJson(extension), JsonObject.class).get("assetIndex").getAsJsonObject().get("sha1").getAsString();
    }

    public static boolean fileVerify(File file, String sha1) {
        if (!file.exists()) {
            return false;
        }
        try {
            return DigestUtils.sha1Hex(FileUtils.readFileToByteArray(file)).toLowerCase(Locale.ROOT).equals(sha1.toLowerCase(Locale.ROOT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
