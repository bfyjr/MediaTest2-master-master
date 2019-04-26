package com.example.mediatest;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tyrantgit.explosionfield.ExplosionField;


public class Activity2 extends AppCompatActivity implements VideoItemAdapter.OnItemClickListener{
    ImageView floatMainImageView;
    FloatingActionButton floatingActionButton;
    SubActionButton.Builder subBuilder ;
    FloatingActionMenu floatingMenu;
    RecyclerView recyclerView;
    android.support.design.widget.FloatingActionButton float_but;
    TabLayout tabLayout;
    SwipeRefreshLayout swipeRefreshLayout;
    private List<VideoItem> videoItemList=new ArrayList<>(),queryVideoList=new ArrayList<>();
    private VideoItemAdapter videoItemAdapter,queryResultAdapter;
    SearchView searchView;
    ExplosionField explosionField;
    TextView tabTextView;
    boolean showQueryFlag=false;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        //设置ToolBar
        Toolbar myToolbar =  findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置返回按键可见
        initSwipRefreshLayout();
        initTableLayout();

        //运行时权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else
        {
            initRecyclerView();
        }
        //设置浮动按钮
        initFloatingButton();


    }

    public void initRecyclerView()
    {
        explosionField= ExplosionField.attach2Window(this);
        videoItemList=VideoUtil.getLocalVideos(this);
        recyclerView=findViewById(R.id.recrcler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        videoItemAdapter=new VideoItemAdapter(videoItemList);
        videoItemAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(videoItemAdapter);


    }
    public void initSwipRefreshLayout()
    {
        swipeRefreshLayout=findViewById(R.id.swiprefresh_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showQueryFlag=false;
                refreshCardItems();
            }
        });
    }

    //查找手机视频文件显示在RecyclerView中
    public void refreshCardItems()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(350);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initRecyclerView();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(Activity2.this, "刷新成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }



    public void initFloatingButton()
    {
        floatMainImageView=new ImageView(this);
        floatMainImageView.setImageResource(R.drawable.plus);
        floatingActionButton= new FloatingActionButton.Builder(this).setContentView(floatMainImageView).build();
        subBuilder= new SubActionButton.Builder(this);
        //设置弹出的小图标
        ImageView icon1 = new ImageView(this);icon1.setImageResource(R.drawable.folder);icon1.setClickable(true);icon1.setLongClickable(true);
        ImageView icon2 = new ImageView(this);icon2.setImageResource(R.drawable.wrench);icon2.setClickable(true);icon2.setLongClickable(true);
        ImageView icon3 = new ImageView(this);icon3.setImageResource(R.drawable.sort);icon3.setClickable(true);icon3.setLongClickable(true);
        //为小图标设置点击监听
        icon1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Activity2.this, "folder", Toast.LENGTH_SHORT).show();
            }
        });
        icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Activity2.this, "wrench", Toast.LENGTH_SHORT).show();
            }
        });
        icon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(Activity2.this, "sort", Toast.LENGTH_SHORT).show();

            }
        });

        floatingMenu=new FloatingActionMenu.Builder(this)
                .addSubActionView(subBuilder.setContentView(icon1).build())
                .addSubActionView(subBuilder.setContentView(icon2).build())
                .addSubActionView(subBuilder.setContentView(icon3).build())
                .attachTo(floatingActionButton).build();

        floatingMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {

            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                // 逆时针旋转90°
                floatMainImageView.setRotation(0);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(
                        View.ROTATION, -45);

                ObjectAnimator animation = ObjectAnimator
                        .ofPropertyValuesHolder(floatMainImageView, pvhR);
                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                // 顺时针旋转90°
                floatMainImageView.setRotation(-45);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(
                        View.ROTATION, 0);
                ObjectAnimator animation = ObjectAnimator
                        .ofPropertyValuesHolder(floatMainImageView, pvhR);
                animation.start();

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case 1:
            {
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    initRecyclerView();
                }else
                {
                    Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        MenuItem searchItem =menu.findItem(R.id.action_search);
         searchView= (SearchView) searchItem.getActionView();
        searchView.setSubmitButtonEnabled(true);
        //设置输入框提示语
        searchView.setQueryHint("搜索本地视频");
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryVideoList.clear();
                //搜索本地视频
                for(int i=0;i<videoItemList.size();i++)
                {
                    if(VideoUtil.splitName(videoItemList.get(i).getPath()).contains(query))
                    {
                        queryVideoList.add(videoItemList.get(i));

                    }
                }
                queryResultAdapter=new VideoItemAdapter(queryVideoList,true,query);
                queryResultAdapter.setOnItemClickListener(Activity2.this);
                recyclerView.setAdapter(queryResultAdapter);
                showQueryFlag=true;
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return true;
    }
    public void initTableLayout()
    {
        tabTextView=findViewById(R.id.tab_textview);
        tabTextView.setVisibility(View.GONE);
        tabLayout=findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("本地"));
        tabLayout.addTab(tabLayout.newTab().setText("网络"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tag=tab.getText().toString();
                //Toast.makeText(Activity2.this, tab.getText().toString(), Toast.LENGTH_SHORT).sho w();
                if(tag=="本地")
                {
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    tabTextView.setVisibility(View.GONE);
                }else
                {
                    swipeRefreshLayout.setVisibility(View.GONE);
                    tabTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        videoItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    public void onItemClick(View v, int position) {
        Intent intent=new Intent(Activity2.this,MainActivity.class);
        if(!showQueryFlag)
        {
            intent.putExtra("local_video_path",videoItemList.get(position).getPath());
            intent.putExtra("position",position);
            //videoItemList.remove(position);
            intent.putExtra("videoItemList", (Serializable) videoItemList);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(!(videoItemList.get(position).getDuration()>0))
            {
                v.setClickable(false);
                return;
            }
        }else
        {
            intent.putExtra("local_video_path",queryVideoList.get(position).getPath());
            intent.putExtra("position",position);
            //videoItemList.remove(position);
            intent.putExtra("videoItemList", (Serializable) queryVideoList);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(!(queryVideoList.get(position).getDuration()>0))
            {
                v.setClickable(false);
                return;
            }
        }
        explosionField.explode(v);
        explosionField.explode(v);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(460);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(intent);
                    }
                });
            }
        }).start();
    }
}
