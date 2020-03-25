package work.kozh.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.concurrent.ScheduledExecutorService;

import androidx.annotation.NonNull;
import work.kozh.media.dialog.MediaPlayerDialog;

/**
 * 封装一个简单的音频播放器
 * <p>
 * 通过一个api直接播放一个音频
 */
public class MediaPlayerHelper {

    private static final float SECONDS_NORMAL_SPPED = 1000; //一秒
    private MediaPlayer mediaPlayer;
    private Context mContext;
    private AudioManager audioManager;
    private int secondsTemp; //
    private String path; // 传入的音频地址
    private PopupWindow speed_popupWindow;

    private int thisViewHeight;

    private int speedText = 10001;

    private int soundSize = 10;

    private int status = 0;//播放器准备状态码

    //常量
    private final int SEEK_MAX = 1000;

    private final int SECONDS_NOMAL_SPPED = 1000;
    private final int MEDIA_STATUS_ISNOTSTART = 0;//未开始播放
    private final int MEDIA_STATUS_ISSTART = 1;//已经开始播放
    private final int MEDIA_STATUS_ISEND = 2;//播放结束
    private final int MEDIA_STATUS_ISERROR = 3;//播放出错
    private final int MEDIA_URL_NULL = 4;//url为空
    private final float MEDIA_SPEED_1_0 = 1.0f;//一倍速
    private final float MEDIA_SPEED_1_25 = 1.25f;//一点二倍速
    private final float MEDIA_SPEED_1_5 = 1.5f;//一点五倍速
    private final float MEDIA_SPEED_2_0 = 2.0f;//二倍速
    private final int MEDIA_CHECKEDSPEED_1_0 = 10001;//一倍速 选中标识
    private final int MEDIA_CHECKEDSPEED_1_25 = 10002;//一点二倍速 选中标识
    private final int MEDIA_CHECKEDSPEED_1_5 = 10003;//一点五倍速 选中标识
    private final int MEDIA_CHECKEDSPEED_2_0 = 10004;//二倍速 选中标识
    private MediaPlayerDialog mDialog;

    private String mPoster;

    private int duration;
    private ScheduledExecutorService mPool;

    private MediaPlayer simpleMediaPlayer;


    /**
     * 初始化 音频管理器
     */
    private void initAudioManager() {
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }


    // ****************************************  简易版播放  直接播放  ********************************************


    public MediaPlayerHelper() {
    }

    private MediaPlayer getSimpleMediaPlayer() {
        if (simpleMediaPlayer == null) {
            synchronized ("String") {
                if (simpleMediaPlayer == null) {
                    return new MediaPlayer();
                }
            }
        }
        return simpleMediaPlayer;
    }

    public void playMedia(final Context context, String path) {

        if (TextUtils.isEmpty(path)) {
            return;
        }

        try {
            Uri parse = Uri.parse(path);
            simpleMediaPlayer = getSimpleMediaPlayer();
            simpleMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            simpleMediaPlayer.setDataSource(context, parse);
            simpleMediaPlayer.prepareAsync();

            //准备完毕后，开始播放
            simpleMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    simpleMediaPlayer.start();
                    Log.i("TAG", "准备完毕，开始播放");
                }
            });

            //播放结束后 释放内存
            simpleMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    simpleMediaPlayer.release();
                    simpleMediaPlayer = null;
                    Toast.makeText(context, "歌曲播放完毕", Toast.LENGTH_SHORT).show();
                    Log.i("TAG", "播放完毕");
                }
            });

            simpleMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    simpleMediaPlayer.release();
                    simpleMediaPlayer = null;
                    Toast.makeText(context, "歌曲播放失败", Toast.LENGTH_SHORT).show();
                    Log.i("TAG", "播放出错");
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void pauseMedia() {
        try {
            if (simpleMediaPlayer != null) {
                Log.i("TAG", "暂停播放");
                simpleMediaPlayer.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeMedia() {
        try {
            if (simpleMediaPlayer != null) {
                Log.i("TAG", "关闭播放器");
                simpleMediaPlayer.pause();
                simpleMediaPlayer.release();
                simpleMediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ****************************************  高阶版播放  含弹窗组件  ********************************************


    public MediaPlayerHelper(Activity context) {
        this.mContext = context;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            try {
                if (what == 0) {
                    mDialog.setSeekBarProgress(mediaPlayer.getCurrentPosition());
                    mDialog.setCurrentDuration(getTime(mediaPlayer.getCurrentPosition()));
                    //1秒后循环执行
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    };


    /**
     * 设置播放速度  这是高阶的组件使用
     *
     * @param speed
     */
    private void changedSpeed(float speed) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!TextUtils.isEmpty(path)) {
                    mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(speed));
                    secondsTemp = (int) (SECONDS_NORMAL_SPPED / speed);
                    mediaPlayer.pause();
                    MediaStart();
                } else {
                    speedText = MEDIA_CHECKEDSPEED_1_0;
                    Toast.makeText(mContext, "无效的播放地址", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 以 dialog 的形式显示媒体组件
     *
     * @param title
     * @param author
     * @param poster
     */
    private void showModal(String title, String author, String poster) {

        Log.i("TAG", "开始创建dialog");

        mDialog = new MediaPlayerDialog(mContext);

        //关闭组件
        mDialog.setOnCloseListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeModal();
            }
        });

        //倍速设置
        mDialog.setOnSpeedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWind();
            }
        });

        //播放/暂停
        mDialog.setOnPlayListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!mediaPlayer.isPlaying()) {
                        MediaStart();
                    } else {
                        MediaPause();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        //进度条的拖动设置
        mDialog.setOnSeekListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("TAG", "seekBar 拖动的进度：" + seekBar.getProgress());
                try {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    mHandler.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        //设置封面等信息
        mDialog.setAuthor(author);

        mDialog.setImagePoster(poster);

        mDialog.setTitle(title);

        mDialog.setCanceledOnTouchOutside(false);

        mDialog.show();

    }


    /**
     * 展示功能弹框  倍速 调节音量
     */
    private void showPopupWind() {

        speed_popupWindow = new PopupWindow(mContext);
        speed_popupWindow.setWidth(mDialog.getDialogWidth());
        speed_popupWindow.setHeight(mDialog.getDialogHeight() / 3);

        View popupVeiw = LayoutInflater.from(mContext).inflate(R.layout.widget_play_speed, null);
        speed_popupWindow.setContentView(popupVeiw);
        speed_popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        speed_popupWindow.setOutsideTouchable(false);
        speed_popupWindow.setFocusable(true);
        speed_popupWindow.showAsDropDown(mDialog.getView());

        final RadioGroup my_music_bsgroup = (RadioGroup) popupVeiw.findViewById(R.id.my_music_bsgroup);
        SeekBar my_music_sound = (SeekBar) popupVeiw.findViewById(R.id.my_music_sound);

        //上一次popupwindow打开时的音量和倍速
        my_music_bsgroup.check(getCheckedId());
        my_music_sound.setProgress(soundSize);
        changedCheckedColor(my_music_bsgroup, getCheckedId());
        //radiobutton 选中
        my_music_bsgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.my_music_bsbtn1) {
                    speedText = MEDIA_CHECKEDSPEED_1_0;
                    changedSpeed(MEDIA_SPEED_1_0);
                } else if (checkedId == R.id.my_music_bsbtn1_25) {
                    speedText = MEDIA_CHECKEDSPEED_1_25;
                    changedSpeed(MEDIA_SPEED_1_25);
                } else if (checkedId == R.id.my_music_bsbtn1_5) {
                    speedText = MEDIA_CHECKEDSPEED_1_5;
                    changedSpeed(MEDIA_SPEED_1_5);

                } else if (checkedId == R.id.my_music_bsbtn2) {
                    speedText = MEDIA_CHECKEDSPEED_2_0;
                    changedSpeed(MEDIA_SPEED_2_0);
                }
                changedCheckedColor(my_music_bsgroup, checkedId);
            }
        });

        //播放音量
        my_music_sound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    soundSize = seekBar.getProgress();
                    float soundSize = progress * 0.1f;
                    mediaPlayer.setVolume(soundSize, soundSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //点击背景关闭弹框
        popupVeiw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed_popupWindow.dismiss();
            }
        });

    }

    /**
     * 倍速选中
     *
     * @return view的id
     */
    private int getCheckedId() {
        int checkedId = R.id.my_music_bsbtn1;
        switch (speedText) {
            case MEDIA_CHECKEDSPEED_1_0:
                return checkedId;
            case MEDIA_CHECKEDSPEED_1_25:
                checkedId = R.id.my_music_bsbtn1_25;
                return checkedId;
            case MEDIA_CHECKEDSPEED_1_5:
                checkedId = R.id.my_music_bsbtn1_5;
                return checkedId;
            case MEDIA_CHECKEDSPEED_2_0:
                checkedId = R.id.my_music_bsbtn2;
                return checkedId;
        }
        return checkedId;
    }

    /**
     * 改变选中btn颜色
     *
     * @param group     单选按钮的组
     * @param checkedId 组内选中的id
     */
    private void changedCheckedColor(RadioGroup group, int checkedId) {
        for (int i = 0; i < group.getChildCount(); i++) {
            if (i != 0) {
                int id = group.getChildAt(i).getId();
                RadioButton childAt = (RadioButton) group.getChildAt(i);
                if (id == checkedId) {
                    childAt.setTextColor(Color.rgb(216, 176, 118));
                } else {
                    childAt.setTextColor(Color.rgb(255, 255, 255));
                }
            }
        }
    }

    /**
     * 传入参数 开始播放 对外方法
     *
     * @param title
     * @param author
     * @param url
     * @param poster
     */
    public void setUp(String title, String author, String poster, String url) {

        this.path = url;//播放地址
        this.mPoster = poster;

        showModal(title, author, poster);

        try {
            //有效播放地址
            Uri parse = Uri.parse(url);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(mContext, parse);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    try {
                        //音频总长度
                        duration = mediaPlayer.getDuration();

                        //seekbar最大长度 以及文本显示
                        mDialog.setSeekBarDuration(duration);
                        mDialog.setTotalDuration(getTime(duration));

                        //开启一个线程 执行进度条 每秒执行一次
                         /*   mPool = Executors.newScheduledThreadPool(3);
                            mPool.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.sendEmptyMessage(0);
                                }
                            }, 1, TimeUnit.SECONDS);*/

                        MediaStart();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    try {
                        reset();
                        mediaPlayer.release();
                        mHandler.removeCallbacksAndMessages(null);
                        Toast.makeText(mContext, "歌曲播放失败", Toast.LENGTH_SHORT).show();
                        Log.i("TAG", "播放出错");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });


            //播放完成
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        reset();
    //                    mediaPlayer.release();
                        mHandler.removeCallbacksAndMessages(null);
                        Toast.makeText(mContext, "歌曲播放完毕", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将int值转换成时长
     *
     * @return 时长
     */
    private String getTime(int durations) {
        int time = durations / 1000;
        int temp;
        StringBuffer sb = new StringBuffer();
        temp = time / 3600;
        if (temp != 0) {//时长不超过一小时则不添加
            sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");
        }
        temp = time % 3600 / 60;
        sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");
        temp = time % 3600 % 60;
        sb.append((temp < 10) ? "0" + temp : "" + temp);
        return sb.toString();
    }

    /**
     * 暂停播放
     */
    private void MediaPause() {
        Log.i("TAG", "MediaPlayer 暂停");
        try {
            mediaPlayer.pause();
            mDialog.setPlayImage(R.drawable.waitting);
            mHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 音频 开始播放 继续播放 重新播放
     */
    private void MediaStart() {
        Log.i("TAG", "MediaPlayer 播放");
        try {
            mDialog.setPlayImage(R.drawable.playing);
            mediaPlayer.start();
            mHandler.sendEmptyMessage(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 释放内存 退出时清除
     */
    private void MediaClear() {
        try {
            mediaPlayer.release();
            mHandler.removeCallbacksAndMessages(null);
            mediaPlayer = null;
        } catch (Exception e) {
            Log.e("清理MediaPlayer", "" + e);
        }
        Log.i("TAG", "清除MediaPlayer");
    }

    /**
     * 重置状态
     */
    private void reset() {
        mDialog.setPlayImage(R.drawable.waitting);
        mHandler.removeCallbacksAndMessages(null);
        Log.i("TAG", "重置状态");
    }

    /**
     * 关闭媒体组件
     */
    private void closeModal() {

        MediaPause();
        MediaClear();
        if (mDialog != null) {
            mDialog.dismiss();
        }
        Log.i("TAG", "关闭组件");

    }
}
