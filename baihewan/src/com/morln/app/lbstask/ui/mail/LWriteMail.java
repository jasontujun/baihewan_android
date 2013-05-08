package com.morln.app.lbstask.ui.mail;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.ExpressionMap;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.cache.SystemSettingSource;
import com.morln.app.lbstask.logic.BbsArticleMgr;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.article.DExpression;
import com.morln.app.lbstask.ui.article.DPhoto;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.utils.StatusCode;
import com.morln.app.system.ui.XBackType;
import com.morln.app.system.ui.XBaseLayer;
import com.morln.app.system.ui.XUIFrame;
import com.morln.app.utils.XLog;
import com.morln.app.utils.XStringUtil;

import java.io.File;

/**
 * Created by jasontujun.
 * Date: 12-3-4
 * Time: 下午5:27
 */
public class LWriteMail extends XBaseLayer {
    private SystemSettingSource systemSettingSource;
    private BbsMailMgr bbsMailMgr;
    private ExpressionMap expressionMap;

    private static final String MAIL_BOARD = "MAILBOX";
    private String receiver;
    private String title, content;

    // 界面
    private RelativeLayout frame;
    private LinearLayout topFrame, contentFrame;
    private ImageView backBtn;
    private Button topBtn1, topBtn2, topBtn3;
    private EditText titleInput, contentInput;
    private LinearLayout boardTipFrame;
    private TextView receiverNameView;
    private TextView signature;

    public LWriteMail(XUIFrame uiFrame,String t, String c, String r) {
        super(uiFrame);

        this.title = t;
        this.content = c;
        this.receiver = r;
        bbsMailMgr = BbsMailMgr.getInstance();
        expressionMap = ExpressionMap.getInstance();
        systemSettingSource = (SystemSettingSource) DataRepo.getInstance().getSource(SourceName.SYSTEM_SETTING);

        setContentView(R.layout.bbs_write_mail);
        frame = (RelativeLayout) findViewById(R.id.frame);
        topFrame = (LinearLayout) findViewById(R.id.top_frame);
        contentFrame = (LinearLayout) findViewById(R.id.content_frame);
        backBtn = (ImageView) findViewById(R.id.back_btn);
        topBtn1 = (Button) findViewById(R.id.top_btn1);
        topBtn2 = (Button) findViewById(R.id.top_btn2);
        topBtn3 = (Button) findViewById(R.id.top_btn3);
        titleInput = (EditText) findViewById(R.id.title_input);
        contentInput = (EditText) findViewById(R.id.content_input);
        boardTipFrame = (LinearLayout) findViewById(R.id.current_board_tip_frame);
        receiverNameView = (TextView) findViewById(R.id.current_board_id_tip);
        signature = (TextView) findViewById(R.id.signature);

        // 设置当前接受者
        receiverNameView.setText(receiver);
        // 初始化标题
        if(!XStringUtil.isNullOrEmpty(title)) {
            titleInput.setText(title);
        }
        // 初始化内容
        if(!XStringUtil.isNullOrEmpty(content)) {
            titleInput.setText(content);
        }
        // 设置签名
        String s = systemSettingSource.getMobileSignature();
        if(XStringUtil.isNullOrEmpty(s)) {
            signature.setText("还没有手机签名哦~");
            signature.setTextColor(getContext().getResources().getColor(R.color.gray));
        }else {
            signature.setText("from百荷湾: "+s);
            signature.setTextColor(getContext().getResources().getColor(R.color.light_purple));
        }


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler1 = getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.WRITE_MAIL_BACK;
                msg.arg1 = 0;// 发帖失败
                handler1.sendMessage(msg);
            }
        });
        topBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DPhoto(LWriteMail.this).show();
            }
        });
        topBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DExpression(LWriteMail.this).show();
            }
        });
        topBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 检验标题或内容不能为空
                title = titleInput.getText().toString();
                if (title == null || title.equals("")) {
                    Toast.makeText(getContext(), "请填写标题！", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(titleInput, getContext());
                    return;
                }
                content = contentInput.getText().toString();
                if (content == null || content.equals("")) {
                    Toast.makeText(getContext(), "请填写内容！", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(contentInput, getContext());
                    return;
                }

                new SendMailTask().execute(null);
            }
        });
    }

    /**
     * TIP 暂不支持！
     * @param localPhotoUrl
     * @param description
     * @param isCompress
     */
    public void addPhoto(String localPhotoUrl, String description, boolean isCompress) {
        // TODO 压缩！
        if(isCompress) {

        }
        File photoFile = new File(localPhotoUrl);
        // 和BBS服务器进行通信。获取服务器上图片的url
        new UploadImageTask(photoFile, description).execute(null);
    }


    @Override
    public int back() {
        return XBackType.SELF_BACK;
    }

    @Override
    public Handler getLayerHandler() {
        return handler;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BbsMsg.EXPRESSION:
                    int expIndex = msg.arg1;
                    String bbsExpression = expressionMap.getBbsExpression(expIndex);

                    int currentIndex = contentInput.getSelectionStart();//获取光标所在位置
                    Editable contentEditable = contentInput.getText();
                    XLog.d("ARTICLE", "光标位置：" + currentIndex + ",当前文本长度" + contentEditable.length());
                    if(0 <= currentIndex && currentIndex <= contentEditable.length()){
                        contentEditable.insert(currentIndex, bbsExpression);
                        // 把表情字符替换为图片
                        CharSequence str = expressionMap.changeToSpanString(LWriteMail.this, contentEditable.toString());
                        contentInput.setText(str);
                        // 调整光标
                        contentInput.setSelection(currentIndex+bbsExpression.length());
                    }
                    break;
                case BbsMsg.ADD_PHOTO:{
                    Bundle bundle = msg.getData();
                    String photoUrl = bundle.getString("file");
                    String description = bundle.getString("description");
                    boolean isCompressed = bundle.getBoolean("compress");
                    addPhoto(photoUrl, description, isCompressed);
                    break;
                }
            }
        }
    };




    /**
     * 上传图片的异步线程
     */
    private class UploadImageTask extends AsyncTask<Void, Void, String> {

        private DialogUtil.WaitingDialog waitingDialog;

        private File imgFile;
        private String description;

        public UploadImageTask(File imgFile, String description) {
            this.imgFile = imgFile;
            this.description = description;
        }

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show("正在上传图片\n请稍后");
        }
        @Override
        protected String doInBackground(Void... para) {
            return BbsArticleMgr.getInstance().uploadImage(MAIL_BOARD, imgFile, description);
        }
        @Override
        protected void onPostExecute(String resultString) {
            if(resultString != null){
                Toast.makeText(getContext(), "上传图片成功！", Toast.LENGTH_SHORT).show();

                int currentIndex = contentInput.getSelectionStart();//获取光标所在位置
                Editable contentEditable = contentInput.getText();
                XLog.d("ARTICLE", "光标位置：" + currentIndex + ",当前文本长度" + contentEditable.length());
                if(0 <= currentIndex && currentIndex <= contentEditable.length()){
                    contentEditable.insert(currentIndex, resultString);
                    contentInput.setSelection(currentIndex + resultString.length());
                }
            }else {
                Toast.makeText(getContext(), "上传图片失败！", Toast.LENGTH_SHORT).show();
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            Toast.makeText(getContext(), "上传图片失败！", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 发送文章的异步线程
     */
    private class SendMailTask extends AsyncTask<Void, Void, Integer> {

        private DialogUtil.WaitingDialog waitingDialog;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode = bbsMailMgr.sendMail(title, content, receiver);
            return resultCode;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            if(StatusCode.isSuccess(resultCode)) {
                Toast.makeText(getContext(), "发送站内信成功！", Toast.LENGTH_SHORT).show();

                Handler handler1 = getFrameHandler();
                Message msg = handler1.obtainMessage();
                msg.what = MainMsg.WRITE_MAIL_BACK;
                msg.arg1 = 1;// 发帖成功
                handler1.sendMessage(msg);
            }else {
                switch(resultCode) {
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                        new DLogin(LWriteMail.this, true).show("由于长时间发呆，要重新登录哦");
                        Toast.makeText(getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "发送站内信失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            Toast.makeText(getContext(), "发送站内信失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
