package com.example.yunoi.cleaningmaster;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TutorialVierPagerAdapter extends FragmentPagerAdapter {
    public TutorialVierPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    //각 도움말 페이지 부르기
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return TutorialPageFirst.newInstance();
            case 1: return TutorialPage2.newInstance();
            case 2: return TutorialPage3.newInstance();
            case 3: return TutorialPage4.newInstance();
            case 4: return TutorialPageLast.newInstance();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 5;
    }

}
