package com.gold.kds517.citylightstv.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gold.kds517.citylightstv.R;
import com.gold.kds517.citylightstv.adapter.MainListAdapter;
import com.gold.kds517.citylightstv.apps.Constants;
import com.gold.kds517.citylightstv.apps.MyApp;
import com.gold.kds517.citylightstv.dialog.PackageDlg;
import com.gold.kds517.citylightstv.dialog.PinDlg;
import com.gold.kds517.citylightstv.dialog.RecordingDlg;
import com.gold.kds517.citylightstv.dialog.SearchDlg;
import com.gold.kds517.citylightstv.dialog.StopRecordingDlg;
import com.gold.kds517.citylightstv.ijklib.widget.media.AndroidMediaController;
import com.gold.kds517.citylightstv.ijklib.widget.media.IjkVideoView;
import com.gold.kds517.citylightstv.listner.SimpleGestureFilter;
import com.gold.kds517.citylightstv.models.EPGChannel;
import com.gold.kds517.citylightstv.models.EPGEvent;
import com.gold.kds517.citylightstv.models.FullModel;
import com.gold.kds517.citylightstv.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.gold.kds517.citylightstv.apps.MyApp.INPUT_FILE;
import static com.gold.kds517.citylightstv.apps.MyApp.INPUT_NAME;
import static com.gold.kds517.citylightstv.apps.MyApp.is_recording;
import static java.lang.Integer.parseInt;

public class PreviewChannelIJKActivity extends AppCompatActivity implements  AdapterView.OnItemClickListener, View.OnClickListener, SimpleGestureFilter.SimpleGestureListener, AdapterView.OnItemLongClickListener,
   IMediaPlayer.OnCompletionListener,IMediaPlayer.OnErrorListener{
    private int mVideoWidth;
    private int mVideoHeight;
    private SimpleGestureFilter detector;
    Context context = null;
    private IjkVideoView surfaceView;
    private AndroidMediaController mMediaController;
    private TextView txt_num;
    private TableLayout mHudView;
    LinearLayout def_lay,ly_bottom,ly_resolution,ly_audio,ly_subtitle;
    RelativeLayout ly_header;

    boolean first = true;

    ImageView btn_back,btn_guide,image_clock,image_star,channel_logo,logo,image_icon;
    RelativeLayout ly_surface,main_lay;
    List<FullModel> full_datas;
    List<EPGChannel> channels;
    List<String> pkg_datas;
    EPGChannel sel_model;
    List<EPGEvent> epgModelList;
    int channel_pos,sub_pos=0,epg_pos,preview_pos,move_pos = 0,osd_time,pro,msg_time = 0;
    MainListAdapter adapter;
    ListView channel_list;
    TextView txt_time,txt_category,txt_title,txt_dec,txt_channel,txt_date,txt_time_passed,txt_remain_time,txt_last_time,txt_current_dec,txt_next_dec,
            firstTime,firstTitle,secondTime,secondTitle,thirdTime,thirdTitle,fourthTime,fourthTitle,txt_progress,num_txt;
    TextView txt_rss;
    SeekBar seekbar;
    String contentUri,mStream_id,key = "",rss = "";
    Handler mHandler = new Handler();
    Handler moveHandler = new Handler();
    Handler mEpgHandler = new Handler();
    Handler rssHandler = new Handler();
    Handler removeHamdler = new Handler();
    Runnable mTicker,moveTicker,mEpgTicker,rssTicker;
    boolean is_full = false,is_up = false,is_create= true,is_rss = false,is_msg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        setContentView(R.layout.activity_preview_ijk_channel);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MyApp.is_welcome = false;
        osd_time = (int) MyApp.instance.getPreference().get(Constants.OSD_TIME);
        detector = new SimpleGestureFilter(this, PreviewChannelIJKActivity.this);
        context = this;
        pkg_datas = new ArrayList<>();
        pkg_datas.addAll(Arrays.asList(getResources().getStringArray(R.array.package_list_ijk)));
        main_lay = (RelativeLayout)findViewById(R.id.main_lay);
        main_lay.setOnClickListener(this);
        MyApp.is_first = true;
        full_datas = MyApp.fullModels_filter;
        channel_pos = (int) MyApp.instance.getPreference().get(Constants.getCHANNEL_POS());
        ly_bottom = findViewById(R.id.ly_bottom);
        channel_list = findViewById(R.id.channel_list);
        channel_list.setOnItemClickListener(this);
        channel_list.setOnItemLongClickListener(this);
        btn_back = findViewById(R.id.btn_back);
        btn_guide = findViewById(R.id.btn_guide);
        image_clock = findViewById(R.id.image_clock);
        image_star = findViewById(R.id.image_star);
        channel_logo = findViewById(R.id.channel_logo);
        btn_back.setOnClickListener(this);
        btn_guide.setOnClickListener(this);
        txt_time = findViewById(R.id.txt_time);
        txt_category = findViewById(R.id.txt_category);
        txt_category.setText(MyApp.maindatas.get(channel_pos));
        txt_title = findViewById(R.id.txt_title);
        txt_dec = findViewById(R.id.txt_dec);
        txt_channel = findViewById(R.id.txt_channel);
        txt_date = findViewById(R.id.txt_date);
        txt_time_passed = findViewById(R.id.txt_time_passed);
        txt_remain_time = findViewById(R.id.txt_remain_time);
        txt_last_time = findViewById(R.id.txt_last_time);
        txt_current_dec = findViewById(R.id.txt_current_dec);
        txt_next_dec = findViewById(R.id.txt_next_dec);
        firstTime = findViewById(R.id.txt_firstTime);
        firstTitle = findViewById(R.id.txt_firstTitle);
        secondTime  = findViewById(R.id.secondTime);
        secondTitle = findViewById(R.id.secondTitle);
        thirdTime = findViewById(R.id.thirdTime);
        thirdTitle = findViewById(R.id.thirdTitle);
        fourthTime = findViewById(R.id.fourthTime);
        fourthTitle = findViewById(R.id.fourthTitle);
        txt_progress = findViewById(R.id.txt_progress);
        num_txt = findViewById(R.id.txt_num);
        txt_progress.setVisibility(View.GONE);
        seekbar = findViewById(R.id.seekbar);
        seekbar.setMax(100);
        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();
        def_lay = findViewById(R.id.def_lay);
        ly_surface = findViewById(R.id.ly_surface);
        ly_surface.setOnClickListener(this);

        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        mMediaController = new AndroidMediaController(this, false);
        surfaceView = findViewById(R.id.surface_view);

        ly_header = findViewById(R.id.ly_header);
        ly_audio = findViewById(R.id.ly_audio);
        ly_resolution = findViewById(R.id.ly_resolution);
        ly_subtitle = findViewById(R.id.ly_subtitle);

        ly_subtitle.setOnClickListener(this);
        ly_resolution.setOnClickListener(this);
        ly_audio.setOnClickListener(this);

        txt_rss = findViewById(R.id.txt_rss);
        txt_rss.setSingleLine(true);

        txt_num = findViewById(R.id.toast_text_view);
        mHudView = findViewById(R.id.hud_view);

        findViewById(R.id.ly_fav).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);
        findViewById(R.id.ly_back).setOnClickListener(this);
        findViewById(R.id.ly_guide).setOnClickListener(this);
        findViewById(R.id.ly_tv_schedule).setOnClickListener(this);
        findViewById(R.id.ly_rec).setOnClickListener(this);
        logo = findViewById(R.id.logo);
        Picasso.with(this).load(Constants.GetIcon(this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(logo);

        logo.setVisibility(View.GONE);
        image_icon = findViewById(R.id.image_icon);

        Picasso.with(this).load(Constants.GetIcon(this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(image_icon);
        ViewGroup.LayoutParams params = ly_surface.getLayoutParams();
        params.height = MyApp.SURFACE_HEIGHT;
        params.width = MyApp.SURFACE_WIDTH;
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        mVideoHeight = displayMetrics.heightPixels;
        mVideoWidth = displayMetrics.widthPixels;

        Log.e("height", String.valueOf(MyApp.SCREEN_HEIGHT));
        if(MyApp.SCREEN_HEIGHT==720){
            setMargins(ly_surface,0,MyApp.top_margin- Utils.dp2px(this,5),MyApp.right_margin+Utils.dp2px(this,5),0);
        }else if(MyApp.SCREEN_HEIGHT==1440){
            params.height = (int) (MyApp.SURFACE_HEIGHT*0.9);
            params.width = (int) (MyApp.SURFACE_WIDTH*0.9);
            setMargins(ly_surface,0,MyApp.top_margin-Utils.dp2px(this,10),MyApp.right_margin,0);
        }else {
            setMargins(ly_surface,0,MyApp.top_margin,MyApp.right_margin,0);
        }

        ly_surface.setLayoutParams(params);
        FullScreencall();
        channels = full_datas.get(channel_pos).getChannels();
        if(channels==null || channels.size()==0){
            Toast.makeText(this,"This category do not have channels", Toast.LENGTH_SHORT).show();
            return;
        }
        adapter = new MainListAdapter(this,channels);
        channel_list.setAdapter(adapter);
        channel_list.requestFocus();
        mStream_id = channels.get(sub_pos).getStream_id();
//        new Thread(this::getEpg).start();
//        showEpg(channels.get(sub_pos));
        new Thread(()-> getEpg(channels.get(sub_pos))).start();
        playChannel();
        new Thread(this::getRespond).start();
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

        runOnUiThread(()->{
            channel_list.requestFocus();
            channel_list.setSelection(sub_pos);
            channel_list.performClick();
        });
    }
    int rss_time;
    private void rssTimer() {
        rss_time = msg_time;
        rssTicker = () -> {
            if (rss_time < 1) {
                txt_rss.setText("");
                txt_rss.setBackgroundResource(R.color.trans_parent);
                ly_header.setVisibility(View.GONE);
                logo.setVisibility(View.VISIBLE);
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


//    private void getEpg(){
//        mHandler.removeCallbacks(mUpdateTimeTask);
//        try {
//            String map = MyApp.instance.getIptvclient().getShortEPG(MyApp.user,MyApp.pass,
//                    mStream_id,4);
//            Log.e(getClass().getSimpleName(),map);
//            map=map.replaceAll("[^\\x00-\\x7F]", "");
//            if (!map.contains("null_error_response")){
//                JSONObject jsonObject = new JSONObject(map);
//                JSONArray maps = (JSONArray) jsonObject.get("epg_listings");
//                epgModelList = new ArrayList<>();
//                if(maps!=null && maps.length()>0){
//                    for(int i = 0;i<maps.length();i++){
//                        try {
//                            JSONObject e_p = (JSONObject) maps.get(i);
//                            EpgModel epgModel = new EpgModel();
//                            epgModel.setId((String)e_p.get("id"));
//                            epgModel.setCh_id((String)e_p.get("channel_id"));
//                            epgModel.setCategory((String)e_p.get("epg_id"));
//                            epgModel.setT_time((String)e_p.get("start"));
//                            epgModel.setT_time_to((String)e_p.get("end"));
//                            byte[] desc_byte = Base64.decode((String)e_p.get("description"), Base64.DEFAULT);
//                            String desc = new String(desc_byte);
//                            epgModel.setDescr(desc);
//                            byte[] title_byte = Base64.decode((String)e_p.get("title"), Base64.DEFAULT);
//                            String title = new String(title_byte);
//                            epgModel.setName(title);
//                            epgModel.setStart_timestamp(e_p.get("start_timestamp").toString());
//                            epgModel.setStop_timestamp(e_p.get("stop_timestamp").toString());
//                            int duration = ((Integer.parseInt(e_p.get("stop_timestamp").toString())) - (Integer.parseInt(e_p.get("start_timestamp").toString())));
//                            epgModel.setDuration(duration);
//                            if(e_p.has("has_archive")) {
//                                Double d = Double.parseDouble(e_p.get("has_archive").toString());
//                                epgModel.setMark_archive(d.intValue());
//                            }
//                            epgModelList.add(epgModel);
//                        }catch (Exception e){
//                            Log.e("error","epg_parse_error");
//                        }
//                    }
//                }
//                runOnUiThread(()->{
//                    if(is_full){
//                        MyApp.is_first = true;
//                        mHandler.removeCallbacks(mUpdateTimeTask);
//                        updateProgressBar();
//                        ly_bottom.setVisibility(View.VISIBLE);
//                        if(channels.get(sub_pos).getStream_icon()!=null && !channels.get(sub_pos).getStream_icon().isEmpty()){
//                            Picasso.with(this).load(channels.get(sub_pos).getStream_icon())
//                                    .into(channel_logo);
//                            channel_logo.setVisibility(View.VISIBLE);
//                        }else {
//                            channel_logo.setVisibility(View.GONE);
//                        }
//                        listTimer();
//                    }
//                    printEpgData();
//                });
//            }
//        }catch (Exception e){
//
//        }
//    }
    int epg_time;
//    int i = 0;
    private void EpgTimer(){
        epg_time = 1;
        mEpgTicker = () -> {
            mEpgHandler.removeCallbacks(mEpgTicker);
            if (epg_time == 0) {
                new Thread(()-> getEpg(channels.get(sub_pos))).start();
                return;
            }
            runNextEpgTicker();
        };
        mEpgTicker.run();
    }

    private void runNextEpgTicker() {
        epg_time--;
        long next = SystemClock.uptimeMillis() + 1000;
        mEpgHandler.postAtTime(mEpgTicker, next);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_surface:
                if(!is_full && txt_progress.getVisibility()== View.GONE){
                    is_full = true;
                    ViewGroup.LayoutParams params = ly_surface.getLayoutParams();
                    params.height = MyApp.SCREEN_HEIGHT+Utils.dp2px(getApplicationContext(),50);
                    params.width = MyApp.SCREEN_WIDTH+Utils.dp2px(getApplicationContext(),50);
                    ly_surface.setPadding(Utils.dp2px(this,0),Utils.dp2px(this,0),Utils.dp2px(this,0),Utils.dp2px(this,0));
                    setMargins(ly_surface,Utils.dp2px(this,0),Utils.dp2px(this,0),Utils.dp2px(this,0),Utils.dp2px(this,0));
                    ly_surface.setLayoutParams(params);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mHandler.removeCallbacks(mUpdateTimeTask);
                            updateProgressBar();
                            ly_bottom.setVisibility(View.VISIBLE);
                            listTimer();
                        }
                    }, 2000);
                }else if(is_full){
                    if(ly_bottom.getVisibility()== View.GONE){
                        if(pro>99){
                            mEpgHandler.removeCallbacks(mEpgTicker);
                            EpgTimer();
                        }
                        ly_bottom.setVisibility(View.VISIBLE);
                    }else {
                        ly_bottom.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.ly_back:
                if(!is_full){
                    MyApp.key = false;
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    releaseMediaPlayer();
                    finish();
                }
                break;
            case R.id.ly_guide:
                if(!is_full && txt_progress.getVisibility()== View.GONE){
                    if(channels==null || channels.size()==0){
                        return;
                    }
                    channel_list.setFocusable(true);
                    MyApp.key = false;
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    releaseMediaPlayer();
                    String channel_name = channels.get(sub_pos).getName();
                    Intent intent = new Intent(this,GuideActivityExtra.class);
                    intent.putExtra("stream_id",mStream_id);
                    intent.putExtra("channel_name",channel_name);
//                    intent.putExtra("channel",channels.get(sub_pos));
                    MyApp.epgChannel = channels.get(sub_pos);
                    startActivity(intent);
                }
                break;
            case R.id.ly_audio:
                break;

            case R.id.ly_subtitle:
                break;
            case R.id.ly_resolution:
                surfaceView.toggleAspectRatio();
                break;
            case R.id.ly_fav:
                if (full_datas.get(channel_pos).getChannels().get(sub_pos).is_favorite()) {
                    pkg_datas.set(0, "Add to Fav");
                    image_star.setVisibility(View.GONE);
                    full_datas.get(channel_pos).getChannels().get(sub_pos).setIs_favorite(false);
                    boolean is_exist = false;
                    int pp = 0;
                    for (int i = 0; i < Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().size(); i++) {
                        if (Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().get(i).getName().equals(full_datas.get(channel_pos).getChannels().get(sub_pos).getName())) {
                            is_exist = true;
                            pp = i;
                        }
                    }
                    if (is_exist)
                        Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().remove(pp);
                    MyApp.instance.getPreference().put(Constants.getFavInfo(), Constants.getListStrFromListEpg(Constants.getFavFullModel(MyApp.fullModels_filter).getChannels()));
                } else {
                    image_star.setVisibility(View.VISIBLE);
                    full_datas.get(channel_pos).getChannels().get(sub_pos).setIs_favorite(true);
                    Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().add(full_datas.get(channel_pos).getChannels().get(sub_pos));
                    MyApp.instance.getPreference().put(Constants.getFavInfo(), Constants.getListStrFromListEpg(Constants.getFavFullModel(MyApp.fullModels_filter).getChannels()));
                    pkg_datas.set(0, "Remove from Fav");
                }
                adapter.selectItem(sub_pos);
                listTimer();
                break;
            case R.id.btn_search:
                SearchDlg searchDlg = new SearchDlg(PreviewChannelIJKActivity.this, (dialog, sel_Channel) -> {
                    dialog.dismiss();
                    FullScreencall();
                    for(int i = 0;i<channels.size();i++){
                        if(channels.get(i).getName().equalsIgnoreCase(sel_Channel.getName())){
                            sub_pos = i;
                            break;
                        }
                    }
                    scrollToLast(channel_list, sub_pos);
                    epg_pos = sub_pos;
                    preview_pos = sub_pos;
                    adapter.selectItem(sub_pos);
                    MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
                    mStream_id = channels.get(sub_pos).getStream_id();
                    mEpgHandler.removeCallbacks(mEpgTicker);
                    EpgTimer();
                    playChannel();
                });
                searchDlg.show();
                break;
            case R.id.ly_tv_schedule:
                startActivity(new Intent(this, WebViewActivity.class));
                break;
            case R.id.ly_rec:
                if(is_recording){
                    ShowStopDlg();
                }else {
                    ShowRecordingDlg();
                }
        }
    }

    private void ShowRecordingDlg(){
        INPUT_NAME = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+channels.get(sub_pos).getName().replaceAll("\\s+","");
        Log.e("file_name",INPUT_NAME);
        RecordingDlg recordingDlg = new RecordingDlg(this, INPUT_NAME, new RecordingDlg.DialogUpdateListener() {
            @Override
            public void OnUpdateNowClick(Dialog dialog, String channel_name, int duration,String time,boolean is_checked) {
                dialog.dismiss();
                INPUT_NAME = channel_name;
                RecordingChannels(duration,time,is_checked);
            }

            @Override
            public void OnUpdateSkipClick(Dialog dialog, String channel_name, int duration,String time,boolean is_checked) {
                dialog.dismiss();
            }
        });
        recordingDlg.show();
    }


    private void ShowStopDlg(){
        StopRecordingDlg stopRecordingDlg = new StopRecordingDlg(this, "",new StopRecordingDlg.DialogUpdateListener() {
            @Override
            public void OnUpdateNowClick(Dialog dialog) {
                dialog.dismiss();
                StopRecord();
            }

            @Override
            public void OnUpdateSkipClick(Dialog dialog) {
                dialog.dismiss();
            }
        });
        stopRecordingDlg.show();
    }

    private void RecordingChannels(int duration,String time,boolean is_checked){
        is_recording = true;
        INPUT_FILE = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                mStream_id,"ts");
        MyApp.instance.scheduleJob(duration,time,is_checked);
    }

    private void StopRecord(){
        is_recording = false;
        MyApp.instance.stopMyJob();
    }
    private void scrollToLast(final ListView listView, final int position) {
        listView.post(() -> listView.setSelection(position));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if(channels.get(preview_pos).getStream_id().equalsIgnoreCase(channels.get(position).getStream_id())){
            ly_surface.setVisibility(View.VISIBLE);
            is_full = true;
            ViewGroup.LayoutParams params = ly_surface.getLayoutParams();
            params.height = MyApp.SCREEN_HEIGHT+Utils.dp2px(getApplicationContext(),50);
            params.width = MyApp.SCREEN_WIDTH+Utils.dp2px(getApplicationContext(),50);
            ly_surface.setPadding(Utils.dp2px(this,0),Utils.dp2px(this,0),Utils.dp2px(this,0),Utils.dp2px(this,0));
            setMargins(ly_surface,Utils.dp2px(this,0),Utils.dp2px(this,0),Utils.dp2px(this,0),Utils.dp2px(this,0));
            ly_surface.setLayoutParams(params);
            mHandler.removeCallbacks(mUpdateTimeTask);
            updateProgressBar();
            ly_bottom.setVisibility(View.VISIBLE);
            listTimer();
        }else {
            MyApp.is_first = true;
            sub_pos = position;
            epg_pos = sub_pos;
            preview_pos = sub_pos;
            adapter.selectItem(sub_pos);
            MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
            mStream_id = channels.get(sub_pos).getStream_id();
            mEpgHandler.removeCallbacks(mEpgTicker);
            EpgTimer();
            playChannel();
//            rssHandler.removeCallbacks(rssTicker);
//            new Thread(this::getRespond).start();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
    private Runnable mUpdateTimeTask = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            if (surfaceView != null) {
                long wrongMedialaanTime = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
                long totalDuration = System.currentTimeMillis()+wrongMedialaanTime;
                if(epgModelList!=null && epgModelList.size()>0){
                    try {
                        long millis =epgModelList.get(0).getStartTime().getTime()+wrongMedialaanTime+Constants.SEVER_OFFSET;
                        long mills_to = epgModelList.get(0).getEndTime().getTime()+wrongMedialaanTime+Constants.SEVER_OFFSET;
                        if(totalDuration>millis){
                            txt_title.setText(new String(Base64.decode(epgModelList.get(0).getTitle(),Base64.DEFAULT)));
                            txt_dec.setText(new String(Base64.decode(epgModelList.get(0).getDec(),Base64.DEFAULT)));
                            try {
                                txt_channel.setText(channels.get(sub_pos).getNum() + " " + channels.get(sub_pos).getName());
                            }catch (Exception e1){
                                txt_channel.setText("    ");
                                final Handler handler = new Handler();
                                handler.postDelayed(() -> txt_channel.setText(channels.get(sub_pos).getNum() + " " + channels.get(sub_pos).getName()), 5000);
                            }
                            txt_date.setText(Constants.epg_format.format(new Date()));
                            int pass_min = (int) ((totalDuration - millis)/(1000*60));
                            int remain_min = (int)(mills_to-totalDuration)/(1000*60);
                            int progress = (int) pass_min*100/(pass_min+remain_min);
                            pro  = progress;
                            seekbar.setProgress(progress);
                            txt_time_passed.setText("Started " + pass_min +" mins ago");
                            txt_remain_time.setText("+"+remain_min+" min");
                            txt_last_time.setText(Constants.clockFormat.format(new Date(mills_to-wrongMedialaanTime)));
                            txt_current_dec.setText(new String(Base64.decode(epgModelList.get(0).getTitle(),Base64.DEFAULT)));
                            txt_next_dec.setText(new String(Base64.decode(epgModelList.get(1).getTitle(),Base64.DEFAULT)));
                            if(channels.get(sub_pos).is_favorite()){
                                image_star.setVisibility(View.VISIBLE);
                            }else {
                                image_star.setVisibility(View.GONE);
                            }
                            if(channels.get(sub_pos).getTv_archive().equalsIgnoreCase("1")){
                                image_clock.setVisibility(View.VISIBLE);
                            }else {
                                image_clock.setVisibility(View.GONE);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    txt_title.setText("No Information");
                    txt_dec.setText("No Information");
                    try {
                        txt_channel.setText(channels.get(sub_pos).getNum() + " " + channels.get(sub_pos).getName());
                    }catch (Exception e2){
                        txt_channel.setText("    ");
                        final Handler handler = new Handler();
                        handler.postDelayed(() -> txt_channel.setText(channels.get(sub_pos).getNum() + " " + channels.get(sub_pos).getName()), 5000);
                    }
                    txt_date.setText(Constants.epg_format.format(new Date()));
                    txt_time_passed.setText("      mins ago");
                    txt_remain_time.setText("      min");
                    txt_last_time.setText("         ");
                    seekbar.setProgress(0);
                    txt_current_dec.setText("No Information");
                    txt_next_dec.setText("No Information");
                }
            }
            mHandler.postDelayed(this, 500);
        }
    };

    @Override
    public void onSwipe(int direction) {

    }

    @Override
    public void onDoubleTap() {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }
    private void printEpgData(){
        Log.e("printdata","true "+epgModelList.size());
        if(txt_progress.getVisibility()== View.GONE){
            if(epgModelList.size()>0) {
                firstTime.setText(Constants.Offset(true,epgModelList.get(0).getStartTime()));
                firstTitle.setText(new String(Base64.decode(epgModelList.get(0).getTitle(),Base64.DEFAULT)));
                if(epgModelList.size()>1){
                    secondTime.setText(Constants.Offset(true,epgModelList.get(1).getStartTime()));
                    secondTitle.setText(new String(Base64.decode(epgModelList.get(1).getTitle(),Base64.DEFAULT)));
                }

                if(epgModelList.size()>2){
                    thirdTime.setText(Constants.Offset(true,epgModelList.get(2).getStartTime()));
                    thirdTitle.setText(new String(Base64.decode(epgModelList.get(2).getTitle(),Base64.DEFAULT)));
                }

                if(epgModelList.size()>3){
                    fourthTime.setText(Constants.Offset(true,epgModelList.get(3).getStartTime()));
                    fourthTitle.setText(new String(Base64.decode(epgModelList.get(3).getTitle(),Base64.DEFAULT)));
                }
            }else {
                firstTime.setText("");
                firstTitle.setText("");
                secondTime.setText("");
                secondTitle.setText("");
                thirdTime.setText("");
                thirdTitle.setText("");
                fourthTime.setText("");
                fourthTitle.setText("");
            }
        }
    }

    int maxTime;
    private void listTimer() {
        maxTime = osd_time;
        mTicker = () -> {
            if (maxTime < 1) {
                ly_bottom.setVisibility(View.GONE);
                return;
            }
            runNextTicker();
        };
        mTicker.run();
    }
    private void runNextTicker() {
        maxTime--;
        long next = SystemClock.uptimeMillis() + 1000;
        removeHamdler.postAtTime(mTicker, next);
    }

    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void doWork() {
        runOnUiThread(() -> {
            try {
                txt_time.setText(Constants.clockFormat.format(new Date()));
            } catch (Exception e) {
            }
        });
    }
    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public void FullScreencall() {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            Toast.makeText(this,""+event.getKeyCode(),Toast.LENGTH_SHORT).show();
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_MENU:
                    if (full_datas.get(channel_pos).getChannels().get(sub_pos).is_favorite()) {
                        pkg_datas.set(0,"Remove from Fav");
                    }else {
                        pkg_datas.set(0,"Add to Fav");
                    }
                    PackageDlg packageDlg = new PackageDlg(PreviewChannelIJKActivity.this, pkg_datas, (dialog, position) -> {
                        dialog.dismiss();
                        switch (position) {
                            case 0:
                                if (full_datas.get(channel_pos).getChannels().get(sub_pos).is_favorite()) {
                                    pkg_datas.set(0, "Add to Fav");
                                    image_star.setVisibility(View.GONE);
                                    full_datas.get(channel_pos).getChannels().get(sub_pos).setIs_favorite(false);
                                    boolean is_exist = false;
                                    int pp = 0;
                                    for (int i = 0; i < full_datas.get(1).getChannels().size(); i++) {
                                        if (full_datas.get(1).getChannels().get(i).getName().equals(full_datas.get(channel_pos).getChannels().get(sub_pos).getName())) {
                                            is_exist = true;
                                            pp = i;
                                        }
                                    }
                                    if (is_exist)
                                        full_datas.get(1).getChannels().remove(pp);
                                    MyApp.instance.getPreference().put(Constants.getFavInfo(), Constants.getListStrFromListEpg(Constants.getFavFullModel(MyApp.fullModels_filter).getChannels()));
                                } else {
                                    image_star.setVisibility(View.VISIBLE);
                                    full_datas.get(channel_pos).getChannels().get(sub_pos).setIs_favorite(true);
                                    Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().add(full_datas.get(channel_pos).getChannels().get(sub_pos));
                                    MyApp.instance.getPreference().put(Constants.getFavInfo(), Constants.getListStrFromListEpg(Constants.getFavFullModel(MyApp.fullModels_filter).getChannels()));
                                    pkg_datas.set(0, "Remove from Fav");
                                }
                                adapter.selectItem(sub_pos);
                                listTimer();
                                break;
                            case 1:
                                SearchDlg searchDlg = new SearchDlg(PreviewChannelIJKActivity.this, (dialog1, sel_Channel) -> {
                                    dialog1.dismiss();
                                    FullScreencall();
                                    for(int i = 0;i<channels.size();i++){
                                        if(channels.get(i).getName().equalsIgnoreCase(sel_Channel.getName())){
                                            sub_pos = i;
                                            break;
                                        }
                                    }
                                    scrollToLast(channel_list, sub_pos);
                                    epg_pos = sub_pos;
                                    preview_pos = sub_pos;
                                    adapter.selectItem(sub_pos);
                                    mStream_id = channels.get(sub_pos).getStream_id();
                                    mEpgHandler.removeCallbacks(mEpgTicker);
                                    EpgTimer();
                                    playChannel();
                                });
                                searchDlg.show();
                                break;
                            case 2:
                                surfaceView.toggleAspectRatio();
                                break;
                            case 3:
                                if(is_recording){
                                    ShowStopDlg();
                                }else {
                                    ShowRecordingDlg();
                                }
                                break;
                            case 4:
                                startActivity(new Intent(PreviewChannelIJKActivity.this,WebViewActivity.class));
                                break;
                        }
                    });
                    packageDlg.show();
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if(ly_bottom.getVisibility()== View.VISIBLE){
                        ly_bottom.setVisibility(View.GONE);
                        return true;
                    }
                    if(is_full){
                        is_full = false;
                        ly_surface.setVisibility(View.VISIBLE);
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        ViewGroup.LayoutParams params = ly_surface.getLayoutParams();
                        params.height = MyApp.SURFACE_HEIGHT;
                        params.width = MyApp.SURFACE_WIDTH;
                        if(MyApp.SCREEN_HEIGHT==720){
                            setMargins(ly_surface,0,MyApp.top_margin-Utils.dp2px(this,5),MyApp.right_margin+Utils.dp2px(this,5),0);
                        }else if(MyApp.SCREEN_HEIGHT==1440){
                            params.height = (int) (MyApp.SURFACE_HEIGHT*0.9);
                            params.width = (int) (MyApp.SURFACE_WIDTH*0.9);
                            setMargins(ly_surface,0,MyApp.top_margin-Utils.dp2px(this,10),MyApp.right_margin,0);
                        }else {
                            setMargins(ly_surface,0,MyApp.top_margin,MyApp.right_margin,0);
                        }
//                        ly_surface.setPadding(15,15,15,15);
                        ly_surface.setLayoutParams(params);
                        ly_bottom.setVisibility(View.GONE);
                        return true;
                    }
                    releaseMediaPlayer();
                    finish();
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if(!is_full){
                        if(channels == null || channels.size()==0){
                            return true;
                        }
                        channel_list.setFocusable(true);
                        MyApp.key = false;
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        releaseMediaPlayer();
//                        Log.e("sub",String .valueOf(sub_pos));
                        String channel_name = channels.get(sub_pos).getName();
                        Intent intent = new Intent(this,GuideActivityExtra.class);
                        intent.putExtra("stream_id",mStream_id);
                        intent.putExtra("channel_name",channel_name);
//                        intent.putExtra("channel",channels.get(sub_pos));
                        MyApp.epgChannel = channels.get(sub_pos);
                        startActivity(intent);
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if(!is_full){
                        MyApp.key = false;
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        releaseMediaPlayer();
                        finish();
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
//                    Log.e("sub",String .valueOf(sub_pos));
                    removeHamdler.removeCallbacks(mTicker);
                    if(is_full){
                        is_up = true;
                        MyApp.is_first = false;
                        if(txt_progress.getVisibility()== View.GONE){
                             if(sub_pos>0){
                                sub_pos--;
                                 MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
                                mStream_id = channels.get(sub_pos).getStream_id();
                                 mEpgHandler.removeCallbacks(mEpgTicker);
                                 EpgTimer();
                                mHandler.removeCallbacks(mUpdateTimeTask);
                                MyApp.is_first = true;
                                channel_list.setSelection(sub_pos);
                                adapter.selectItem(sub_pos);
                                playChannel();
//                                 rssHandler.removeCallbacks(rssTicker);
//                                 new Thread(this::getRespond).start();
                                 return true;
                              }else {
                                 sub_pos = channels.size()-1;
                                 MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
                                 mStream_id = channels.get(sub_pos).getStream_id();
                                 mEpgHandler.removeCallbacks(mEpgTicker);
                                 EpgTimer();
                                 mHandler.removeCallbacks(mUpdateTimeTask);
                                 MyApp.is_first = true;
                                 channel_list.setSelection(sub_pos);
                                 adapter.selectItem(sub_pos);
                                 playChannel();
//                                 rssHandler.removeCallbacks(rssTicker);
//                                 new Thread(this::getRespond).start();
                                 return true;
                             }
                        }
                        return true;
                    }
//                    MyApp.key = false;
                    if(view ==channel_list){
                        is_up = false;
                        MyApp.is_first = false;
                        if(txt_progress.getVisibility()== View.GONE){
                            if(sub_pos<channels.size()-1){
                                sub_pos++;
                                MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
                                mStream_id = channels.get(sub_pos).getStream_id();
                                mEpgHandler.removeCallbacks(mEpgTicker);
                                EpgTimer();
                            }
//                            channel_list.setSelection(sub_pos);
//                            adapter.selectItem(sub_pos);
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    removeHamdler.removeCallbacks(mTicker);
                    if(is_full){
                        is_up = false;
                        MyApp.is_first = false;
                        if(txt_progress.getVisibility()== View.GONE){
                            if(sub_pos<channels.size()-1){
                                sub_pos++;
                                MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
                                mStream_id = channels.get(sub_pos).getStream_id();
                                mEpgHandler.removeCallbacks(mEpgTicker);
                                EpgTimer();
                                mHandler.removeCallbacks(mUpdateTimeTask);
                                MyApp.is_first = true;
                                channel_list.setSelection(sub_pos);
                                adapter.selectItem(sub_pos);
                                playChannel();
//                                rssHandler.removeCallbacks(rssTicker);
//                                new Thread(this::getRespond).start();
                                return true;
                            }else {
                                sub_pos = 0;
                                MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
                                mStream_id = channels.get(sub_pos).getStream_id();
                                mEpgHandler.removeCallbacks(mEpgTicker);
                                EpgTimer();
                                mHandler.removeCallbacks(mUpdateTimeTask);
                                MyApp.is_first = true;
                                channel_list.setSelection(sub_pos);
                                adapter.selectItem(sub_pos);
                                playChannel();
//                                rssHandler.removeCallbacks(rssTicker);
//                                new Thread(this::getRespond).start();
                                return true;
                            }
                        }
                        return true;
                    }
                    if(view == channel_list){
                        is_up = true;
                        MyApp.is_first = false;
                        if(txt_progress.getVisibility()== View.GONE){
                            if(sub_pos>0){
                                sub_pos--;
                                MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
                                mStream_id = channels.get(sub_pos).getStream_id();
                                mEpgHandler.removeCallbacks(mEpgTicker);
                                EpgTimer();
                            }
//                            channel_list.setSelection(sub_pos);
//                            adapter.selectItem(sub_pos);
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    if(is_full){
                        if(ly_bottom.getVisibility()== View.VISIBLE){
                            ly_bottom.setVisibility(View.GONE);
                            if(pro>99){
                                mEpgHandler.removeCallbacks(mEpgTicker);
                                EpgTimer();
                            }
                        }else {
                            ly_bottom.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_0:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE) num_txt.setVisibility(View.VISIBLE);
                    if (!key.isEmpty()) {
                        key += "0";
                        move_pos = parseInt(key);
                        if (move_pos > MyApp.channel_size) {
                            num_txt.setText("");
                            key = "";
                            move_pos = 0;
                            moveHandler.removeCallbacks(moveTicker);
                        } else {
                            moveHandler.removeCallbacks(moveTicker);
                            num_txt.setText(key);
                            moveTimer();
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_1:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE)
                        num_txt.setVisibility(View.VISIBLE);
                    key += "1";
                    move_pos = parseInt(key);
                    if (move_pos > MyApp.channel_size - 1) {
                        key = "";
                        num_txt.setText("");
                        move_pos = 0;
                        moveHandler.removeCallbacks(moveTicker);
                    } else {
                        moveHandler.removeCallbacks(moveTicker);
                        num_txt.setText(key);
                        moveTimer();

                    }
                    break;
                case KeyEvent.KEYCODE_2:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE)
                        num_txt.setVisibility(View.VISIBLE);
                    key += "2";
                    move_pos = parseInt(key);
                    if (move_pos > MyApp.channel_size - 1) {
                        key = "";
                        num_txt.setText("");
                        move_pos = 0;
                        moveHandler.removeCallbacks(moveTicker);
                    } else {
                        moveHandler.removeCallbacks(moveTicker);
                        num_txt.setText(key);
                        moveTimer();
                    }
                    break;
                case KeyEvent.KEYCODE_3:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE)
                        num_txt.setVisibility(View.VISIBLE);
                    key += "3";
                    move_pos = parseInt(key);
                    if (move_pos > MyApp.channel_size - 1) {
                        key = "";
                        num_txt.setText("");
                        move_pos = 0;
                        moveHandler.removeCallbacks(moveTicker);
                    } else {
                        moveHandler.removeCallbacks(moveTicker);
                        num_txt.setText(key);
                        moveTimer();
                    }
                    break;
                case KeyEvent.KEYCODE_4:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE)
                        num_txt.setVisibility(View.VISIBLE);
                    key += "4";
                    move_pos = parseInt(key);
                    if (move_pos > MyApp.channel_size - 1) {
                        key = "";
                        num_txt.setText("");
                        move_pos = 0;
                        moveHandler.removeCallbacks(moveTicker);
                    } else {
                        moveHandler.removeCallbacks(moveTicker);
                        num_txt.setText(key);
                        moveTimer();
                    }
                    break;
                case KeyEvent.KEYCODE_5:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE) num_txt.setVisibility(View.VISIBLE);
                    key += "5";
                    move_pos = parseInt(key);
                    if (move_pos > MyApp.channel_size - 1) {
                        key = "";
                        num_txt.setText("");
                        move_pos = 0;
                        moveHandler.removeCallbacks(moveTicker);
                    } else {
                        moveHandler.removeCallbacks(moveTicker);
                        num_txt.setText(key);
                        moveTimer();
                    }
                    break;
                case KeyEvent.KEYCODE_6:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE) num_txt.setVisibility(View.VISIBLE);
                    key += "6";
                    move_pos = parseInt(key);
                    if (move_pos > MyApp.channel_size - 1) {
                        key = "";
                        num_txt.setText("");
                        move_pos = 0;
                        moveHandler.removeCallbacks(moveTicker);
                    } else {
                        moveHandler.removeCallbacks(moveTicker);
                        num_txt.setText(key);
                        moveTimer();
                    }
                    break;
                case KeyEvent.KEYCODE_7:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE) num_txt.setVisibility(View.VISIBLE);
                    key += "7";
                    move_pos = parseInt(key);
                    if (move_pos > MyApp.channel_size - 1) {
                        key = "";
                        num_txt.setText("");
                        move_pos = 0;
                        moveHandler.removeCallbacks(moveTicker);
                    } else {
                        moveHandler.removeCallbacks(moveTicker);
                        num_txt.setText(key);
                        moveTimer();
                    }
                    break;
                case KeyEvent.KEYCODE_8:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE) num_txt.setVisibility(View.VISIBLE);
                    key += "8";
                    move_pos = parseInt(key);
                    if (move_pos > MyApp.channel_size - 1) {
                        key = "";
                        num_txt.setText("");
                        move_pos = 0;
                        moveHandler.removeCallbacks(moveTicker);
                    } else {
                        moveHandler.removeCallbacks(moveTicker);
                        num_txt.setText(key);
                        moveTimer();
                    }
                    break;
                case KeyEvent.KEYCODE_9:
                    MyApp.key = true;
                    if (num_txt.getVisibility() == View.GONE) num_txt.setVisibility(View.VISIBLE);
                    key += "9";
                    move_pos = parseInt(key);
                    if (move_pos > MyApp.channel_size - 1) {
                        key = "";
                        num_txt.setText("");
                        move_pos = 0;
                        moveHandler.removeCallbacks(moveTicker);
                    } else {
                        moveHandler.removeCallbacks(moveTicker);
                        num_txt.setText(key);
                        moveTimer();
                    }
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    int moveTime;
    private void moveTimer() {
        moveTime = 2;
        moveTicker = () -> {
            if(moveTime==1){
                if(is_full){
                    for(int i = 0;i<channels.size();i++){
                        if (parseInt(channels.get(i).getNum()) == move_pos) {
                            sel_model = channels.get(i);
                            sub_pos = i;
                            MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
                            break;
                        }
                    }
                    if (sel_model == null) {
                        MyApp.key = false;
                        key = "";
                        num_txt.setText("");
                        num_txt.setVisibility(View.GONE);
                        Toast.makeText(PreviewChannelIJKActivity.this,"This category do not have this channel", Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        key = "";
                        num_txt.setText("");
                        num_txt.setVisibility(View.GONE);
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        mStream_id = sel_model.getStream_id();
                        MyApp.is_first = true;
                        channel_list.setSelection(sub_pos);
                        adapter.selectItem(sub_pos);
//                        new Thread(()->getEpg()).start();
                        new Thread(()-> getEpg(channels.get(sub_pos))).start();
                        playChannel();
                        listTimer();
                    }
                }else {
                    for(int i = 0;i<channels.size();i++){
                        if (parseInt(channels.get(i).getNum()) == move_pos) {
                            sel_model = channels.get(i);
                            sub_pos = i;
                            MyApp.instance.getPreference().put(Constants.getSubPos(),sub_pos);
                            break;
                        }
                    }
                    if (sel_model == null) {
                        MyApp.key = false;
                        key = "";
                        num_txt.setText("");
                        num_txt.setVisibility(View.GONE);
                        Toast.makeText(PreviewChannelIJKActivity.this,"This category do not have this channel", Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        key = "";
                        num_txt.setText("");
                        num_txt.setVisibility(View.GONE);
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        mStream_id = sel_model.getStream_id();
                        MyApp.is_first = true;
                        channel_list.setSelection(sub_pos);
                        adapter.selectItem(sub_pos);
//                        new Thread(()->getEpg()).start();
                        new Thread(()-> getEpg(channels.get(sub_pos))).start();
                        playChannel();
                        listTimer();
                    }
                }
                return;
            }
            moveNextTicker();
        };
        moveTicker.run();
    }

    private void getEpg(EPGChannel epgChannel){
        try {
            String map = MyApp.instance.getIptvclient().getShortEPG(MyApp.user,MyApp.pass,
                    epgChannel.getStream_id()+"",4);
            Log.e(getClass().getSimpleName(),map);
            Gson gson=new Gson();
            map=map.replaceAll("[^\\x00-\\x7F]", "");
            if (!map.contains("null_error_response")){
                Log.e("response",map);
                try {
                    JSONObject jsonObject= new JSONObject(map);
                    JSONArray jsonArray=jsonObject.getJSONArray("epg_listings");
                    epgModelList = new ArrayList<>();
                    epgModelList.addAll(gson.fromJson(jsonArray.toString(), new TypeToken<List<EPGEvent>>(){}.getType()));
                    runOnUiThread(this::printEpgData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveNextTicker() {
        moveTime--;
        long next = SystemClock.uptimeMillis() + 1000;
        moveHandler.postAtTime(moveTicker, next);
    }

    private void playChannel(){
        if (surfaceView != null) {
            releaseMediaPlayer();
            surfaceView = null;
        }
        surfaceView = findViewById(R.id.surface_view);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        mVideoHeight = displayMetrics.heightPixels;
        mVideoWidth = displayMetrics.widthPixels;
        mStream_id = channels.get(sub_pos).getStream_id();

        EPGChannel showmodel = channels.get(sub_pos);
        checkAddedRecent(showmodel);
        Constants.getRecentFullModel(MyApp.fullModels_filter).getChannels().add(0,showmodel);
        //get recent series names list
        List<String> recent_series_names = new ArrayList<>();
        for (EPGChannel channel:Constants.getRecentFullModel(MyApp.fullModels_filter).getChannels()){
            recent_series_names.add(channel.getName());
        }
        //set
        MyApp.instance.getPreference().put(Constants.getRecentChannels(), recent_series_names);
        contentUri = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                mStream_id,"ts");
        if(channels.get(sub_pos).getStream_icon()!=null && !channels.get(sub_pos).getStream_icon().isEmpty()){
            Picasso.with(PreviewChannelIJKActivity.this).load(channels.get(sub_pos).getStream_icon())
                    .into(channel_logo);
            channel_logo.setVisibility(View.VISIBLE);
        }else {
            channel_logo.setVisibility(View.GONE);
        }
        if(channels.get(sub_pos).is_locked() && channel_pos==1){
            PinDlg pinDlg = new PinDlg(this, new PinDlg.DlgPinListener() {
                @Override
                public void OnYesClick(Dialog dialog, String pin_code) {
                    dialog.dismiss();
                    String pin = (String)MyApp.instance.getPreference().get(Constants.PIN_CODE);
                    if(pin_code.equalsIgnoreCase(pin)){
                        dialog.dismiss();
                        playVideo();
                    }else {
                        dialog.dismiss();
                        Toast.makeText(PreviewChannelIJKActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void OnCancelClick(Dialog dialog, String pin_code) {
                    dialog.dismiss();
                }
            });
            pinDlg.show();
        }else {
            playVideo();
        }
        channel_list.requestFocus();
    }

    private void checkAddedRecent(EPGChannel showModel) {
        Iterator<EPGChannel> iter =  Constants.getRecentFullModel(MyApp.fullModels_filter).getChannels().iterator();
        while(iter.hasNext()){
            EPGChannel movieModel = iter.next();
            if (movieModel.getName().equals(showModel.getName()))
                iter.remove();
        }
    }

    private void playVideo() {
        toggleFullscreen(true);
//        Log.e("url",contentUri);
        if(def_lay.getVisibility()== View.VISIBLE)def_lay.setVisibility(View.GONE);
        try {
            surfaceView.setMediaController(mMediaController);
            surfaceView.setHudView(mHudView);
            mMediaController.hide();
            surfaceView.setVideoPath(contentUri);
            surfaceView.setOnCompletionListener(this);
            surfaceView.setOnErrorListener(this);
            surfaceView.start();
            updateProgressBar();
        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }
    @Override
    protected void onUserLeaveHint() {
        releaseMediaPlayer();
        super.onUserLeaveHint();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!is_create) {
            if (surfaceView != null) {
                releaseMediaPlayer();
                surfaceView = null;
            }
            surfaceView = findViewById(R.id.surface_view);
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            mVideoHeight = displayMetrics.heightPixels;
            mVideoWidth = displayMetrics.widthPixels;
            playVideo();
            channel_list.requestFocus();
        } else {
            is_create = false;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
    private void releaseMediaPlayer() {

        if (surfaceView == null)
            return;
        surfaceView.release(true);
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        releaseMediaPlayer();
        onResume();
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        releaseMediaPlayer();
        return false;
    }

    @Override
    public void onDestroy() {
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

}
