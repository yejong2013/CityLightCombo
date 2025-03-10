package com.gold.kds517.citylightstv.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gold.kds517.citylightstv.R;
import com.gold.kds517.citylightstv.apps.Constants;
import com.gold.kds517.citylightstv.apps.MyApp;
import com.gold.kds517.citylightstv.dialog.OSDDlg;
import com.gold.kds517.citylightstv.dialog.PackageDlg;
import com.gold.kds517.citylightstv.models.MovieModel;
import com.gold.kds517.citylightstv.utils.Utils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeriesPlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener, IVLCVout.Callback,View.OnClickListener, IVLCVout.OnNewVideoLayoutListener{
    public int mHeight;
    public int mWidth;
    private MediaPlayer mMediaPlayer = null;
    LibVLC libvlc;
    public  SurfaceView surfaceView;
    SurfaceView remote_subtitles_surface;
    private SurfaceHolder holder;

    LinearLayout ly_play,ly_resolution,ly_audio,ly_subtitle,ly_audio_delay;
    RelativeLayout ly_header;
    ImageView img_play,image_icon;


    MediaPlayer.TrackDescription[] traks;
    MediaPlayer.TrackDescription[] subtraks;
    String[] resolutions = new String[]{"16:9", "4:3", "16:9"};
    int current_resolution = 0;
    boolean first = true;
    SeekBar seekBar;
    LinearLayout bottom_lay, def_lay;
    TextView title_txt, start_txt, end_txt;
    ImageView imageView;
    Handler mHandler = new Handler();
    Handler handler = new Handler();
    Runnable mTicker,rssTicker,delayTicker;
    String cont_url,title,stream_id,img,name,rss="";
    int dration_time = 0,pos,position,selected_item = 0,msg_time = 0;
    private OSDDlg osdDlg;
    private long delay_time;
    Handler delay_handler = new Handler();
    private boolean is_msg = false;
    List<MovieModel> movieModels;
    boolean is_create = true;
    List<String>  pkg_datas;
    boolean is_long =false,is_rss = false;
    Handler rssHandler = new Handler();
    TextView txt_rss;
    private FrameLayout mVideoSurfaceFrame = null;
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;
    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;
    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private static int CURRENT_SIZE = SURFACE_BEST_FIT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_player);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        pkg_datas = new ArrayList<>();
        pkg_datas.addAll(Arrays.asList(getResources().getStringArray(R.array.package_list2)));
        mVideoSurfaceFrame = findViewById(R.id.video_surface_frame);
        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable mRunnable = () -> updateVideoSurfaces();
                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.post(mRunnable);
                    }
                }
            };
        }
        mVideoSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
        surfaceView = findViewById(R.id.surface_view);
        surfaceView.setOnClickListener(view -> {
            if(bottom_lay.getVisibility()== View.VISIBLE){
                bottom_lay.setVisibility(View.GONE);
            }else {
                bottom_lay.setVisibility(View.VISIBLE);
                updateTimer();
            }
        });
        pos = MyApp.episode_pos;
        movieModels = MyApp.movieModels;
        stream_id = movieModels.get(pos).getStream_id();
        img = movieModels.get(pos).getStream_icon();
        name =movieModels.get(pos).getName();
        String type = movieModels.get(pos).getType();
        holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.RGBX_8888);
        def_lay = findViewById(R.id.def_lay);
        bottom_lay = findViewById(R.id.vod_bottom_lay);
        title_txt = findViewById(R.id.vod_channel_title);
        imageView = findViewById(R.id.vod_channel_img);
        start_txt = findViewById(R.id.vod_start_time);
        end_txt = findViewById(R.id.vod_end_time);
        seekBar = findViewById(R.id.vod_seekbar);
        seekBar.setOnSeekBarChangeListener(this);

        txt_rss = findViewById(R.id.txt_rss);
        txt_rss.setSingleLine(true);
        image_icon = findViewById(R.id.image_icon);
        Picasso.with(this).load(Constants.GetIcon(this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(image_icon);
        remote_subtitles_surface = findViewById(R.id.remote_subtitles_surface);
        remote_subtitles_surface.setZOrderMediaOverlay(true);
        remote_subtitles_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        ly_header = findViewById(R.id.ly_header);
        ly_audio = findViewById(R.id.ly_audio);
        ly_play = findViewById(R.id.ly_play);
        ly_resolution = findViewById(R.id.ly_resolution);
        ly_subtitle = findViewById(R.id.ly_subtitle);
        ly_audio_delay = findViewById(R.id.ly_audio_delay);

        ly_play.setOnClickListener(this);
        ly_resolution.setOnClickListener(this);
        ly_subtitle.setOnClickListener(this);
        ly_audio.setOnClickListener(this);
        ly_audio_delay.setOnClickListener(this);

        img_play = findViewById(R.id.img_play);

        title_txt.setText(name);
        cont_url = MyApp.instance.getIptvclient().buildSeriesStreamURL(MyApp.user,MyApp.pass,stream_id,type);
        title = name;
        try {
            Picasso.with(this).load(getIntent().getStringExtra("img"))
                    .placeholder(R.drawable.icon_default)
                    .error(R.drawable.icon_default)
                    .into(imageView);

        }catch (Exception e){
            Picasso.with(this).load(R.drawable.icon_default).into(imageView);
        }
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int SCREEN_HEIGHT = displayMetrics.heightPixels;
        int SCREEN_WIDTH = displayMetrics.widthPixels;
        holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        mHeight = displayMetrics.heightPixels;
        mWidth = displayMetrics.widthPixels;
        playVideo(cont_url);
        FullScreencall();
        new Thread(this::getRespond).start();
    }


    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mMediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (CURRENT_SIZE == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    mMediaPlayer.setScale(scale);
                    mMediaPlayer.setAspectRatio(null);
                } else {
                    mMediaPlayer.setScale(0);
                    mMediaPlayer.setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mMediaPlayer.setAspectRatio("16:9");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_4_3:
                mMediaPlayer.setAspectRatio("4:3");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(1);
                break;
        }
    }

    private void updateVideoSurfaces() {
        int sw = getWindow().getDecorView().getWidth();
        int sh = getWindow().getDecorView().getHeight();

        // sanity check
        if (sw * sh == 0) {
            return;
        }

        mMediaPlayer.getVLCVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceView.setLayoutParams(lp);
            lp = mVideoSurfaceFrame.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoSurfaceFrame.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surfaceView.setLayoutParams(lp);
        if (remote_subtitles_surface != null)
            remote_subtitles_surface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mVideoSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mVideoSurfaceFrame.setLayoutParams(lp);

        surfaceView.invalidate();
        if (remote_subtitles_surface != null)
            remote_subtitles_surface.invalidate();
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }

    private void getRespond(){
        String url = "";
        switch (MyApp.firstServer){
            case first:
                url=Constants.GetUrl1(this);
                break;
            case second:
                url=Constants.GetUrl2(this);
                break;
            case third:
                url=Constants.GetUrl3(this);
                break;
        }
        try{
            String response = MyApp.instance.getIptvclient().login(url);
            Log.e("response",response);
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("status")) {
                    JSONObject data_obj = object.getJSONObject("data");
                    String msg=data_obj.getString("message");
                    try {
                        msg_time = Integer.parseInt(data_obj.getString("message_time"));
                    }catch (Exception e){
                        msg_time = 20;
                    }
                    is_msg = !data_obj.getString("message_on_off").isEmpty() && data_obj.getString("message_on_off").equalsIgnoreCase("1");
                    if (msg.equals("")) msg=getString(R.string.app_name);
                    String finalMsg = msg;
                    runOnUiThread(()->{
                        String rss_feed = "                 "+ finalMsg +"                 ";
                        Paint paint = new Paint();
                        paint.setTextSize(25);
                        paint.setColor(Color.BLACK);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTypeface(Typeface.DEFAULT);
                        Rect result = new Rect();
                        paint.getTextBounds(rss_feed, 0, rss_feed.length(), result);
                        float width = paint.measureText(rss_feed, 0, rss_feed.length());
                        if(rss.equalsIgnoreCase(rss_feed)){
                            ly_header.setVisibility(View.GONE);
//                            image_icon.setVisibility(View.GONE);
//                            txt_rss.setVisibility(View.GONE);
                            is_rss = false;
                        }else {
                            rss =rss_feed;
                            is_rss = true;
                            ly_header.setVisibility(View.VISIBLE);
                        }

                        int divide = (MyApp.SCREEN_WIDTH)/Utils.dp2px(this,(int) width);
                        Log.e("divide",divide+"");
                        if(divide>1){
                            if(is_msg){
                                ly_header.setVisibility(View.VISIBLE);
                                txt_rss.setText(rss);
                                Animation bottomToTop = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
                                txt_rss.clearAnimation();
                                txt_rss.startAnimation(bottomToTop);
                            }else {
                                ly_header.setVisibility(View.GONE);
                            }
                        }else {
                            if(is_msg){
                                ly_header.setVisibility(View.VISIBLE);
                                for(int i =0;i<divide+1;i++){
                                    rss_feed += rss_feed;
                                }
                                Log.e("rss2",rss);
//                            txt_rss.setText(rss);
//                            txt_rss.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.marquee1));
                                txt_rss.setSelected(true);
                                txt_rss.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                txt_rss.setText(rss_feed);
                            }else {
                                ly_header.setVisibility(View.GONE);
                            }
                        }


                        rssTimer();
                    });
                } else {
                    Toast.makeText(this, "Server Error!", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    int rss_time;
    private void rssTimer() {
        rss_time = msg_time;
        rssTicker = () -> {
            if (rss_time < 1) {
                txt_rss.setText("");
                txt_rss.setBackgroundResource(R.color.trans_parent);
                ly_header.setVisibility(View.GONE);
                image_icon.setVisibility(View.GONE);
                return;
            }
            runRssTicker();
        };
        rssTicker.run();
    }

    private void runRssTicker() {
        rss_time --;
        long next = SystemClock.uptimeMillis() + 1000;
        rssHandler.postAtTime(rssTicker, next);
    }

    private void playVideo(String path) {
        Log.e("url",path);
        if(def_lay.getVisibility()== View.VISIBLE)def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
        toggleFullscreen(true);
//        toggleFullscreen(true);
        try {

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("0");//this option is used to show the first subtitle track
            options.add("--subsdec-encoding");

            libvlc = new LibVLC(this, options);

            // Creating media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            mMediaPlayer.setAspectRatio(MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT);


            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            if (remote_subtitles_surface != null)
                vout.setSubtitlesView(remote_subtitles_surface);

            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.setWindowSize(mWidth, mHeight);
            vout.addCallback(this);
            vout.attachViews(this);
//            vout.setSubtitlesView(tv_subtitle);


            Log.e("VideoPlay",path);
            Media m = new Media(libvlc, Uri.parse(path));
            mMediaPlayer.setMedia(m);
            m.release();
            mMediaPlayer.play();
//            mediaSeekTo();
            updateProgressBar();
            updateTimer();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }

    }
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mMediaPlayer != null) {
                if (traks == null && subtraks == null) {
                    first = false;
                    traks = mMediaPlayer.getAudioTracks();
                    subtraks = mMediaPlayer.getSpuTracks();
                }
                long totalDuration = mMediaPlayer.getLength();
                long currentDuration = mMediaPlayer.getTime();
                end_txt.setText("" + Utils.milliSecondsToTimer(totalDuration));
                start_txt.setText("" + Utils.milliSecondsToTimer(currentDuration));
                int progress = (int) (Utils.getProgressPercentage(currentDuration, totalDuration));
                seekBar.setProgress(progress);
                mHandler.postDelayed(this, 500);
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        if (!is_create) {
            if (libvlc != null) {
                releaseMediaPlayer();
                surfaceView = null;
            }
            surfaceView = (SurfaceView) findViewById(R.id.surface_view);
            holder = surfaceView.getHolder();
            holder.setFormat(PixelFormat.RGBX_8888);
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            int SCREEN_HEIGHT = displayMetrics.heightPixels;
            int SCREEN_WIDTH = displayMetrics.widthPixels;
            holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            mHeight = displayMetrics.heightPixels;
            mWidth = displayMetrics.widthPixels;
            playVideo(cont_url);
        } else {
            is_create = false;
        }
    }
    private void toggleFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("AUDIO_TRACK", 0);
        editor.commit();

        SharedPreferences pref2 = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = pref2.edit();
        editor1.putInt("SUB_TRACK", 0);
        releaseMediaPlayer();
    }
    @Override
    protected void onUserLeaveHint()
    {
        releaseMediaPlayer();
        finish();
        super.onUserLeaveHint();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }
    private void releaseMediaPlayer() {
        if (libvlc == null)
            return;
            mMediaPlayer.stop();
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.removeCallback(this);
            vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mWidth = 0;
        mHeight = 0;
    }
    private MediaPlayer.EventListener mPlayerListener = new MediaPlayerListener(this);

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<SeriesPlayActivity> mOwner;

        private MediaPlayerListener(SeriesPlayActivity owner) {
            mOwner = new WeakReference<>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            SeriesPlayActivity player = mOwner.get();
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    player.releaseMediaPlayer();
                    if(player.pos<player.movieModels.size()-1){
                        MyApp.episode_pos = player.pos+1;
                        player.pos = player.pos+1;
                        player.recreate();
                    }else {
                        player.releaseMediaPlayer();
                        player.finish();
                    }
                    break;
                case MediaPlayer.Event.Playing:
//                    Toast.makeText(player, "Playing", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
//                    Toast.makeText(player, "Stop", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.Buffering:
//                    Toast.makeText(player, "Buffering", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.EncounteredError:
//                    Toast.makeText(player, "Error", Toast.LENGTH_SHORT).show();
                    player.def_lay.setVisibility(View.VISIBLE);
                    break;

                case MediaPlayer.Event.TimeChanged:
                    break;
                case MediaPlayer.Event.PositionChanged:
                    //Log.d(TAG, "PositionChanged");
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_audio:
                if (traks != null) {
                    if (traks.length > 0) {
                        showAudioTracksList();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.ly_subtitle:
                if (subtraks != null) {
                    if (subtraks.length > 0) {
                        showSubTracksList();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.ly_resolution:
                current_resolution++;
                if (current_resolution == resolutions.length)
                    current_resolution = 0;

                mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
                break;

            case R.id.ly_play:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    img_play.setImageResource(R.drawable.exo_play);
                } else {
                    mMediaPlayer.play();
                    img_play.setImageResource(R.drawable.exo_pause);
                }
                if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                updateTimer();
                break;
            case R.id.ly_audio_delay:
                osdDlg = new OSDDlg(this, delay_time / 1000, (dialog, episode_num) -> {
                    if(mMediaPlayer!=null){
                        delay_time = episode_num*1000;
                        mMediaPlayer.setAudioDelay(delay_time);
                    }
                });
                osdDlg.show();
                delayTimer();
                break;
        }
    }

    private void delayTimer(){
        delay_handler.removeCallbacks(delayTicker);
        updateDelayTimer();
    }
    int delay_max;
    private void updateDelayTimer(){
        delay_max = 5;
        delayTicker = () -> {
            Log.e("delay_time",String.valueOf(delay_max));
            if(delay_max<1){
                if(osdDlg.isShowing()){
                    osdDlg.dismiss();
                }
                return;
            }
            runNextDelayTicker();
        };
        delayTicker.run();
    }

    private void runNextDelayTicker(){
        delay_max--;
        long next = SystemClock.uptimeMillis() + 1000;
        delay_handler.postAtTime(delayTicker, next);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        long totalDuration = mMediaPlayer.getLength();
        int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);
        mMediaPlayer.setTime(currentPosition);
        updateProgressBar();
    }


    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }


    private void updateTimer() {
        handler.removeCallbacks(mTicker);
        startTimer();
    }

    int maxTime;
    private void startTimer() {
        maxTime = 10;
        mTicker = new Runnable() {
            public void run() {
                if (maxTime < 1) {
                    if (bottom_lay.getVisibility() == View.VISIBLE)
                        bottom_lay.setVisibility(View.GONE);
                    return;
                }
                runNextTicker();
            }
        };
        mTicker.run();
    }

    private void runNextTicker() {
        maxTime --;
        long next = SystemClock.uptimeMillis() + 1000;
        handler.postAtTime(mTicker, next);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            long curr_pos = mMediaPlayer.getTime();
            long max_pos = mMediaPlayer.getLength();
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                            img_play.setImageResource(R.drawable.exo_play);
                        } else {
                            mMediaPlayer.play();
                            img_play.setImageResource(R.drawable.exo_pause);
                        }
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        updateTimer();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        dration_time += 30;
                        if (curr_pos < dration_time * 1000)
                            mMediaPlayer.setTime(1);
                        else {
                            long st = (long) (curr_pos - (long) dration_time * 1000);
                            mMediaPlayer.setTime(st);
                        }
                        dration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        dration_time += 30;
                        if (max_pos < dration_time * 1000)
                            mMediaPlayer.setTime((long) (max_pos - 10));
                        else mMediaPlayer.setTime((long) (curr_pos + (long) dration_time * 1000));
                        dration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        if(bottom_lay.getVisibility()==View.VISIBLE){
                            bottom_lay.setVisibility(View.GONE);
                            return true;
                        }
                        releaseMediaPlayer();
                        finish();
                        break;
                    case KeyEvent.KEYCODE_MENU:
                        PackageDlg packageDlg = new PackageDlg(SeriesPlayActivity.this, pkg_datas, (dialog, position) -> {
                            dialog.dismiss();
                            is_long = false;
                            switch (position) {
                                case 0:
                                    if (subtraks != null) {
                                        if (subtraks.length > 0) {
                                            showSubTracksList();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                                    }
                                    break;
                                case 1:
                                    if (traks != null) {
                                        if (traks.length > 0) {
                                            showAudioTracksList();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                                    }
                                    break;
                                case 2:
                                    current_resolution++;
                                    if (current_resolution == resolutions.length)
                                        current_resolution = 0;

                                    mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
                                    break;
                                case 3:
                                    osdDlg = new OSDDlg(SeriesPlayActivity.this,  delay_time/1000, (dialog1, episode_num) -> {
                                        if(mMediaPlayer!=null){
                                            delay_time = episode_num*1000;
                                            mMediaPlayer.setAudioDelay(delay_time);
                                        }
                                    });
                                    osdDlg.show();
                                    delayTimer();
                                    break;
                            }
                        });
                        packageDlg.show();
                        break;
                }
            }

        }catch (Exception e){

        }
        return super.dispatchKeyEvent(event);
    }

    public void FullScreencall() {
        if(Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else  {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void showAudioTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SeriesPlayActivity.this);
        builder.setTitle("Audio track");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < traks.length; i++) {
            names.add(traks[i].name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("AUDIO_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_item = which;
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("AUDIO_TRACK", selected_item);
                editor.commit();

                mMediaPlayer.setAudioTrack(traks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSubTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SeriesPlayActivity.this);
        builder.setTitle("Subtitle");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < subtraks.length; i++) {
            names.add(subtraks[i].name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("SUB_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_item = which;
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("SUB_TRACK", selected_item);
                editor.commit();
                mMediaPlayer.setSpuTrack(subtraks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
