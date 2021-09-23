package com.Ychao.NativeGallery;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.RemoteAction;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.Ychao.NativeGallery.MediaPicker.NativeGalleryMediaPickerFragment;
import com.Ychao.NativeGallery.MediaPicker.NativeGalleryMediaReceiver;
import com.Ychao.NativeGallery.Permission.NativeGalleryPermissionFragment;
import com.Ychao.NativeGallery.Permission.NativeGalleryPermissionReceiver;
import com.Ychao.NativeGallery.Utils.NativeGalleryUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NativeGallerySystem {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_AUDIO = 4;
    public static boolean overwriteExistingMedia = false;
    public static boolean mediaSaveOmitDCIM = false;

    public NativeGallerySystem() {
    }

    @SuppressLint("Range")
    public static String SaveMedia(Context context, int mediaType, String filePath, String directoryName) {
        File originalFile = new File(filePath);
        if (!originalFile.exists()) {
            Log.e("Unity", "Original media file is missing or inaccessible!");
            return "";
        } else {
            int pathSeparator = filePath.lastIndexOf(47);
            int extensionSeparator = filePath.lastIndexOf(46);
            String filename = pathSeparator >= 0 ? filePath.substring(pathSeparator + 1) : filePath;
            String extension = extensionSeparator >= 0 ? filePath.substring(extensionSeparator + 1) : "";
            String mimeType = extension.length() > 0 ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ENGLISH)) : null;
            ContentValues values = new ContentValues();
            values.put("title", filename);
            values.put("_display_name", filename);
            values.put("date_added", System.currentTimeMillis() / 1000L);
            if (mimeType != null && mimeType.length() > 0) {
                values.put("mime_type", mimeType);
            }

            if (mediaType == 1) {
                int imageOrientation = NativeGalleryUtils.GetImageOrientation(context, filePath);
                switch (imageOrientation) {
                    case 3:
                        values.put("orientation", 180);
                    case 4:
                    default:
                        break;
                    case 5:
                    case 6:
                        values.put("orientation", 90);
                        break;
                    case 7:
                    case 8:
                        values.put("orientation", 270);
                }
            }

            Uri externalContentUri;
            if (mediaType == 1) {
                externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (mediaType == 2) {
                externalContentUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else {
                externalContentUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            String path;
            if (Build.VERSION.SDK_INT >= 29) {
                values.put("relative_path", mediaSaveOmitDCIM ? directoryName + "/" : "DCIM/" + directoryName + "/");
                values.put("datetaken", System.currentTimeMillis());

                for (int i = 0; i < 2; ++i) {
                    values.put("is_pending", true);

                    if (i == 1) {
                        String filenameWithoutExtension = extension.length() > 0 && filename.length() > extension.length() ? filename.substring(0, filename.length() - extension.length() - 1) : filename;
                        path = filenameWithoutExtension + " " + (new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss")).format(new Date());
                        if (extension.length() > 0) {
                            path = path + "." + extension;
                        }

                        values.put("title", path);
                        values.put("_display_name", path);
                    }

                    Uri uri = null;
                    if (!overwriteExistingMedia) {
                        uri = context.getContentResolver().insert(externalContentUri, values);
                    } else {
                        Cursor cursor = null;

                        try {
                            path = "relative_path=? AND _display_name=?";
                            String[] selectionArgs = new String[]{values.getAsString("relative_path"), values.getAsString("_display_name")};
                            cursor = context.getContentResolver().query(externalContentUri, new String[]{"_id"}, path, selectionArgs, (String) null);
                            if (cursor != null && cursor.moveToFirst()) {
                                uri = ContentUris.withAppendedId(externalContentUri, cursor.getLong(cursor.getColumnIndex("_id")));
                                Log.d("Unity", "Overwriting existing media");
                            }
                        } catch (Exception var39) {
                            Log.e("Unity", "Couldn't overwrite existing media's metadata:", var39);
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }

                        }

                        if (uri == null) {
                            uri = context.getContentResolver().insert(externalContentUri, values);
                        }
                    }

                    if (uri != null) {
                        try {
                            if (NativeGalleryUtils.WriteFileToStream(originalFile, context.getContentResolver().openOutputStream(uri))) {
                                values.put("is_pending", false);
                                context.getContentResolver().update(uri, values, (String) null, (String[]) null);
                                Log.d("Unity", "Saved media to: " + uri.toString());
                                path = NativeGalleryUtils.getPathFromUri(context, uri);
                                return path != null && path.length() > 0 ? path : uri.toString();
                            }
                        } catch (IllegalStateException var43) {
                            if (i == 1) {
                                Log.e("Unity", "Exception:", var43);
                            }

                            context.getContentResolver().delete(uri, (String) null, (String[]) null);
                        } catch (Exception var44) {
                            Exception e = var44;
                            Log.e("Unity", "Exception:", var44);
                            if (overwriteExistingMedia && var44.getClass().getName().equals("android.app.RecoverableSecurityException")) {
                                try {
                                    RemoteAction remoteAction = (RemoteAction) e.getClass().getMethod("getUserAction").invoke(e);
                                    context.startIntentSender(remoteAction.getActionIntent().getIntentSender(), (Intent) null, 0, 0, 0);
                                } catch (Exception var37) {
                                    Log.e("Unity", "RecoverableSecurityException failure:", var37);
                                    return "";
                                }

                                path = NativeGalleryUtils.getPathFromUri(context, uri);
                                return path != null && path.length() > 0 ? path : uri.toString();
                            }

                            context.getContentResolver().delete(uri, (String) null, (String[]) null);
                            return "";
                        }
                    }

                    if (overwriteExistingMedia) {
                        break;
                    }
                }
            } else {
                File directory = new File(mediaSaveOmitDCIM ? Environment.getExternalStorageDirectory() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), directoryName);
                directory.mkdirs();
                int fileIndex = 1;
                path = extension.length() > 0 && filename.length() > extension.length() ? filename.substring(0, filename.length() - extension.length() - 1) : filename;
                String newFilename = filename;

                File file;
                do {
                    file = new File(directory, newFilename);
                    newFilename = path + fileIndex++;
                    if (extension.length() > 0) {
                        newFilename = newFilename + "." + extension;
                    }
                } while (!overwriteExistingMedia && file.exists());

                try {
                    if (NativeGalleryUtils.WriteFileToStream(originalFile, new FileOutputStream(file))) {
                        values.put("_data", file.getAbsolutePath());
                        if (!overwriteExistingMedia) {
                            context.getContentResolver().insert(externalContentUri, values);
                        } else {
                            Uri existingMediaUri = null;
                            Cursor cursor = null;

                            try {
                                cursor = context.getContentResolver().query(externalContentUri, new String[]{"_id"}, "_data=?", new String[]{values.getAsString("_data")}, (String) null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    existingMediaUri = ContentUris.withAppendedId(externalContentUri, cursor.getLong(cursor.getColumnIndex("_id")));
                                    Log.d("Unity", "Overwriting existing media");
                                }
                            } catch (Exception var38) {
                                Log.e("Unity", "Couldn't overwrite existing media's metadata:", var38);
                            } finally {
                                if (cursor != null) {
                                    cursor.close();
                                }

                            }

                            if (existingMediaUri == null) {
                                context.getContentResolver().insert(externalContentUri, values);
                            } else {
                                context.getContentResolver().update(existingMediaUri, values, (String) null, (String[]) null);
                            }
                        }

                        Log.d("Unity", "Saved media to: " + file.getPath());
                        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                        mediaScanIntent.setData(Uri.fromFile(file));
                        context.sendBroadcast(mediaScanIntent);
                        return file.getAbsolutePath();
                    }
                } catch (Exception var42) {
                    Log.e("Unity", "Exception:", var42);
                }
            }

            return "";
        }
    }

    public static void MediaDeleteFile(Context context, String path, int mediaType) {
        if (mediaType == 1) {
            context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "_data=?", new String[]{path});
        } else if (mediaType == 2) {
            context.getContentResolver().delete(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "_data=?", new String[]{path});
        } else {
            context.getContentResolver().delete(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_data=?", new String[]{path});
        }
    }


    public static void PickMedia(Context context, NativeGalleryMediaReceiver mediaReceiver, int mediaType, boolean selectMultiple, String savePath, String mime, String title) {
        if (CheckPermission(context, true) != 1) {
            if (!selectMultiple) {
                mediaReceiver.OnMediaReceived("");
            } else {
                mediaReceiver.OnMultipleMediaReceived("");
            }

        } else {
            Bundle bundle = new Bundle();
            bundle.putInt("NGMP_MEDIA_TYPE", mediaType);
            bundle.putBoolean("NGMP_MULTIPLE", selectMultiple);
            bundle.putString("NGMP_SAVE_PATH", savePath);
            bundle.putString("NGMP_MIME", mime);
            bundle.putString("NGMP_TITLE", title);
            Fragment request = new NativeGalleryMediaPickerFragment(mediaReceiver);
            request.setArguments(bundle);
            ((Activity) context).getFragmentManager().beginTransaction().add(0, request).commit();
        }
    }

    public static void RequestPermission(Context context, NativeGalleryPermissionReceiver permissionReceiver, boolean readPermission, int lastCheckResult) {
        if (CheckPermission(context, readPermission) == 1) {
            permissionReceiver.OnPermissionResult(1);
        } else if (lastCheckResult == 0) {
            permissionReceiver.OnPermissionResult(0);
        } else {
            Bundle bundle = new Bundle();
            bundle.putBoolean("NG_ReadOnly", readPermission);
            Fragment request = new NativeGalleryPermissionFragment(permissionReceiver);
            request.setArguments(bundle);
            ((Activity) context).getFragmentManager().beginTransaction().add(0, request).commit();
        }
    }

    @SuppressLint("WrongConstant")
    @TargetApi(23)
    public static int CheckPermission(Context context, boolean readPermission) {
        if (Build.VERSION.SDK_INT < 23) {
            return 1;
        } else if (!readPermission && Build.VERSION.SDK_INT >= 29) {
            return 1;
        } else {
            return context.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0 || !readPermission && context.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0 ? 0 : 1;
        }
    }


    public static void OpenSettings(Context context) {
        Uri uri = Uri.fromParts("package", context.getPackageName(), (String) null);
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static boolean CanSelectMultipleMedia() {
        return Build.VERSION.SDK_INT >= 18;
    }

    public static boolean CanSelectMultipleMediaTypes() {
        return Build.VERSION.SDK_INT >= 19;
    }


    public static String GetMimeTypeFromExtension(String extension) {
        if (extension != null && extension.length() != 0) {
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ENGLISH));
            return mime != null ? mime : "";
        } else {
            return "";
        }
    }


    public static String LoadImageAtPath(Context context, String path, String temporaryFilePath, int maxSize) {
        return NativeGalleryUtils.LoadImageAtPath(context, path, temporaryFilePath, maxSize);
    }


    public static String GetImageProperties(Context context, String path) {
        return NativeGalleryUtils.GetImageProperties(context, path);
    }

    @TargetApi(17)
    public static String GetVideoProperties(Context context, String path) {
        return NativeGalleryUtils.GetVideoProperties(context, path);
    }

    @TargetApi(29)
    public static String GetVideoThumbnail(Context context, String path, String savePath, boolean saveAsJpeg, int maxSize, double captureTime) {
        return NativeGalleryUtils.GetVideoThumbnail(context, path, savePath, saveAsJpeg, maxSize, captureTime);
    }

}
