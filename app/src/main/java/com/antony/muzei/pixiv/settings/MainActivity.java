/*
 *     This file is part of PixivforMuzei3.
 *
 *     PixivforMuzei3 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program  is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.antony.muzei.pixiv.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.antony.muzei.pixiv.R;
import com.antony.muzei.pixiv.util.IntentUtils;
import com.google.android.apps.muzei.api.MuzeiContract;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Autogenerated code that sets up the Activity and the tabs
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // If Muzei is not installed, this will redirect the user to Muzei's Play Store listing
        if (!isMuzeiInstalled()) {
            // TODO localize these strings
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialogTitle_muzeiNotInstalled))
                    .setMessage(getString(R.string.dialog_installMuzei))
                    .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                        if (!IntentUtils.launchActivity(
                                this,
                                new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.nurik.roman.muzei"))
                        )) {
                            Intent fallback = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=net.nurik.roman.muzei"));
                            IntentUtils.launchActivity(this, fallback);
                        }
                    })
                    .setNegativeButton(R.string.dialog_no, (dialog, which) -> {
                        // Do nothing
                        dialog.dismiss();
                    })
                    .show();
        } else if (!isProviderSelected()) {
            new AlertDialog.Builder(this)
                    .setTitle(getApplicationContext().getString(R.string.dialogTitle_muzeiNotActiveSource))
                    .setMessage(getApplicationContext().getString(R.string.dialog_selectSource))
                    .setNeutralButton(android.R.string.ok, (dialog, which) ->
                    {
                        Intent intent = MuzeiContract.Sources.createChooseProviderIntent("com.antony.muzei.pixiv.provider");
                        startActivity(intent);
                    })
                    .show();
        }
    }

    // Checks if Muzei is installed
    private boolean isMuzeiInstalled() {
        boolean found = true;
        try {
            getApplicationContext().getPackageManager().getPackageInfo("net.nurik.roman.muzei", 0);
        } catch (PackageManager.NameNotFoundException ex) {
            found = false;
        }
        return found;
    }

    // Does a check to see if PixivForMuzei3 is selected as the active provider in Muzei
    private boolean isProviderSelected() {
        Cursor authorityCursor = getApplicationContext()
                .getContentResolver()
                .query(MuzeiContract.Sources.getContentUri(),
                        new String[]{MuzeiContract.Sources.COLUMN_NAME_AUTHORITY},
                        null,
                        null,
                        null);

        if (authorityCursor == null) {
            return false;
        }

        int authorityColumn;
        try {
            authorityColumn = authorityCursor.getColumnIndex(MuzeiContract.Sources.COLUMN_NAME_AUTHORITY);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return true;
        }
        while (authorityCursor.moveToNext()) {
            String selectedAuthority = authorityCursor.getString(authorityColumn);
            if (selectedAuthority.equals("com.antony.muzei.pixiv.provider")) {
                authorityCursor.close();
                return true;
            }
        }
        authorityCursor.close();
        return false;
    }
}
