package com.Ychao.NativeGallery.Permission;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

@TargetApi(23)
public class NativeGalleryPermissionFragment extends Fragment {

    public static final String READ_PERMISSION_ONLY = "NG_ReadOnly";
    private static final int PERMISSIONS_REQUEST_CODE = 123655;
    private final NativeGalleryPermissionReceiver permissionReceiver;

    public NativeGalleryPermissionFragment() {
        this.permissionReceiver = null;
    }

    @SuppressLint("ValidFragment")
    public NativeGalleryPermissionFragment(NativeGalleryPermissionReceiver permissionReceiver) {
        this.permissionReceiver = permissionReceiver;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.permissionReceiver == null) {
            this.getFragmentManager().beginTransaction().remove(this).commit();
        } else {
            boolean readPermissionOnly = this.getArguments().getBoolean("NG_ReadOnly");
            String[] permissions = readPermissionOnly ? new String[]{"android.permission.READ_EXTERNAL_STORAGE"} : new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
            this.requestPermissions(permissions, 123655);
        }

    }

    @SuppressLint("WrongConstant")
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123655) {
            if (this.permissionReceiver == null) {
                Log.e("Unity", "Fragment data got reset while asking permissions!");
                this.getFragmentManager().beginTransaction().remove(this).commit();
            } else {
                int result = 1;
                if (permissions.length != 0 && grantResults.length != 0) {
                    for(int i = 0; i < permissions.length && i < grantResults.length; ++i) {
                        if (grantResults[i] == -1) {
                            if (!this.shouldShowRequestPermissionRationale(permissions[i])) {
                                result = 0;
                                break;
                            }

                            result = 2;
                        }
                    }
                } else {
                    result = 2;
                }

                // Permission 函数回调
                this.permissionReceiver.OnPermissionResult(result);
                this.getFragmentManager().beginTransaction().remove(this).commit();

                try {
                    Intent resumeUnityActivity = new Intent(this.getActivity(), this.getActivity().getClass());
                    resumeUnityActivity.setFlags(131072);
                    this.getActivity().startActivityIfNeeded(resumeUnityActivity, 0);
                } catch (Exception var6) {
                    Log.e("Unity", "Exception (resume):", var6);
                }

            }
        }
    }








}
