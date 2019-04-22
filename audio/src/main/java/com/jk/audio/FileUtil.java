package com.jk.audio;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by cai.jia on 2015/12/3.
 */
public class FileUtil {

    /**
     * 删除文件夹或文件
     *
     * @param path 要删除的文件或文件夹的路径
     */
    public static void delete(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            for (File f : files) {
                delete(f.getAbsolutePath());
            }
            file.delete();
        }
    }

    /**
     * 复制文件或文件夹
     *
     * @param source 原文件路径
     * @param target 目标文件路径
     */
    public static void copyFile(String source, String target) {
        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(target)) {
            return;
        }

        try {
            File sourceFile = new File(source);
            if (sourceFile.isFile()) {
                FileInputStream inputStream = new FileInputStream(sourceFile);
                File targetFile = new File(target, sourceFile.getName());
                writeFile(targetFile, inputStream);

            } else {
                File dir = new File(target, sourceFile.getName());
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File[] files = sourceFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    copyFile(source + File.separator + files[i].getName(), dir.getAbsolutePath());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将Android Asset目录下的文件拷贝到指定目录
     *
     * @param sourceFileName Asset目录下文件名
     * @param target         指定目录
     */
    public static void copyAssetFile(Context context, String sourceFileName, String target) {
        if (context == null || TextUtils.isEmpty(sourceFileName) || TextUtils.isEmpty(target)) {
            return;
        }

        AssetManager assetManager = context.getAssets();
        try {
            String[] paths = assetManager.list(sourceFileName);
            if (paths.length > 0) {
                //目录
                File dir = new File(target, sourceFileName);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                for (String path : paths) {
                    copyAssetFile(context, sourceFileName + File.separator + path, dir.getAbsolutePath());
                }
            } else {
                //文件
                InputStream inputStream = assetManager.open(sourceFileName);
                File targetFile = new File(target, getFileName(sourceFileName));
                writeFile(targetFile, inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件转成字符串
     *
     * @param file 文本文件
     * @return
     */
    public static String fileToString(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            return streamToString(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 流转字符串
     *
     * @param inputStream 输入流
     * @return
     */
    public static String streamToString(InputStream inputStream) {
        return new String(streamToBytes(inputStream));
    }

    /**
     * 流转成字节数组
     *
     * @param inputStream 输入流
     * @return
     */
    public static byte[] streamToBytes(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 8];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                inputStream.close();
            } catch (Exception e) {

            }
        }
        return out.toByteArray();
    }

    /**
     * 得到文件名
     *
     * @param path
     */
    private static String getFileName(String path) {
        if (!path.contains(File.separator)) {
            return path;
        }

        return path.substring(path.lastIndexOf(File.separator) + 1);
    }

    private static void writeFile(File file, InputStream zipInStream) {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(zipInStream);
            out = new BufferedOutputStream(new FileOutputStream(file));
            int len;
            byte[] buffer = new byte[1024 * 8];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 是否挂载sd卡
     *
     * @return
     */
    public static boolean isMountSdCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) &&
                !Environment.isExternalStorageRemovable();
    }

    /**
     * 得到缓存路径
     *
     * @param context
     * @return
     */
    public static String getCachePath(Context context) {
        if (isMountSdCard()) {
            File file = context.getExternalCacheDir();
            if (file != null) {
                return file.getAbsolutePath();
            }
        }
        return context.getCacheDir().getAbsolutePath();
    }

    public static Uri createFileCachePath(Context context) {
        return Uri.fromFile(getDiskCacheFile(context, System.currentTimeMillis() + ".jpg"));
    }

    public static File getSdcardFile(String dir) {
        boolean hasSdcard = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable();
        if (hasSdcard) {
            File dirFile = new File(Environment.getExternalStorageDirectory(),dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            return dirFile;
        }
        return null;
    }

    public static File getSdcardFile(String dir, String fileName) {
        boolean hasSdcard = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable();
        if (hasSdcard) {
            File dirFile = new File(Environment.getExternalStorageDirectory(),dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File file =  new File(dirFile, fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return file;
        }
        return null;
    }

    public static File getDiskCacheFile(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        File file = new File(cachePath + File.separator + uniqueName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static File getDiskCacheDir(Context context, String dir) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        File dirFile = new File(cachePath + File.separator + dir);
        dirFile.mkdirs();
        return dirFile;
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static void writeSdcard(Context context, String fileName,String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        File file = getDiskCacheFile(context, fileName);
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new ByteArrayInputStream(content.getBytes()));
            out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
