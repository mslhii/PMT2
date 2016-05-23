package com.kritikalerror.pmt2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.kritikalerror.pmt2.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Set;

/**
 * @author Michael H.
 */
public class FriendListViewAdapter extends BaseAdapter implements SectionIndexer {

    private static LayoutInflater mLayoutInflater;
    private ArrayList<String> mParseUsers;

    // Member variables for section indexing
    private String[] mSections;
    HashMap<String, Integer> mMapIndex;

    private static final int[] colorPicker = {Color.parseColor("#9933CC"),
            Color.parseColor("#669900"),
            Color.parseColor("#0099CC"),
            Color.DKGRAY,
            Color.parseColor("#FF8800"),
            Color.parseColor("#CC0000"),
            Color.parseColor("#FFDE82")};

    private static boolean[] colorUsage = {false,
            false,
            false,
            false,
            false,
            false,
            false};

    private Context context;
    private int mLength;

    private static final String ADMOB_PUBLISHER_ID = "ca-app-pub-6309606968767978/8492050042";

    public FriendListViewAdapter(Context ctx, ArrayList<String> parseUsers) {
        this.context = ctx;
        this.mLength = parseUsers.size();
        this.mLayoutInflater = LayoutInflater.from(ctx);
        this.mParseUsers = parseUsers;

        this.mParseUsers.add(0, null);

        this.mMapIndex = new LinkedHashMap<String, Integer>();

        for (int x = 1; x < parseUsers.size(); x++) {
            String firstChar = parseUsers.get(x).substring(0, 1);
            mMapIndex.put(firstChar, x);
        }

        Set<String> sectionLetters = mMapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

        Log.d("sectionList", sectionList.toString());
        Collections.sort(sectionList);

        mSections = new String[sectionList.size()];

        sectionList.toArray(mSections);
    }

    @Override
    public int getCount() {
        return mParseUsers != null ? mParseUsers.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mParseUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String friend = (String) getItem(position);

        Log.e("PMTFA", "position is: " + position);

        // Set up the ad for position 0
        if (position == 0) {
            if (convertView == null || !(convertView instanceof AdView)) {
                AdView adView = new AdView(this.context);
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(ADMOB_PUBLISHER_ID);
                AdRequest.Builder adRequestBuilder = new AdRequest.Builder();

                for (int i = 0; i < adView.getChildCount(); i++) {
                    adView.getChildAt(i).setFocusable(false);
                }

                adView.setFocusable(false);

                float density = this.context.getResources().getDisplayMetrics().density;
                int height = Math.round(AdSize.BANNER.getHeight() * density);
                AbsListView.LayoutParams params
                        = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,
                        height);
                adView.setLayoutParams(params);
                adView.loadAd(adRequestBuilder.build());

                convertView = adView;
            }

            //mMapIndex.put("a", position);
        }
        // This is for all the other users
        else {
            if (convertView == null || convertView instanceof AdView) {
                convertView = mLayoutInflater.inflate(R.layout.list_item_friend, parent, false);

                holder = new ViewHolder();
                holder.username = (TextView) convertView.findViewById(R.id.friend_username);

                convertView.setTag(holder);

                // Generate some nice backgrounds per user
                convertView.setBackgroundColor(determineColor());
                Log.e("PMTFA", "crashes in convertview");
            } else {
                holder = (ViewHolder) convertView.getTag();
                Log.e("PMTFA", "crashes in else");
            }

            holder.username.setText(friend.toUpperCase());
        }

        return convertView;
    }

    /**
     * Function to pick color in sequential order based on the boolean
     * @return
     */
    public static int determineColor()
    {
        for (int i = 0; i < colorUsage.length; i++)
        {
            if (!colorUsage[i])
            {
                colorUsage[i] = true;
                return colorPicker[i];
            }
        }

        for (int j = 1; j < colorUsage.length; j++)
        {
            colorUsage[j] = false;
        }
        return colorPicker[0];
    }

    public static class ViewHolder {
        public TextView username;
    }

    public int getPositionForSection(int section) {
        Log.d("section", "" + section);
        return mMapIndex.get(mSections[section]);
    }

    public int getSectionForPosition(int position) {
        Log.d("position", "" + position);
        return 0;
    }

    public Object[] getSections() {
        return mSections;
    }

}
