package work.kozh.media.dialog;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import work.kozh.media.R;


//一个最普通的选择圆角dialog


public class MediaPlayerDialog extends Dialog {
    private ImageView mPlay;
    private ImageView mPoster;
    private ImageView mClose;
    private TextView mTitle;//定义标题文字
    private ImageView mSpeed;
    private SeekBar mSeekBar;
    private TextView mAuthor;

    private TextView mTvDuration;
    private TextView mTvTotalTime;


    private Context mContext;

    public MediaPlayerDialog(@NonNull Context context) {
        super(context, R.style.MyCircleDialog);
        this.mContext = context;

        //通过LayoutInflater获取布局
        View view = LayoutInflater.from(getContext()).inflate(R.layout.widget_media_player, null);
        mTitle = (TextView) view.findViewById(R.id.tv_title);
        mAuthor = (TextView) view.findViewById(R.id.tv_author);
        mSpeed = (ImageView) view.findViewById(R.id.speed);
        mClose = (ImageView) view.findViewById(R.id.close);
        mPoster = (ImageView) view.findViewById(R.id.img_poster);
        mPlay = (ImageView) view.findViewById(R.id.play);
        mSeekBar = (SeekBar) view.findViewById(R.id.seek);

        mTvDuration = view.findViewById(R.id.tv_duration);
        mTvTotalTime = view.findViewById(R.id.tv_total_time);

//        setContentView(view);  //设置显示的视图
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
        params.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.9);

        setContentView(view, params);

    }


    //设置标题
    public void setTitle(String content) {
        this.mTitle.setText(content);
    }

    //设置作者
    public void setAuthor(String content) {
        this.mAuthor.setText(content);
    }

    //设置图片
    public void setImagePoster(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Glide.with(mContext).load(path).into(mPoster);
    }

    public void setImagePoster(Uri path) {
        if (path == null) {
            return;
        }
        Glide.with(mContext).load(path).into(mPoster);
    }

    //设置当前进度文本
    public void setCurrentDuration(String time) {
        this.mTvDuration.setText(time);
    }

    //设置总进度文本
    public void setTotalDuration(String time) {
        this.mTvTotalTime.setText(time);
    }


    //设置当前进度seekBar
    public void setSeekBarProgress(int progress) {
        this.mSeekBar.setProgress(progress);
    }

    //设置总进度seekBar
    public void setSeekBarDuration(int progress) {
        this.mSeekBar.setMax(progress);
    }

    //设置播放状态图片
    public void setPlayImage(int resId) {
        Glide.with(mContext).load(resId).into(mPlay);
    }

    //倍速监听
    public void setOnSpeedListener(View.OnClickListener listener) {
        this.mSpeed.setOnClickListener(listener);
    }

    //关闭监听
    public void setOnCloseListener(View.OnClickListener listener) {
        this.mClose.setOnClickListener(listener);
    }

    //播放监听
    public void setOnPlayListener(View.OnClickListener listener) {
        this.mPlay.setOnClickListener(listener);
    }

    //进度监听
    public void setOnSeekListener(SeekBar.OnSeekBarChangeListener listener) {
        this.mSeekBar.setOnSeekBarChangeListener(listener);
    }

    //获取倍速view
    public View getView() {
        return mSpeed;
    }

    //获取dialog的宽高
    public int getDialogWidth() {
        return (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
    }

    public int getDialogHeight() {
        return (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.8);
    }

}
