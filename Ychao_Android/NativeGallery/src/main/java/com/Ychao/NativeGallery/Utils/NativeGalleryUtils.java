package com.Ychao.NativeGallery.Utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class NativeGalleryUtils {

    private static final String TAG = "[NativeResUtils]";
    private static String secondaryStoragePath = null;

    public static String getPathFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        } else {
            String selection = null;
            String[] selectionArgs = null;

            try {
                if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
                    String id;
                    String[] split;
                    if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                        id = DocumentsContract.getDocumentId(uri);
                        split = id.split(":");
                        if ("primary".equalsIgnoreCase(split[0])) {
                            return Environment.getExternalStorageDirectory() + File.separator + split[1];
                        }

                        if ("raw".equalsIgnoreCase(split[0])) {
                            return split[1];
                        }

                        return GetSecondaryStoragePathFor(split[1]);
                    }

                    if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                        id = DocumentsContract.getDocumentId(uri);
                        if (id.startsWith("raw:")) {
                            return id.substring(4);
                        }

                        uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                        id = DocumentsContract.getDocumentId(uri);
                        split = id.split(":");
                        String type = split[0];
                        if ("image".equals(type)) {
                            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(type)) {
                            uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(type)) {
                            uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        } else if ("raw".equals(type)) {
                            return split[1];
                        }

                        selection = "_id=?";
                        selectionArgs = new String[]{split[1]};
                    }
                }

                if ("content".equalsIgnoreCase(uri.getScheme())) {
                    String[] projection = new String[]{"_data"};
                    Cursor cursor = null;

                    try {
                        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, (String) null);
                        if (cursor != null) {
                            int column_index = cursor.getColumnIndexOrThrow("_data");
                            if (cursor.moveToFirst()) {
                                String columnValue = cursor.getString(column_index);
                                if (columnValue != null && columnValue.length() > 0) {
                                    String var8 = columnValue;
                                    return var8;
                                }
                            }
                        }
                    } catch (Exception var13) {
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }

                    }
                } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                    return uri.getPath();
                }

                return null;
            } catch (Exception var15) {
                Log.e("Unity", "Exception:", var15);
                return null;
            }
        }
    }

    public static boolean WriteFileToStream(File file, OutputStream out) {
        boolean var3;
        try {
            FileInputStream in = new FileInputStream(file);

            try {
                byte[] buf = new byte[1024];

                int len;
                while((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                return true;
            } finally {
                try {
                    in.close();
                } catch (Exception var24) {
                    Log.e("Unity", "Exception:", var24);
                }

            }
        } catch (Exception var26) {
            Log.e("Unity", "Exception:", var26);
            var3 = false;
        } finally {
            try {
                out.close();
            } catch (Exception var23) {
                Log.e("Unity", "Exception:", var23);
            }

        }

        return var3;
    }

    @SuppressLint("Range")
    public static int GetImageOrientation(Context context, String path) {
        int orientation;
        try {
            ExifInterface exif = new ExifInterface(path);
            orientation = exif.getAttributeInt("Orientation", 0);
            if (orientation != 0) {
                return orientation;
            }
        } catch (Exception var9) {
        }

        Cursor cursor = null;

        byte var4;
        try {
            cursor = context.getContentResolver().query(Uri.fromFile(new File(path)), new String[]{"orientation"}, (String)null, (String[])null, (String)null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }

            orientation = cursor.getInt(cursor.getColumnIndex("orientation"));
            if (orientation == 90) {
                var4 = 6;
                return var4;
            }

            if (orientation == 180) {
                var4 = 3;
                return var4;
            }

            if (orientation != 270) {
                var4 = 1;
                return var4;
            }

            var4 = 8;
        } catch (Exception var10) {
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }

        return var4;
    }

    public static String LoadImageAtPath(Context context, String path, String temporaryFilePath, int maxSize) {
        BitmapFactory.Options metadata = GetImageMetadata(path);
        if (metadata == null) {
            return path;
        } else {
            boolean shouldCreateNewBitmap = false;
            if (metadata.outWidth > maxSize || metadata.outHeight > maxSize) {
                shouldCreateNewBitmap = true;
            }

            if (metadata.outMimeType != null && !metadata.outMimeType.equals("image/jpeg") && !metadata.outMimeType.equals("image/png")) {
                shouldCreateNewBitmap = true;
            }

            int orientation = GetImageOrientation(context, path);
            if (orientation != 1 && orientation != 0) {
                shouldCreateNewBitmap = true;
            }

            if (shouldCreateNewBitmap) {
                Bitmap bitmap = null;
                FileOutputStream out = null;

                try {
                    int sampleSize = 1;
                    int halfHeight = metadata.outHeight / 2;

                    for(int halfWidth = metadata.outWidth / 2; halfHeight / sampleSize >= maxSize || halfWidth / sampleSize >= maxSize; sampleSize *= 2) {
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = sampleSize;
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeFile(path, options);
                    float scaleX = 1.0F;
                    float scaleY = 1.0F;
                    if (bitmap.getWidth() > maxSize) {
                        scaleX = (float)maxSize / (float)bitmap.getWidth();
                    }

                    if (bitmap.getHeight() > maxSize) {
                        scaleY = (float)maxSize / (float)bitmap.getHeight();
                    }

                    float scale = scaleX < scaleY ? scaleX : scaleY;
                    if (scale < 1.0F || orientation != 1 && orientation != 0) {
                        Matrix transformationMatrix = GetImageOrientationCorrectionMatrix(orientation, scale);
                        Bitmap transformedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transformationMatrix, true);
                        if (transformedBitmap != bitmap) {
                            bitmap.recycle();
                            bitmap = transformedBitmap;
                        }
                    }

                    out = new FileOutputStream(temporaryFilePath);
                    if (metadata.outMimeType != null && metadata.outMimeType.equals("image/jpeg")) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    } else {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    }

                    path = temporaryFilePath;
                } catch (Exception var28) {
                    Log.e("Unity", "Exception:", var28);

                    try {
                        File temporaryFile = new File(temporaryFilePath);
                        if (temporaryFile.exists()) {
                            temporaryFile.delete();
                        }
                    } catch (Exception var27) {
                    }
                } finally {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }

                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (Exception var26) {
                    }

                }
            }

            return path;
        }
    }

    public static String GetImageProperties(Context context, String path) {
        BitmapFactory.Options metadata = GetImageMetadata(path);
        if (metadata == null) {
            return "";
        } else {
            int width = metadata.outWidth;
            int height = metadata.outHeight;
            String mimeType = metadata.outMimeType;
            if (mimeType == null) {
                mimeType = "";
            }

            int orientation = GetImageOrientation(context, path);
            byte orientationUnity;
            if (orientation == 0) {
                orientationUnity = -1;
            } else if (orientation == 1) {
                orientationUnity = 0;
            } else if (orientation == 6) {
                orientationUnity = 1;
            } else if (orientation == 3) {
                orientationUnity = 2;
            } else if (orientation == 8) {
                orientationUnity = 3;
            } else if (orientation == 2) {
                orientationUnity = 4;
            } else if (orientation == 5) {
                orientationUnity = 5;
            } else if (orientation == 4) {
                orientationUnity = 6;
            } else if (orientation == 7) {
                orientationUnity = 7;
            } else {
                orientationUnity = -1;
            }

            if (orientation == 6 || orientation == 8 || orientation == 5 || orientation == 7) {
                int temp = width;
                width = height;
                height = temp;
            }

            return width + ">" + height + ">" + mimeType + ">" + orientationUnity;
        }
    }

    @TargetApi(17)
    public static String GetVideoProperties(Context context, String path) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

        String height;
        try {
            metadataRetriever.setDataSource(path);
            String width = metadataRetriever.extractMetadata(18);
            height = metadataRetriever.extractMetadata(19);
            String duration = metadataRetriever.extractMetadata(9);
            String rotation = "0";
            if (Build.VERSION.SDK_INT >= 17) {
                rotation = metadataRetriever.extractMetadata(24);
            }

            if (width == null) {
                width = "0";
            }

            if (height == null) {
                height = "0";
            }

            if (duration == null) {
                duration = "0";
            }

            if (rotation == null) {
                rotation = "0";
            }

            String var7 = width + ">" + height + ">" + duration + ">" + rotation;
            return var7;
        } catch (Exception var11) {
            Log.e("Unity", "Exception:", var11);
            height = "";
        } finally {
            metadataRetriever.release();
        }

        return height;
    }

    @SuppressLint("WrongConstant")
    @TargetApi(29)
    public static String GetVideoThumbnail(Context context, String path, String savePath, boolean saveAsJpeg, int maxSize, double captureTime) {
        Bitmap bitmap = null;
        FileOutputStream out = null;

        String var39;
        try {
            if (captureTime < 0.0D && maxSize <= 1024) {
                try {
                    if (Build.VERSION.SDK_INT < 29) {
                        bitmap = ThumbnailUtils.createVideoThumbnail(path, maxSize > 512 ? 2 : 1);
                    } else {
                        bitmap = ThumbnailUtils.createVideoThumbnail(new File(path), maxSize > 512 ? new Size(1024, 786) : new Size(512, 384), (CancellationSignal)null);
                    }
                } catch (Exception var36) {
                    Log.e("Unity", "Exception:", var36);
                }
            }

            if (bitmap == null) {
                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();

                try {
                    metadataRetriever.setDataSource(path);

                    try {
                        int width = Integer.parseInt(metadataRetriever.extractMetadata(18));
                        int height = Integer.parseInt(metadataRetriever.extractMetadata(19));
                        if (maxSize > width && maxSize > height) {
                            maxSize = width > height ? width : height;
                        }
                    } catch (Exception var34) {
                    }

                    if (captureTime < 0.0D) {
                        captureTime = 0.0D;
                    } else {
                        try {
                            double duration = (double)Long.parseLong(metadataRetriever.extractMetadata(9)) / 1000.0D;
                            if (captureTime > duration) {
                                captureTime = duration;
                            }
                        } catch (Exception var33) {
                        }
                    }

                    long frameTime = (long)(captureTime * 1000000.0D);
                    if (Build.VERSION.SDK_INT < 27) {
                        bitmap = metadataRetriever.getFrameAtTime(frameTime, 2);
                    } else {
                        bitmap = metadataRetriever.getScaledFrameAtTime(frameTime, 2, maxSize, maxSize);
                    }
                } finally {
                    metadataRetriever.release();
                }
            }

            if (bitmap != null) {
                out = new FileOutputStream(savePath);
                if (saveAsJpeg) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                } else {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }

                var39 = savePath;
                return var39;
            }

            var39 = "";
        } catch (Exception var37) {
            Log.e("Unity", "Exception:", var37);
            String var10 = "";
            return var10;
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }

            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception var32) {
            }

        }

        return var39;
    }

    private static Matrix GetImageOrientationCorrectionMatrix(int orientation, float scale) {
        Matrix matrix = new Matrix();
        switch(orientation) {
            case 2:
                matrix.postScale(-scale, scale);
                break;
            case 3:
                matrix.postRotate(180.0F);
                matrix.postScale(scale, scale);
                break;
            case 4:
                matrix.postScale(scale, -scale);
                break;
            case 5:
                matrix.postRotate(90.0F);
                matrix.postScale(-scale, scale);
                break;
            case 6:
                matrix.postRotate(90.0F);
                matrix.postScale(scale, scale);
                break;
            case 7:
                matrix.postRotate(270.0F);
                matrix.postScale(-scale, scale);
                break;
            case 8:
                matrix.postRotate(270.0F);
                matrix.postScale(scale, scale);
                break;
            default:
                matrix.postScale(scale, scale);
        }

        return matrix;
    }

    private static BitmapFactory.Options GetImageMetadata(String path) {
        try {
            BitmapFactory.Options result = new BitmapFactory.Options();
            result.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, result);
            return result;
        } catch (Exception var2) {
            Log.e("Unity", "Exception:", var2);
            return null;
        }
    }

    private static String GetSecondaryStoragePathFor(String localPath) {
        if (secondaryStoragePath == null) {
            String primaryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String strSDCardPath = System.getenv("SECONDARY_STORAGE");
            if (strSDCardPath == null || strSDCardPath.length() == 0) {
                strSDCardPath = System.getenv("EXTERNAL_SDCARD_STORAGE");
            }

            String[] externalPaths;
            String absolutePath;
            if (strSDCardPath != null && strSDCardPath.length() > 0) {
                if (!strSDCardPath.contains(":")) {
                    strSDCardPath = strSDCardPath + ":";
                }

                externalPaths = strSDCardPath.split(":");

                for (int i = 0; i < externalPaths.length; ++i) {
                    String path = externalPaths[i];
                    if (path != null && path.length() > 0) {
                        File file = new File(path);
                        if (file.exists() && file.isDirectory() && file.canRead() && !file.getAbsolutePath().equalsIgnoreCase(primaryPath)) {
                            absolutePath = file.getAbsolutePath() + File.separator + localPath;
                            if ((new File(absolutePath)).exists()) {
                                secondaryStoragePath = file.getAbsolutePath();
                                return absolutePath;
                            }
                        }
                    }
                }
            }

            externalPaths = new String[]{"/storage", "/mnt", "/storage/removable", "/removable", "/data", "/mnt/media_rw", "/mnt/sdcard0"};
            String[] var15 = externalPaths;
            int var16 = externalPaths.length;

            for (int var17 = 0; var17 < var16; ++var17) {
                absolutePath = var15[var17];

                try {
                    File[] fileList = (new File(absolutePath)).listFiles();
                    File[] var9 = fileList;
                    int var10 = fileList.length;

                    for (int var11 = 0; var11 < var10; ++var11) {
                        File file = var9[var11];
                        if (file.exists() && file.isDirectory() && file.canRead() && !file.getAbsolutePath().equalsIgnoreCase(primaryPath)) {
                            absolutePath = file.getAbsolutePath() + File.separator + localPath;
                            if ((new File(absolutePath)).exists()) {
                                secondaryStoragePath = file.getAbsolutePath();
                                return absolutePath;
                            }
                        }
                    }
                } catch (Exception var14) {
                }
            }

            secondaryStoragePath = "_NulL_";
        } else if (!secondaryStoragePath.equals("_NulL_")) {
            return secondaryStoragePath + File.separator + localPath;
        }

        return null;
    }

}
