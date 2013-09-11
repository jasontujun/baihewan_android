package com.morln.app.lbstask.ui.article;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.xengine.android.media.graphics.util.XGraphicUtil;
import com.xengine.android.media.image.processor.XAndroidImageProcessor;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.system.mobile.XPhotoListener;
import com.xengine.android.system.ui.XBaseFrame;
import com.xengine.android.system.ui.XDialog;
import com.xengine.android.system.ui.XUILayer;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.io.File;
import java.io.IOException;

/**
 * 图片详情对话框
 * Created by jasontujun.
 * Date: 12-3-4
 * Time: 下午9:37
 */
public class DPhoto implements XDialog {
    private XUILayer uiLayer;

    // 界面
    private Dialog dialog;
    private Button shootBtn, galleryBtn;
    private Button rotateBtn;
    private ImageView photoView;
    private RelativeLayout attrFrame;
    private TextView sizeView;
    private CheckBox compressBox;
    private Button okBtn,cancelBtn;

    private boolean isCompressed;
    private static final float COMPRESS_RATE = 0.75f;

    private File photoFile;
    private Bitmap bitmap;

    public DPhoto(XUILayer ul){
        this.uiLayer = ul;

        XBaseFrame activity = (XBaseFrame) uiLayer.getUIFrame();
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_photo);

        shootBtn = (Button) dialog.findViewById(R.id.shoot_btn);
        galleryBtn = (Button) dialog.findViewById(R.id.gallery_btn);
        photoView = (ImageView) dialog.findViewById(R.id.photo_view);
        rotateBtn = (Button) dialog.findViewById(R.id.rotate_btn);
        attrFrame = (RelativeLayout) dialog.findViewById(R.id.attr_frame);
        sizeView = (TextView) dialog.findViewById(R.id.size);
        compressBox = (CheckBox) dialog.findViewById(R.id.compress_box);
        okBtn = (Button) dialog.findViewById(R.id.ok_btn);
        cancelBtn = (Button) dialog.findViewById(R.id.cancel_btn);

        sizeView.setText("0B");

        shootBtn.setVisibility(View.VISIBLE);
        galleryBtn.setVisibility(View.VISIBLE);
        photoView.setVisibility(View.GONE);
        rotateBtn.setVisibility(View.GONE);
        attrFrame.setVisibility(View.GONE);

        // 监听
        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    bitmap = XGraphicUtil.rotate(90, bitmap);
                    photoView.setImageBitmap(bitmap);
                }
            }
        });
        compressBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isCompressed = b;
                if (b) {
                    sizeView.setText(XStringUtil.fileSize2String(
                            (long) (photoFile.length() * COMPRESS_RATE)));
                } else {
                    sizeView.setText(XStringUtil.fileSize2String(photoFile.length()));
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                dismiss();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (photoFile == null) {
                    Toast.makeText(uiLayer.getContext(), "没有选择图片哦~~", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(shootBtn, uiLayer.getContext());
                    AnimationUtil.startShakeAnimation(galleryBtn, uiLayer.getContext());
                    return;
                }

                // 先保存图片为文件
                int compressRate = 100;
                if (isCompressed) {
                    compressRate = 75;
                }
                XAndroidImageProcessor.getInstance().saveImageToSd(photoFile.getName(), bitmap,
                        Bitmap.CompressFormat.JPEG, compressRate);
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }

                Handler handler = uiLayer.getLayerHandler();
                Message msg = handler.obtainMessage();
                msg.what = BbsMsg.ADD_PHOTO;
                Bundle bundle = new Bundle();
                bundle.putString("file", photoFile.getAbsolutePath());
                bundle.putString("description", "");
                bundle.putBoolean("compress", compressBox.isChecked());
                msg.setData(bundle);
                handler.sendMessage(msg);

                dismiss();
            }
        });
        shootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uiLayer.mobileMgr().doTakePhoto(photoListener);
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uiLayer.mobileMgr().doPickPhotoFromGallery(photoListener);
            }
        });
    }

    // 获取图片的监听
    private XPhotoListener photoListener = new XPhotoListener() {
        @Override
        public void onSuccess(File file) {
            if (file != null) {
                XLog.d("PHOTO", "photo file:" + file.getAbsolutePath());
                try {
                    shootBtn.setVisibility(View.INVISIBLE);
                    galleryBtn.setVisibility(View.INVISIBLE);
                    photoView.setVisibility(View.VISIBLE);
                    rotateBtn.setVisibility(View.VISIBLE);
                    attrFrame.setVisibility(View.VISIBLE);

                    photoFile = file;
                    bitmap = XAndroidImageProcessor.getInstance()
                            .getLocalImage(file.getName(), XImageProcessor.ImageSize.SCREEN);
                    photoView.setImageBitmap(bitmap);
                    sizeView.setText(XStringUtil.fileSize2String(photoFile.length()));
                } catch (IOException e1) {
                    Toast.makeText(uiLayer.getContext(), "拍照失败~", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(uiLayer.getContext(), "拍照失败~", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onFail() {
            Toast.makeText(uiLayer.getContext(), "拍照失败~", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void dismiss() {
        dialog.dismiss();
    }
}
