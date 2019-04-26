package com.example.mediatest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import tyrantgit.explosionfield.ExplosionField;

public class MainActivity extends AppCompatActivity {
    public VideoItem videoItem;
    public String videoPath;
    public List<VideoItem> videoItemList;
    public ExplosionField explosionField;
    public RecyclerView playRecyclerView;
    public VideoItemAdapter videoItemAdapter;
    public SwipeRefreshLayout swipeRefreshLayout;
    public MyVideoView myVideoView;
    public ConstraintLayout controlConstraintLayout,videoviewConsLayout;
    public ImageButton playButton,fullScreenButton,fullReturnButton;
    public SeekBar durationSeekBar;
    public TextView durationTextview,videoTitleText,centerVolumeText;
    public Thread  durationThread;
    public String durationStr,currentBrightness;
    boolean isFirstLoad=true;
    long lastTime,thisTime;
    float startX,startY,endX,endY,MIN_SCROLL_Y=90,MIN_SCROLL_X=80,scale,//scale为视频宽高比
            lastY, thisY,lastX,thisX;//监听滑动手势//lastY记录滑动时上一次的竖直位置;
    ConstraintLayout.LayoutParams layoutParams;
    public AudioManager audioManager;
    public int maxVolume,currentVolume;//最大媒体音量
    public int systemUiVisibility;
    public int LAND_SCREEN=1,PORT_SCREEN=0,SCREEN_STATE=-1,transProgress=0,screenWidth,screenHeight,midScreenPoint;
    RelativeLayout playRelativeLayout;
    LinearLayout topTextLayout;
    public boolean twoFingerFlag=false;
    public int currentVideoPosition=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAll();

    }
    public void initAll()
    {
        videoPath=getIntent().getStringExtra("local_video_path");
        // Toast.makeText(this, videoPath, Toast.LENGTH_SHORT).show();
        videoItemList = (List<VideoItem>) getIntent().getSerializableExtra("videoItemList");
        currentVideoPosition=getIntent().getIntExtra("position",0);
        videoItem=videoItemList.get(currentVideoPosition);
        myVideoView=findViewById(R.id.video_view);
        controlConstraintLayout=findViewById(R.id.control_consLayout);
        playButton=findViewById(R.id.play_but);
        fullScreenButton=findViewById(R.id.full_screen);
        durationSeekBar=findViewById(R.id.play_seekbar);
        durationTextview=findViewById(R.id.duratioon_textview);
        videoTitleText=findViewById(R.id.videoTitleText);
        videoviewConsLayout=findViewById(R.id.video_conslayout);
        centerVolumeText=findViewById(R.id.centerVolumeText);
        audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume= audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        playRelativeLayout=findViewById(R.id.playRelativeLayout);
        fullReturnButton=findViewById(R.id.full_return_button);
        topTextLayout=findViewById(R.id.topLinearLayout);


        SCREEN_STATE=PORT_SCREEN;
        //下面两行用于测量屏幕宽高
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        midScreenPoint=point.x/2;
        screenWidth=point.x;
        screenHeight=point.y;
        //保留屏幕初始状态
        systemUiVisibility=getWindow().getDecorView().getSystemUiVisibility();

        requestSDpermission(videoPath);//播放视频方法
        initRecyclerView();
    }
    public void initControlConstrainLayout()
    {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isFirstLoad)//第一次加载资源
                {

                    myVideoView.start();
                    myVideoView.setBackground(null);
                    durationThread.start();
                    playButton.setBackgroundResource(R.drawable.pause);
                    isFirstLoad=false;
                }else//普通暂停/播放
                {
                    if(myVideoView.isPlaying())
                    {myVideoView.pause();playButton.setBackgroundResource(R.drawable.play);}else
                    {myVideoView.start();playButton.setBackgroundResource(R.drawable.pause);}

                }

            }
        });
        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击实现全屏
               if(SCREEN_STATE==LAND_SCREEN)
               {
                   convertToPortraitScreen();

               }else
               {
                   convertToLandScreen();
               }

            }
        });
        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress=seekBar.getProgress();
                float percent=((float)progress)/100;
                myVideoView.seekTo((int)(myVideoView.getDuration()*percent));
            }
        });
        //设置TextView显示时间
        int duration=myVideoView.getDuration();
        String second=VideoUtil.twoChar(duration/1000%60+"");
        String minute=VideoUtil.twoChar(duration/1000/60+"");
        durationStr=minute+":"+second;

        durationTextview.setText("00:00"+"/"+durationStr);
        videoTitleText.setText(VideoUtil.splitName(videoItem.getPath()));


    }

    public void initRecyclerView()
    {
        swipeRefreshLayout=findViewById(R.id.play_swiprefresh_view);
        explosionField= ExplosionField.attach2Window(this);
        playRecyclerView=findViewById(R.id.play_recrcler_view);
        playRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        videoItemAdapter=new VideoItemAdapter(videoItemList,false);
        videoItemAdapter.setOnItemClickListener(new VideoItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                VideoItem videoItem=videoItemList.get(position);
                explosionField.explode(v);
                if(!(videoItemList.get(position).getDuration()>0))
                {
                    v.setClickable(false);
                    return;
                }
                isFirstLoad=true;
                videoItem=videoItemList.get(position);
                requestSDpermission(videoItem.getPath());
                currentVideoPosition=position;

            }
        });
        playRecyclerView.setAdapter(videoItemAdapter);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                    videoItemAdapter.notifyItemRangeChanged(0,videoItemList.size());

                Toast.makeText(MainActivity.this, "成功刷新"+videoItemList.size()+"个视频", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }
    private void requestSDpermission(String videoPath) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            initVideoPath(videoPath);
        }
    }

    private void initVideoPath(String videoPath) {
        //获取视频略缩图放在播放封面
        Drawable drawable=new BitmapDrawable(MainActivity.this.getResources(),VideoUtil.getVideoThumbnail(videoPath,this));
        myVideoView.setBackground(drawable);
        myVideoView.setVideoPath(videoPath);
         myVideoView.pause();
         durationSeekBar.setProgress(0);
         setVideoviewPreparedListener();
         setVideoviewCompletionListener();




    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initVideoPath(videoPath);
                } else {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
            default:
        }
    }

    public void setVideoviewCompletionListener()
    {
        myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
                durationSeekBar.setProgress(0);
            }
        });
    }

    public void setVideoviewPreparedListener()
    {
        myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int width=mp.getVideoWidth();
                int height=mp.getVideoHeight();
                //获取视频宽高比例
                scale=((float)width)/((float)height);
                setVideoviewLayoutparams(scale);
                initListenDuration();//实例化监听视频进度进程
                initControlConstrainLayout();//初始化控制栏
                setVideoViewTouchListener();//为播放界面添加手势监听
                fullReturnButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        convertToPortraitScreen();
                    }
                });

            }
        });
    }
    public void setVideoviewLayoutparams(float scale)
    {

        durationSeekBar.setProgress(0);
        layoutParams= (ConstraintLayout.LayoutParams) myVideoView.getLayoutParams();
       // Log.i("Hs", "point.x: "+point.x);
        if(scale<1)//高视频
        {
            if(SCREEN_STATE==PORT_SCREEN)
            {
                //视频源为竖屏大小时，宽度不充满屏幕
                layoutParams.height = 700;
                layoutParams.width = (int)(layoutParams.height*scale);
                layoutParams.bottomMargin=0;
            }else
            {//横屏
                layoutParams.height=screenWidth;
                layoutParams.width=(int)(screenWidth*scale);
                //layoutParams.bottomMargin=100;
            }
        }else//宽视频
        {
            if(SCREEN_STATE==PORT_SCREEN)
            {//宽度充满屏幕
                layoutParams.width = screenWidth;
                layoutParams.height = (int)((float)screenWidth/scale);
            }else
            {
                //
                layoutParams.height=screenWidth;
                layoutParams.width=(int)(screenWidth*scale);
            }

        }
        //重新设置视频大小布局
        myVideoView.setLayoutParams(layoutParams);
        //开始播放前加载视频略缩图
        playButton.setBackgroundResource(R.drawable.play);
        //动态监控播放进度
        myVideoView.seekTo(transProgress);
    }
    //动态监控视频进度
    public void initListenDuration()
    {
        durationThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(myVideoView.isPlaying())
                {
                    float per=(float)myVideoView.getCurrentPosition()/(float)myVideoView.getDuration();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            durationSeekBar.setProgress((int)(100*per));
                            durationTextview.setText(VideoUtil.twoChar(myVideoView.getCurrentPosition()/1000/60+"")+":"
                                    +VideoUtil.twoChar(myVideoView.getCurrentPosition()/1000%60+"")+"/"
                                    +durationStr);
                        }
                    });
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }
    //播放界面手势监听，单击，双击，滑动快进，调整音量亮度
    public void setVideoViewTouchListener()
    {
        videoviewConsLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        lastX=endX=startX=event.getX();lastY=endY=startY=event.getY();
                        lastTime=thisTime;
                        thisTime=System.currentTimeMillis();
                        if(Math.abs(thisTime-lastTime)<400)//双击暂停/播放
                        {
                            if(myVideoView.isPlaying())
                            {
                                myVideoView.pause();
                                playButton.setBackgroundResource(R.drawable.play);
                            }else
                            {
                                if(isFirstLoad)
                                {
                                    myVideoView.start();
                                    myVideoView.setBackground(null);
                                    durationThread.start();
                                    isFirstLoad=false;
                                }
                                myVideoView.pause();
                                myVideoView.start();
                                initListenDuration();
                                durationThread.start();
                                playButton.setBackgroundResource(R.drawable.pause);
                            }
                            controlConstraintLayout.setVisibility(View.VISIBLE);
                            videoTitleText.setVisibility(View.VISIBLE);
                        }else//单击隐藏/显示控制栏
                        {
                            if(controlConstraintLayout.getVisibility()==View.VISIBLE)
                            {
                                controlConstraintLayout.setVisibility(View.INVISIBLE);
                                topTextLayout.setVisibility(View.INVISIBLE);
                            }else
                            {
                                controlConstraintLayout.setVisibility(View.VISIBLE);
                                topTextLayout.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    }
                    case MotionEvent.ACTION_MOVE://要响应此事件需要在布局文件中添加android:clickable="true"
                    {
                        endY=event.getY();
                        endX=event.getX();
                        if(Math.abs((int)(startX-endX))>MIN_SCROLL_X)//滑动距离超过最小距离才判定滑动,进行快进操作
                        {
                            if(event.getPointerCount()==2)
                            {twoFingerFlag=true;
                            break;}
                                centerVolumeText.setVisibility(View.VISIBLE);
                                controlConstraintLayout.setVisibility(View.VISIBLE);
                                thisX = event.getX();
                                if (Math.abs(thisX - lastX) >= 20) {
                                    if (thisX > lastX)//右滑，快进
                                    {
                                        myVideoView.seekTo(myVideoView.getCurrentPosition() + 300);
                                        //myVideoView.seekTo(myVideoView.getCurrentPosition());
                                    } else//左滑，快退
                                    {
                                        myVideoView.seekTo(myVideoView.getCurrentPosition() - 300);
                                    }
                                    centerVolumeText.setText("进度：" + VideoUtil.twoChar(myVideoView.getCurrentPosition() / 1000 / 60 + "")
                                            + ":" + VideoUtil.twoChar(myVideoView.getCurrentPosition() / 1000 % 60 + "") + "/" + durationStr);
                                }

                                durationSeekBar.setProgress((int) (100 * (float) myVideoView.getCurrentPosition() / (float) myVideoView.getDuration()));
                        }

                        if(Math.abs((int)(startY-endY))>MIN_SCROLL_Y)//滑动距离Y超过最小距离才判定滑动
                        {
                            if(startX<=midScreenPoint)
                            {
                                centerVolumeText.setVisibility(View.VISIBLE);
                                thisY=event.getY();
                                if(Math.abs(thisY-lastY)>=50)
                                {
                                    if(thisY<lastY)
                                    {currentBrightness=setBrightness(10);}
                                    else
                                    {currentBrightness=setBrightness(-10);}
                                    centerVolumeText.setText("亮度："+(int)(Float.parseFloat(currentBrightness)*100)+"%");
                                    lastY=thisY;
                                }
                            }else//在右边屏幕滑动
                            {
                                centerVolumeText.setVisibility(View.VISIBLE);
                                thisY=event.getY();
                                if(Math.abs(thisY-lastY)>=50)
                                {
                                    if(thisY<lastY)
                                    {audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,0);}
                                    else
                                    {audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,0);}
                                    currentVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                    centerVolumeText.setText("音量："+(int)((float)(currentVolume)/maxVolume*100)+"%");
                                    lastY=thisY;
                                }
                            }
                        }

                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    {
                        Log.i("Hs", "Up"+twoFingerFlag);
                        if(twoFingerFlag)
                        {
                            if(startX-endX>200)
                            {
                                playPreviousVideo();
                                twoFingerFlag=false;
                            }
                            if(endX-startX>200)
                            {
                                playNextVideo();
                                twoFingerFlag=false;
                            }
                        }
                        centerVolumeText.setVisibility(View.INVISIBLE);
                        break;
                    }
                    default:


                }

                return false;
            }
        });

    }

    public String setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness =lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = (float) 0.1;
        }
        getWindow().setAttributes(lp);

        return lp.screenBrightness+"";
    }
    public void convertToLandScreen(){
        transProgress=myVideoView.getCurrentPosition();
        SCREEN_STATE=LAND_SCREEN;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        setVideoviewLayoutparams(scale);
        midScreenPoint=screenHeight/2;
        fullScreenButton.setImageResource(R.drawable.full);
        hideSystemBar();
        playButton.setBackgroundResource(myVideoView.isPlaying()?R.drawable.pause:R.drawable.play);
        fullReturnButton.setVisibility(View.VISIBLE);

    }
    public void convertToPortraitScreen()
    {
        transProgress=myVideoView.getCurrentPosition();
        SCREEN_STATE=PORT_SCREEN;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        setVideoviewLayoutparams(scale);
        midScreenPoint=screenWidth/2;
        fullScreenButton.setImageResource(R.drawable.notfull);
        fullReturnButton.setVisibility(View.GONE);
        playButton.setBackgroundResource(myVideoView.isPlaying()?R.drawable.pause:R.drawable.play);
        showSystemBar();

    }

    @Override
    public void onBackPressed() {
        if(SCREEN_STATE==LAND_SCREEN)
        {
            SCREEN_STATE=PORT_SCREEN;
            convertToPortraitScreen();
        }
        else
        {
            super.onBackPressed();
        }
    }

    public void hideSystemBar(){
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏

        }
    }
    public void showSystemBar(){
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(systemUiVisibility);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
        }
    }
    public void playPreviousVideo()
    {
        if(currentVideoPosition>0)
        {
            Log.i("Hs", currentVideoPosition+"");
            isFirstLoad=true;
            requestSDpermission(videoItemList.get(currentVideoPosition-1).getPath());
            currentVideoPosition--;
        }else
        {
            Toast.makeText(this, "当前已经是第一集", Toast.LENGTH_SHORT).show();
        }
    }
    public void playNextVideo()
    {
        if(currentVideoPosition<videoItemList.size()-1)
        {
            Log.i("Hs", currentVideoPosition+"");
            isFirstLoad=true;
            requestSDpermission(videoItemList.get(currentVideoPosition+1).getPath());
            currentVideoPosition++;
        }else
        {
            Toast.makeText(this, "当前已经是最后一集", Toast.LENGTH_SHORT).show();
        }
    }
}
