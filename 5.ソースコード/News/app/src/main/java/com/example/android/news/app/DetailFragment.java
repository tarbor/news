package com.example.android.news.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.news.app.data.NewsContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private static final String[] NEWS_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            NewsContract.NewsEntry._ID,
            NewsContract.NewsEntry.COLUMN_LISTINDEX,
            NewsContract.NewsEntry.COLUMN_LINK,
            NewsContract.NewsEntry.COLUMN_DATE,
            NewsContract.NewsEntry.COLUMN_TITLE,
            NewsContract.NewsEntry.COLUMN_CONTENT
    };
    static final int COL_NEWS_ID = 0;
    static final int COL_NEWS_LISTINDEX = 1;
    static final int COL_NEWS_LINK = 2;
    static final int COL_NEWS_DATE = 3;
    static final int COL_NEWS_TITLE = 4;
    static final int COL_NEWS_CONTENT = 5;


    private String mListIndex;

    public DetailFragment() {
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // Loader初期化
        getLoaderManager().initLoader(DETAIL_LOADER,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        mListIndex = String.valueOf(0);
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mListIndex = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        Uri newsListIndexUri = NewsContract.NewsEntry.buildNewsUri(Integer.parseInt(mListIndex));

        return new CursorLoader(this.getActivity(),
                newsListIndexUri,
                NEWS_COLUMNS,
                mListIndex,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                TextView view = (TextView) getView().findViewById(R.id.detail_text);
                view.setText(cursor.getString(COL_NEWS_CONTENT));
                if (getView().findViewById(R.id.detail_text_link) != null) {
                    TextView viewLink = (TextView) getView().findViewById(R.id.detail_text_link);
                    viewLink.setText("\n" + cursor.getString(COL_NEWS_LINK));
                }
                if (getView().findViewById(R.id.detail_text_date) != null) {
                    TextView viewDate = (TextView) getView().findViewById(R.id.detail_text_date);
                    viewDate.setText("\n" + cursor.getString(COL_NEWS_DATE));
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
