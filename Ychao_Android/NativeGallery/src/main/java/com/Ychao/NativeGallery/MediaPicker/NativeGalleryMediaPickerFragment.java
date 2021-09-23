package com.Ychao.NativeGallery.MediaPicker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.Ychao.NativeGallery.Utils.NativeGalleryUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class NativeGalleryMediaPickerFragment extends Fragment {

    private static final int MEDIA_REQUEST_CODE = 987455;
    public static final String MEDIA_TYPE_ID = "NGMP_MEDIA_TYPE";
    public static final String SELECT_MULTIPLE_ID = "NGMP_MULTIPLE";
    public static final String SAVE_PATH_ID = "NGMP_SAVE_PATH";
    public static final String MIME_ID = "NGMP_MIME";
    public static final String TITLE_ID = "NGMP_TITLE";

    public static boolean preferGetContent = false;
    public static boolean tryPreserveFilenames = false;
    private final NativeGalleryMediaReceiver mediaReceiver;
    private boolean selectMultiple;
    private String savePathDirectory;
    private String savePathFilename;
    private ArrayList<String> savedFiles;


    public NativeGalleryMediaPickerFragment(){
        this.mediaReceiver = null;
    }

    @SuppressLint("ValidFragment")
    public NativeGalleryMediaPickerFragment(NativeGalleryMediaReceiver mediaReceiver){
        this.mediaReceiver = mediaReceiver;
    }

    @SuppressLint("WrongConstant")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mediaReceiver == null) {
            this.getFragmentManager().beginTransaction().remove(this).commit();
        } else {
            int mediaType = this.getArguments().getInt("NGMP_MEDIA_TYPE");
            String mime = this.getArguments().getString("NGMP_MIME");
            String title = this.getArguments().getString("NGMP_TITLE");
            this.selectMultiple = this.getArguments().getBoolean("NGMP_MULTIPLE");
            String savePath = this.getArguments().getString("NGMP_SAVE_PATH");
            int pathSeparator = savePath.lastIndexOf(47);
            this.savePathFilename = pathSeparator >= 0 ? savePath.substring(pathSeparator + 1) : savePath;
            this.savePathDirectory = pathSeparator > 0 ? savePath.substring(0, pathSeparator) : this.getActivity().getCacheDir().getAbsolutePath();
            int mediaTypeCount = 0;
            if ((mediaType & 1) == 1) {
                ++mediaTypeCount;
            }

            if ((mediaType & 2) == 2) {
                ++mediaTypeCount;
            }

            if ((mediaType & 4) == 4) {
                ++mediaTypeCount;
            }

            Intent intent;
            if (!preferGetContent && !this.selectMultiple && mediaTypeCount == 1 && mediaType != 4) {
                if (mediaType == 1) {
                    intent = new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                } else if (mediaType == 2) {
                    intent = new Intent("android.intent.action.PICK", android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                } else {
                    intent = new Intent("android.intent.action.PICK", android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                }
            } else {
                intent = new Intent(mediaTypeCount > 1 && Build.VERSION.SDK_INT >= 19 ? "android.intent.action.OPEN_DOCUMENT" : "android.intent.action.GET_CONTENT");
                intent.addCategory("android.intent.category.OPENABLE");
                intent.addFlags(1);
                if (this.selectMultiple && Build.VERSION.SDK_INT >= 18) {
                    intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
                }

                if (mediaTypeCount > 1) {
                    mime = "*/*";
                    if (Build.VERSION.SDK_INT >= 19) {
                        String[] mimetypes = new String[mediaTypeCount];
                        int index = 0;
                        if ((mediaType & 1) == 1) {
                            mimetypes[index++] = "image/*";
                        }

                        if ((mediaType & 2) == 2) {
                            mimetypes[index++] = "video/*";
                        }

                        if ((mediaType & 4) == 4) {
                            mimetypes[index++] = "audio/*";
                        }

                        intent.putExtra("android.intent.extra.MIME_TYPES", mimetypes);
                    }
                }
            }

            intent.setType(mime);
            if (title != null && title.length() > 0) {
                intent.putExtra("android.intent.extra.TITLE", title);
            }

            this.startActivityForResult(Intent.createChooser(intent, title), 987455);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 987455) {
            if (!this.selectMultiple) {
                String result;
                if (resultCode == -1 && data != null) {
                    result = this.getPathFromURI(data.getData());
                    if (result == null) {
                        result = "";
                    }
                } else {
                    result = "";
                }

                if (result.length() > 0 && !(new File(result)).exists()) {
                    result = "";
                }

                if (this.mediaReceiver != null) {

                    //回调函数
                    this.mediaReceiver.OnMediaReceived(result);
                }
            } else {
                ArrayList<String> result = new ArrayList();
                if (resultCode == -1 && data != null) {
                    this.fetchPathsOfMultipleMedia(result, data);
                }

                for(int i = result.size() - 1; i >= 0; --i) {
                    if (result.get(i) == null || ((String)result.get(i)).length() == 0 || !(new File((String)result.get(i))).exists()) {
                        result.remove(i);
                    }
                }

                String resultCombined = "";

                for(int i = 0; i < result.size(); ++i) {
                    if (i == 0) {
                        resultCombined = resultCombined + (String)result.get(i);
                    } else {
                        resultCombined = resultCombined + ">" + (String)result.get(i);
                    }
                }

                if (this.mediaReceiver != null) {

                    //回调函数
                    this.mediaReceiver.OnMultipleMediaReceived(resultCombined);
                }
            }

            this.getFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @TargetApi(18)
    private void fetchPathsOfMultipleMedia(ArrayList<String> result, Intent data) {
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();

            for(int i = 0; i < count; ++i) {
                result.add(this.getPathFromURI(data.getClipData().getItemAt(i).getUri()));
            }
        } else if (data.getData() != null) {
            result.add(this.getPathFromURI(data.getData()));
        }

    }

    private String getPathFromURI(Uri uri) {
        if (uri == null) {
            return null;
        } else {
            Log.d("Unity", "Selected media uri: " + uri.toString());
            String path = NativeGalleryUtils.getPathFromUri(this.getActivity(), uri);
            if (path != null && path.length() > 0) {
                FileInputStream inputStream = null;

                String var4;
                try {
                    inputStream = new FileInputStream(new File(path));
                    inputStream.read();
                    var4 = path;
                } catch (Exception var14) {
                    return this.copyToTempFile(uri);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception var13) {
                        }
                    }

                }

                return var4;
            } else {
                return this.copyToTempFile(uri);
            }
        }
    }

    @SuppressLint("Range")
    private String copyToTempFile(Uri uri) {
        ContentResolver resolver = this.getActivity().getContentResolver();
        Cursor returnCursor = null;
        String filename = null;

        try {
            returnCursor = resolver.query(uri, (String[])null, (String)null, (String[])null, (String)null);
            if (returnCursor != null && returnCursor.moveToFirst()) {
                filename = returnCursor.getString(returnCursor.getColumnIndex("_display_name"));
            }
        } catch (Exception var23) {
            Log.e("Unity", "Exception:", var23);
        } finally {
            if (returnCursor != null) {
                returnCursor.close();
            }

        }

        if (filename == null || filename.length() < 3) {
            filename = "temp";
        }

        String extension = null;
        String mime = resolver.getType(uri);
        if (mime != null) {
            String mimeExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
            if (mimeExtension != null && mimeExtension.length() > 0) {
                extension = "." + mimeExtension;
            }
        }

        if (extension == null) {
            int filenameExtensionIndex = filename.lastIndexOf(46);
            if (filenameExtensionIndex > 0 && filenameExtensionIndex < filename.length() - 1) {
                extension = filename.substring(filenameExtensionIndex);
            } else {
                extension = ".tmp";
            }
        }

        if (!tryPreserveFilenames) {
            filename = this.savePathFilename;
        } else if (filename.endsWith(extension)) {
            filename = filename.substring(0, filename.length() - extension.length());
        }

        try {
            InputStream input = resolver.openInputStream(uri);
            if (input == null) {
                return null;
            } else {
                String fullName = filename + extension;
                if (this.savedFiles != null) {
                    int n = 1;

                    for(int i = 0; i < this.savedFiles.size(); ++i) {
                        if (((String)this.savedFiles.get(i)).equals(fullName)) {
                            ++n;
                            fullName = filename + n + extension;
                            i = -1;
                        }
                    }
                }

                File tempFile = new File(this.savePathDirectory, fullName);
                FileOutputStream output = null;

                String var13;
                try {
                    output = new FileOutputStream(tempFile, false);
                    byte[] buf = new byte[4096];

                    int len;
                    while((len = input.read(buf)) > 0) {
                        output.write(buf, 0, len);
                    }

                    if (this.selectMultiple) {
                        if (this.savedFiles == null) {
                            this.savedFiles = new ArrayList();
                        }

                        this.savedFiles.add(fullName);
                    }

                    var13 = tempFile.getAbsolutePath();
                } finally {
                    if (output != null) {
                        output.close();
                    }

                    input.close();
                }

                return var13;
            }
        } catch (Exception var26) {
            Log.e("Unity", "Exception:", var26);
            return null;
        }
    }
}
