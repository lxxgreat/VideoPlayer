package com.shane.android.videoplayer.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.shane.android.videoplayer.R;
import com.shane.android.videoplayer.bean.Video;
import com.shane.android.videoplayer.bean.VideoUrl;
import com.shane.android.videoplayer.interf.IController;
import com.shane.android.videoplayer.util.LogUtil;

import org.cybergarage.upnp.Device;
import org.wlf.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author shane（https://github.com/lxxgreat）
 * @version 1.0
 * @created 2016-10-23
 */

public class SuperVideoPlayer extends RelativeLayout {
    private static final String TAG = SuperVideoPlayer.class.getSimpleName();
    public static final String KEY_URL = "key_url";
    public static final String KEY_PATH = "key_path";

    private final int MSG_HIDE_CONTROLLER = 10;
    private final int MSG_UPDATE_PLAY_TIME = 11;
    private final int MSG_EXIT_FORM_TV_RESULT = 13;
    private final int MSG_FILE_DOWNLOAD_SUCCEED = 14;
    private final int MSG_FILE_DOWNLOAD_FAILED = 15;
    private final int MSG_FILE_DOWNLOAD_ING = 16;

    private MediaController.PageType mCurrPageType = MediaController.PageType.SHRINK;//当前是横屏还是竖屏

    private Context mContext;
    private SuperVideoView mSuperVideoView;
    private MediaController mMediaController;
    private Timer mUpdateTimer;
    private VideoPlayCallbackImpl mVideoPlayCallback;

    private View mProgressBarView;
    private View mCloseBtnView;
    private View mTvBtnView;
    private View mDLNARootLayout;

    private ArrayList<Video> mAllVideo;
    private Video mNowPlayVideo;

    private List<Device> mDevices;
    private IController mController;
    private Device mSelectDevice;
    //是否自动隐藏控制栏
    private boolean mAutoHideController = true;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_PLAY_TIME:
                    updatePlayTime();
                    updatePlayProgress();
                    break;
                case MSG_HIDE_CONTROLLER:
                    showOrHideController();
                    break;
                case MSG_FILE_DOWNLOAD_SUCCEED:
                    LogUtil.d(TAG, "MSG_FILE_DOWNLOAD_SUCCEED:" + mNowPlayVideo.getPlayUrl().getUrl());
                    Bundle data = msg.getData();
                    String url = data.getString(KEY_URL);
                    String local = data.getString(KEY_PATH);

                    if (mNowPlayVideo.getPlayUrl().getUrl().equals(local)) {
                        Uri uri = Uri.parse(local);
                        mSuperVideoView.setVideoURI(uri);
                        mSuperVideoView.setVisibility(VISIBLE);
                        startPlayVideo(0);
                        LogUtil.d(TAG, "url:" + url + " path:" + local);
                    }
                    break;
                case MSG_FILE_DOWNLOAD_FAILED:
                    LogUtil.d(TAG, "MSG_FILE_DOWNLOAD_FAILED:" + mNowPlayVideo.getPlayUrl().getUrl());
                    Bundle data2 = msg.getData();
                    String url2 = data2.getString(KEY_URL);
                    String local2 = data2.getString(KEY_PATH);

                    if (mNowPlayVideo.getPlayUrl().getUrl().equals(url2)) {
                        mSuperVideoView.setVideoPath(url2);
                        mSuperVideoView.setVisibility(VISIBLE);
                        startPlayVideo(0);
                        LogUtil.d(TAG, "url2:" + url2 + " path:" + local2);
                    }
                    break;
                case MSG_FILE_DOWNLOAD_ING:
                    FileDownloader.start(mNowPlayVideo.getPlayUrl().getUrl());
                    break;

                default:
                    break;
            }

            return false;
        }
    });


    public void notifyFileDownloaderStatus (String url, boolean status, String path) {
        LogUtil.d(TAG, "notifyFileDownloaderStatus---2");
        Bundle data = new Bundle();
        data.putString(KEY_URL, url);
        data.putString(KEY_PATH, path);
        Message msg = new Message();
        msg.setData(data);
        msg.what = status ? MSG_FILE_DOWNLOAD_SUCCEED:MSG_FILE_DOWNLOAD_FAILED;
        mHandler.sendMessage(msg);
    }

    private View.OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.video_close_view) {
                mVideoPlayCallback.onCloseVideo();
            }
        }
    };

    private View.OnTouchListener mOnTouchVideoListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                showOrHideController();
            }
            return mCurrPageType == MediaController.PageType.EXPAND;
        }
    };

    private MediaController.MediaControlImpl mMediaControl = new MediaController.MediaControlImpl() {
        @Override
        public void alwaysShowController() {
            SuperVideoPlayer.this.alwaysShowController();
        }

        @Override
        public void onSelectSrc(int position) {
            Video selectVideo = mAllVideo.get(position);
            if (selectVideo.equal(mNowPlayVideo)) return;
            mNowPlayVideo = selectVideo;
            mNowPlayVideo.setPlayPos(0);
            mMediaController.initPlayVideo(mNowPlayVideo);
            loadAndPlay(mNowPlayVideo.getPlayUrl(), 0);
        }

        @Override
        public void onSelectFormat(int position) {
            VideoUrl videoUrl = mNowPlayVideo.getVideoUrl().get(position);
            if (mNowPlayVideo.getPlayUrl().equal(videoUrl)) return;
            mNowPlayVideo.setPlayPos(position);
            playVideoAtLastPos();
        }

        @Override
        public void onPlayTurn() {
            if (mSuperVideoView.isPlaying()) {
                pausePlay(true);
            } else {
                goOnPlay();
            }
        }

        @Override
        public void onPageTurn() {
            mVideoPlayCallback.onSwitchPageType();
        }

        @Override
        public void onProgressTurn(MediaController.ProgressState state, int progress) {
            if (state.equals(MediaController.ProgressState.START)) {
                mHandler.removeMessages(MSG_HIDE_CONTROLLER);
            } else if (state.equals(MediaController.ProgressState.STOP)) {
                resetHideTimer();
            } else {
                int time = progress * mSuperVideoView.getDuration() / 100;
                mSuperVideoView.seekTo(time);
                updatePlayTime();
            }
        }
    };

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    LogUtil.d(TAG, "mOnPreparedListener, onInfo");

                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START
                            || what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
                        mProgressBarView.setVisibility(View.GONE);
                        setCloseButton(true);
                        return true;
                    }
                    return false;
                }
            });

        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            stopUpdateTimer();
            stopHideTimer(true);
            mMediaController.playFinish(mSuperVideoView.getDuration());
            mVideoPlayCallback.onPlayFinish();
            Toast.makeText(mContext, "视频播放完成", Toast.LENGTH_SHORT).show();
        }
    };

    public void setVideoPlayCallback(VideoPlayCallbackImpl videoPlayCallback) {
        mVideoPlayCallback = videoPlayCallback;
    }

    /**
     * 如果在地图页播放视频，请先调用该接口
     */
    @SuppressWarnings("unused")
    public void setSupportPlayOnSurfaceView() {
        mSuperVideoView.setZOrderMediaOverlay(true);
    }

    @SuppressWarnings("unused")
    public SuperVideoView getSuperVideoView() {
        return mSuperVideoView;
    }

    public void setPageType(MediaController.PageType pageType) {
        mMediaController.setPageType(pageType);
        mCurrPageType = pageType;
    }

    /***
     * 强制横屏模式
     */
    @SuppressWarnings("unused")
    public void forceLandscapeMode() {
        mMediaController.forceLandscapeMode();
    }

    /***
     * 播放本地视频 只支持横屏播放
     *
     * @param fileUrl fileUrl
     */
    @SuppressWarnings("unused")
    public void loadLocalVideo(String fileUrl) {
        VideoUrl videoUrl = new VideoUrl();
        videoUrl.setIsOnlineVideo(false);
        videoUrl.setFormatUrl(fileUrl);
        videoUrl.setFormatName("本地视频");
        Video video = new Video();
        ArrayList<VideoUrl> videoUrls = new ArrayList<>();
        videoUrls.add(videoUrl);
        video.setVideoUrl(videoUrls);
        video.setPlayPos(0);

        mNowPlayVideo = video;

        /***
         * 初始化控制条的精简模式
         */
        mMediaController.initTrimmedMode();
        loadAndPlay(mNowPlayVideo.getPlayUrl(), 0);
    }

    /**
     * 播放多个视频,默认播放第一个视频，第一个格式
     *
     * @param allVideo 所有视频
     */
    public void loadMultipleVideo(ArrayList<Video> allVideo) {
        loadMultipleVideo(allVideo, 0, 0);
    }

    /**
     * 播放多个视频
     *
     * @param allVideo     所有的视频
     * @param selectVideo  指定的视频
     * @param selectFormat 指定的格式
     */
    public void loadMultipleVideo(ArrayList<Video> allVideo, int selectVideo, int selectFormat) {
        loadMultipleVideo(allVideo, selectVideo, selectFormat, 0);
    }

    /***
     * @param allVideo     所有的视频
     * @param selectVideo  指定的视频
     * @param selectFormat 指定的格式
     * @param seekTime     开始进度
     */
    public void loadMultipleVideo(ArrayList<Video> allVideo, int selectVideo, int selectFormat, int seekTime) {
        if (null == allVideo || allVideo.size() == 0) {
            Toast.makeText(mContext, "视频列表为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mAllVideo.clear();
        mAllVideo.addAll(allVideo);
        mNowPlayVideo = mAllVideo.get(selectVideo);
        mNowPlayVideo.setPlayPos(selectFormat);
        mMediaController.initVideoList(mAllVideo);
        mMediaController.initPlayVideo(mNowPlayVideo);

        loadAndPlay(mNowPlayVideo.getPlayUrl(), seekTime);
    }

    /**
     * 暂停播放
     *
     * @param isShowController 是否显示控制条
     */
    public void pausePlay(boolean isShowController) {
        mSuperVideoView.pause();
        mMediaController.setPlayState(MediaController.PlayState.PAUSE);
        stopHideTimer(isShowController);
    }

    /***
     * 继续播放
     */
    public void goOnPlay() {
        mSuperVideoView.start();
        mMediaController.setPlayState(MediaController.PlayState.PLAY);
        resetHideTimer();
        resetUpdateTimer();
    }

    /**
     * 关闭视频
     */
    public void close() {
        mMediaController.setPlayState(MediaController.PlayState.PAUSE);
        stopHideTimer(true);
        stopUpdateTimer();
        mSuperVideoView.pause();
        mSuperVideoView.stopPlayback();
        mSuperVideoView.setVisibility(GONE);
    }

    public boolean isAutoHideController() {
        return mAutoHideController;
    }

    public void setAutoHideController(boolean autoHideController) {
        mAutoHideController = autoHideController;
    }

    public SuperVideoPlayer(Context context) {
        super(context);
        initView(context);
    }

    public SuperVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public SuperVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        View.inflate(context, R.layout.super_vodeo_player_layout, this);
        mSuperVideoView = (SuperVideoView) findViewById(R.id.video_view);
        mMediaController = (MediaController) findViewById(R.id.controller);
        mProgressBarView = findViewById(R.id.progressbar);
        mCloseBtnView = findViewById(R.id.video_close_view);
        mTvBtnView = findViewById(R.id.video_share_tv_view);
        mDLNARootLayout = findViewById(R.id.rel_dlna_root_layout);

        mMediaController.setMediaControl(mMediaControl);
        mSuperVideoView.setOnTouchListener(mOnTouchVideoListener);

        setDLNAButton(false);
        setCloseButton(false);
        mDLNARootLayout.setVisibility(GONE);
        showProgressView(false);

        mCloseBtnView.setOnClickListener(mOnClickListener);
        mProgressBarView.setOnClickListener(mOnClickListener);

        mAllVideo = new ArrayList<>();
    }


    /**
     * 显示DLNA可以推送的按钮
     */
    private void setDLNAButton(boolean isShow) {
        mTvBtnView.setVisibility(isShow ? VISIBLE : INVISIBLE);
    }

    /**
     * 显示关闭视频的按钮
     *
     * @param isShow isShow
     */
    private void setCloseButton(boolean isShow) {
        mCloseBtnView.setVisibility(isShow ? VISIBLE : INVISIBLE);
    }

    /**
     * 更换清晰度地址时，续播
     */
    private void playVideoAtLastPos() {
        int playTime = mSuperVideoView.getCurrentPosition();
        mSuperVideoView.stopPlayback();
        loadAndPlay(mNowPlayVideo.getPlayUrl(), playTime);
    }

    /**
     * 加载并开始播放视频
     *
     * @param videoUrl videoUrl
     */
    private void loadAndPlay(VideoUrl videoUrl, int seekTime) {
        Log.e("TAG", "videoUrl:" + videoUrl);
        showProgressView(seekTime > 0);
        setCloseButton(true);
        if (TextUtils.isEmpty(videoUrl.getUrl())) {
            Log.e("TAG", "videoUrl should not be null");
            return;
        }
        mSuperVideoView.setOnPreparedListener(mOnPreparedListener);
        if (videoUrl.isOnlineVideo()) {
            mHandler.obtainMessage(MSG_FILE_DOWNLOAD_ING).sendToTarget();
        } else {
            Uri uri = Uri.parse(videoUrl.getUrl());
            mSuperVideoView.setVideoURI(uri);
            mSuperVideoView.setVisibility(VISIBLE);
            startPlayVideo(seekTime);
        }
    }

    /**
     * 播放视频
     * should called after setVideoPath()
     */
    private void startPlayVideo(int seekTime) {
        if (null == mUpdateTimer) resetUpdateTimer();
        resetHideTimer();
        mSuperVideoView.setOnCompletionListener(mOnCompletionListener);
        mSuperVideoView.start();
        if (seekTime > 0) {
            mSuperVideoView.seekTo(seekTime);
        }
        mMediaController.setPlayState(MediaController.PlayState.PLAY);
    }

    /**
     * 更新播放的进度时间
     */
    private void updatePlayTime() {
        int allTime = mSuperVideoView.getDuration();
        int playTime = mSuperVideoView.getCurrentPosition();
        mMediaController.setPlayProgressTxt(playTime, allTime);
    }

    /**
     * 更新播放进度条
     */
    private void updatePlayProgress() {
        int allTime = mSuperVideoView.getDuration();
        int playTime = mSuperVideoView.getCurrentPosition();
        int loadProgress = mSuperVideoView.getBufferPercentage();
        int progress = playTime * 100 / allTime;
        mMediaController.setProgressBar(progress, loadProgress);
    }

    /**
     * 显示loading圈
     *
     * @param isTransparentBg isTransparentBg
     */
    private void showProgressView(Boolean isTransparentBg) {
        mProgressBarView.setVisibility(VISIBLE);
        if (!isTransparentBg) {
            mProgressBarView.setBackgroundResource(android.R.color.black);
        } else {
            mProgressBarView.setBackgroundResource(android.R.color.transparent);
        }
    }


    private void showOrHideController() {
        mMediaController.closeAllSwitchList();
        if (mMediaController.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_exit_from_bottom);
            animation.setAnimationListener(new AnimationImp() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    mMediaController.setVisibility(View.GONE);
                }
            });
            mMediaController.startAnimation(animation);
        } else {
            mMediaController.setVisibility(View.VISIBLE);
            mMediaController.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_enter_from_bottom);
            mMediaController.startAnimation(animation);
            resetHideTimer();
        }
    }

    private void alwaysShowController() {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mMediaController.setVisibility(View.VISIBLE);
    }

    private void resetHideTimer() {
        if (!isAutoHideController()) return;
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        int TIME_SHOW_CONTROLLER = 4000;
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, TIME_SHOW_CONTROLLER);
    }

    private void stopHideTimer(boolean isShowController) {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mMediaController.clearAnimation();
        mMediaController.setVisibility(isShowController ? View.VISIBLE : View.GONE);
    }

    private void resetUpdateTimer() {
        mUpdateTimer = new Timer();
        int TIME_UPDATE_PLAY_TIME = 1000;
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_UPDATE_PLAY_TIME);
            }
        }, 0, TIME_UPDATE_PLAY_TIME);
    }

    private void stopUpdateTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    private void setController(IController controller) {
        mController = controller;
    }

    /**
     * 继续在本地播放
     */
    private synchronized void goOnPlayAtLocal() {
        showProgressView(true);
        new Thread() {
            @Override
            public void run() {
                final boolean isSuccess = mController.stop(mSelectDevice);
                Message message = new Message();
                message.what = MSG_EXIT_FORM_TV_RESULT;
                message.arg1 = isSuccess ? 1 : 0;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    private class AnimationImp implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    public interface VideoPlayCallbackImpl {
        void onCloseVideo();

        void onSwitchPageType();

        void onPlayFinish();
    }

}