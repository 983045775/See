package com.minardwu.see.activity;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.minardwu.see.R;
import com.minardwu.see.adapter.MyFragmentPagerAdapter;
import com.minardwu.see.base.Config;
import com.minardwu.see.entity.Photo;
import com.minardwu.see.event.DeletePhotoEvent;
import com.minardwu.see.event.PageChangeEvent;
import com.minardwu.see.fragment.ShowPhotoFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ShowPhotoActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private List<Fragment> fragmentList;
    private MyFragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);

        EventBus.getDefault().register(this);

        Bundle bundle = getIntent().getExtras();
        int type = bundle.getInt("type");
        int position = bundle.getInt("position");


        fragmentList = new ArrayList<Fragment>();
        if(type==0){
            fragmentList.clear();
            for(Photo photo:Config.yourPhotos)
                fragmentList.add(new ShowPhotoFragment(type,photo));
        }else if(type==1){
            fragmentList.clear();
            for(Photo photo:Config.myPhotos)
                fragmentList.add(new ShowPhotoFragment(type,photo));
        }
        adapter =new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(0);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //页面跳转完后调用
            @Override
            public void onPageSelected(int position) {
                EventBus.getDefault().post(new PageChangeEvent(1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeletePhotoEvent(DeletePhotoEvent event){
        if(event.getResult()==1){
            Toast.makeText(this,"删除成功", Toast.LENGTH_SHORT).show();
            Config.deletePhoto = true;//标记，以便在YourFragment中更新视图
            for(Photo tempphoto:Config.yourPhotos)
                if(tempphoto.getPhotoid().equals(event.getPhotoid()))
                    if(tempphoto.getState()==1){//如果是目前设置为展示的图片，将其删除之后还要设置第一个为展示
                        Config.yourPhotos.remove(tempphoto);
                        Config.yourPhotos.get(0).setState(1);
                    }else {//若不是展示的图片则直接删除就行
                        Config.yourPhotos.remove(tempphoto);
                    }
            //跳转回YourFragment
            //fragmentList.remove(1);
            //adapter.notifyDataSetChanged();
            //finish();
        }else {
            Toast.makeText(this,"删除失败", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
