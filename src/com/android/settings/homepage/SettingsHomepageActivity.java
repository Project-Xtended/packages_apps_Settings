/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.homepage;

import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.app.settings.SettingsEnums;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;
import android.provider.Settings;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.internal.util.UserIcons;
import com.android.internal.util.xtended.XFontHelper;
import com.android.internal.util.xtended.XImageUtils;

import com.android.settings.R;
import com.android.settings.accounts.AvatarViewMixin;
import com.android.settings.core.HideNonSystemOverlayMixin;
import com.android.settings.homepage.contextualcards.ContextualCardsFragment;
import com.android.settings.overlay.FeatureFactory;

import com.android.settingslib.drawable.CircleFramedDrawable;

import com.google.android.material.appbar.AppBarLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SettingsHomepageActivity extends FragmentActivity {
    private static final String TAG = "SettingsHomepageActivity";

    Context context;
    ImageView avatarView;
    UserManager mUserManager;

    View homepageSpacer;
    View homepageMainLayout;
    ImageView iv;
    ImageView mCustomImage;
    Drawable mStockDrawable;

    private static final String SPACER_IMAGE = "custom_spacer_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getApplicationContext();

        final boolean useNewSearchBar = Settings.System.getIntForUser(context.getContentResolver(),
                Settings.System.USE_NEW_SEARCHBAR, 0, UserHandle.USER_CURRENT) != 0;

        setContentView(useNewSearchBar  ? R.layout.settings_homepage_container_a12
                                        : R.layout.settings_homepage_container);
        final View root = findViewById(R.id.settings_homepage_container);
        root.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        if (useNewSearchBar)
            setHomepageContainerPaddingTopA12();
        else
            setHomepageContainerPaddingTop();

        mUserManager = context.getSystemService(UserManager.class);

        final Toolbar toolbar = findViewById(R.id.search_action_bar);
        FeatureFactory.getFactory(this).getSearchFeatureProvider()
                .initSearchToolbar(this /* activity */, toolbar, SettingsEnums.SETTINGS_HOMEPAGE);

        getLifecycle().addObserver(new HideNonSystemOverlayMixin(this));

        avatarView = root.findViewById(R.id.account_avatar);
        //final AvatarViewMixin avatarViewMixin = new AvatarViewMixin(this, avatarView);
        avatarView.setImageDrawable(getCircularUserIcon(context));
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$UserSettingsActivity"));
                startActivity(intent);
            }
        });
        //getLifecycle().addObserver(avatarViewMixin);


        showFragment(new TopLevelSettings(), R.id.main_content);
        ((FrameLayout) findViewById(R.id.main_content))
                .getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        homepageSpacer = findViewById(R.id.settings_homepage_spacer);
        homepageMainLayout = findViewById(R.id.main_content_scrollable_container);

        TextView tv = homepageSpacer.findViewById(R.id.spacer_text);
        iv = homepageSpacer.findViewById(R.id.spacer_image);
        mCustomImage = homepageSpacer.findViewById(R.id.custom_image);
        mStockDrawable = context.getDrawable(R.drawable.x_spacer);
        Drawable xtendedDrawable = context.getDrawable(R.drawable.xtended_dashboard_icon);
        try {
            XFontHelper.setFontType(tv, getFontStyle());
            if (configAnim() == 0) {
                 iv.setVisibility(View.VISIBLE);
                 if (isProfileAvatar() == 1) {
                     homepageSpacer.setBackground(null);
                     mCustomImage.setImageDrawable(null);
                     mCustomImage.setVisibility(View.GONE);
                     iv.setVisibility(View.VISIBLE);
                     iv.setImageDrawable(getCircularUserIcon(context));
                     iv.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$UserSettingsActivity"));
                            startActivity(intent);
                         }
                     });
                 } else if (isProfileAvatar() == 0) {
                     homepageSpacer.setBackground(null);
                     mCustomImage.setImageDrawable(null);
                     mCustomImage.setVisibility(View.GONE);
                     iv.setVisibility(View.VISIBLE);
                     if (mStockDrawable != null) {
                         iv.setImageDrawable(mStockDrawable);
                     }
                 } else if (isProfileAvatar() == 2) {
                     homepageSpacer.setBackground(null);
                     mCustomImage.setImageDrawable(null);
                     mCustomImage.setVisibility(View.GONE);
                     iv.setVisibility(View.VISIBLE);
                     if (xtendedDrawable != null) {
                         iv.setImageDrawable(xtendedDrawable);
                     }
                 } else if (isProfileAvatar() == 3) {
                     iv.setVisibility(View.GONE);
                     BitmapDrawable bp = getCustomImageFromString(SPACER_IMAGE, context);
                     if (bp == null) Log.d(TAG,"Bitmap is null!!");
                     if (bp != null)  {
                         if (isCrop() == 0){
                             homepageSpacer.setBackground(bp);
                             mCustomImage.setVisibility(View.GONE);
                          } else {
                             homepageSpacer.setBackground(null);
                             mCustomImage.setImageDrawable(bp);
                             mCustomImage.setVisibility(View.VISIBLE);
                          }
                     }
                 }
                 tv.setVisibility(View.GONE);
            } else if (configAnim() == 1) {
                 iv.setVisibility(View.GONE);
                 tv.setVisibility(View.VISIBLE);
            }
            if (avatarView != null) {
                avatarView.setVisibility(isSearchDisabled()? View.GONE: View.VISIBLE);
             }
        } catch (Exception e) {}
        if (!isHomepageSpacerEnabled() && homepageSpacer != null && homepageMainLayout != null) {
            homepageSpacer.setVisibility(View.GONE);
            setMargins(homepageMainLayout, 0,0,0,0);
        }
    }

    private void showFragment(Fragment fragment, int id) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final Fragment showFragment = fragmentManager.findFragmentById(id);

        if (showFragment == null) {
            fragmentTransaction.add(id, fragment);
        } else {
            fragmentTransaction.show(showFragment);
        }
        fragmentTransaction.commit();
    }

    public void saveCustomFileFromString(Uri fileUri, String fileName, Context mContext) {
        try {
            final InputStream fileStream = mContext.getContentResolver().openInputStream(fileUri);
            File file = new File(mContext.getFilesDir(), fileName);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = fileStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } catch (Exception e) {
        }
    }

    public BitmapDrawable getCustomImageFromString(String fileName, Context mContext) {
        String imageUri = Settings.System.getStringForUser(mContext.getContentResolver(),
                Settings.System.SETTINGS_SPACER_CUSTOM,
                UserHandle.USER_CURRENT);
        if (imageUri != null) {
            saveCustomFileFromString(Uri.parse(imageUri), SPACER_IMAGE, mContext);
        }
        BitmapDrawable mImage = null;
        File file = new File(mContext.getFilesDir(), fileName);
        if (file.exists()) {
            final Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
            mImage = new BitmapDrawable(mContext.getResources(), XImageUtils.resizeMaxDeviceSize(mContext, image));
        }
        return mImage;
    }

    private boolean isHomepageSpacerEnabled() {
        return Settings.System.getInt(this.getContentResolver(),
        Settings.System.SETTINGS_SPACER, 0) != 0;
    }

    private int configAnim() {
         return Settings.System.getInt(this.getContentResolver(),
                Settings.System.SETTINGS_SPACER_STYLE, 0);
    }

    private int getFontStyle() {
         return Settings.System.getInt(this.getContentResolver(),
                Settings.System.SETTINGS_SPACER_FONT_STYLE, 0);
    }

    private int isCrop() {
        return Settings.System.getInt(this.getContentResolver(),
        Settings.System.SETTINGS_SPACER_IMAGE_CROP, 1);
    }

    private int isProfileAvatar() {
        return Settings.System.getInt(this.getContentResolver(),
        Settings.System.SETTINGS_SPACER_IMAGE_STYLE, 0);
    }

    private boolean isSearchDisabled() {
        return Settings.System.getInt(this.getContentResolver(),
        Settings.System.SETTINGS_SPACER_IMAGE_SEARCHBAR, 0) == 1;
    }

    private static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    @VisibleForTesting
    void setHomepageContainerPaddingTop() {
        final View view = this.findViewById(R.id.homepage_container);

        final int searchBarHeight = getResources().getDimensionPixelSize(R.dimen.search_bar_height);
        final int searchBarMargin = getResources().getDimensionPixelSize(R.dimen.search_bar_margin);

        // The top padding is the height of action bar(48dp) + top/bottom margins(16dp)
        final int paddingTop = searchBarHeight + searchBarMargin * 2;
        view.setPadding(0 /* left */, paddingTop, 0 /* right */, 0 /* bottom */);

        // Prevent inner RecyclerView gets focus and invokes scrolling.
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

    private void setHomepageContainerPaddingTopA12() {
        final View view = findViewById(R.id.homepage_container);
        final AppBarLayout appBarLayout = findViewById(R.id.appbar_layout_a12);

        // use appBarLayout's current height
        appBarLayout.post(() -> {
            view.setPadding(0, appBarLayout.getMeasuredHeight(), 0, 0);
        });

        // Prevent inner RecyclerView gets focus and invokes scrolling.
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

    private Drawable getCircularUserIcon(Context context) {
        Bitmap bitmapUserIcon = mUserManager.getUserIcon(UserHandle.myUserId());

        if (bitmapUserIcon == null) {
            // get default user icon.
            final Drawable defaultUserIcon = UserIcons.getDefaultUserIcon(
                    context.getResources(), UserHandle.myUserId(), false);
            bitmapUserIcon = UserIcons.convertToBitmap(defaultUserIcon);
        }
        Drawable drawableUserIcon = new CircleFramedDrawable(bitmapUserIcon,
                bitmapUserIcon.getHeight());

        return drawableUserIcon;
    }

    @Override
    public void onResume() {
        super.onResume();
        avatarView.setImageDrawable(getCircularUserIcon(getApplicationContext()));
    }
}
