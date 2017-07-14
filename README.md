# CircularIndicatorTabLayout #

 [ ![Download](https://api.bintray.com/packages/ngima/CircularIndicatorTabLayout/CircularIndicatorTabLayout/images/download.svg) ](https://bintray.com/ngima/CircularIndicatorTabLayout/download_file?file_path=circular-idicator-tab-layout-1.0.0.aar)

## Demo ##
![Demo](./images/demo1.gif)

## Installation ##

* Download CircularIndicatorTabLayout aar file from here: [ ![Download](https://api.bintray.com/packages/ngima/CircularIndicatorTabLayout/CircularIndicatorTabLayout/images/download.svg) ](https://bintray.com/ngima/CircularIndicatorTabLayout/download_file?file_path=circular-idicator-tab-layout-1.0.0.aar)
* Move `circular-idicator-tab-layout-1.0.0.aar` to `app/libs`
* In project build.gradle file add 
  `repositories {  
        flatDir {
            dirs 'libs'
        }
    }`
 * In app build.gradle file add
  `compile(name: 'circular-idicator-tab-layout-1.0.0', ext: 'aar')`


## Example ##
### In layout ### 

    <np.com.ngimasherpa.citablayout.CircularIndicatorTabLayout  
            android:id="@+id/tab_monitoring_criteria"  
            android:layout_width="match_parent"  
            android:layout_height="@dimen/spacing_72"  
            app:iconColor="@color/colorPrimaryDark"  
            app:indicatorColor="@color/colorAccent"  
            app:indicatorPosition="bottom"  
            app:lineColor="@android:color/holo_red_dark"  
            app:lineEnabled="true"  
            app:mode="fixed"  
            app:selectedIconColor="@color/colorAccent"  
            app:tabViewIconId="@+id/iconTabViewLayout"  
            app:tabViewLayoutId="@layout/tab_layout"  
            app:tabViewTextViewColor="@color/colorPrimaryDark"  
            app:tabViewTextViewId="@+id/textTabViewLayout"  
            app:tabViewTextViewSelectedColor="@color/colorAccent"  
            app:tab_gravity="fill" />
  
### In java ### 

        SectionPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        CircularIndicatorTabLayout tabLayout = (CircularIndicatorTabLayout) findViewById(R.id.tab_monitoring_criteria);
        
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setIcons(
                R.drawable.ic_arrow_drop_down,
                R.drawable.ic_audiotrack,
                R.drawable.ic_beach);
