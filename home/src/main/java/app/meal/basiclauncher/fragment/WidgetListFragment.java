package app.meal.basiclauncher.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import app.meal.basiclauncher.R;
import app.meal.basiclauncher.helper.WidgetListAdapter;
import app.meal.basiclauncher.helper.WidgetListLoader;
import app.meal.basiclauncher.view.CellLayout;

public class WidgetListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AppWidgetProviderInfo>> {

    private WidgetListAdapter viewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_widget_list, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        viewAdapter = new WidgetListAdapter(getActivity(), R.layout.item_widget_list, R.id.widgetListItemLabel, R.id.widgetListItemIcon, R.id.widgetListItemPicture);
        ListView listView = (ListView) rootView.findViewById(R.id.widgetList);
        listView.setAdapter(viewAdapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivityForResult(
                        new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
                                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, ((CellLayout) view).allocateAppWidgetId()),
                        R.integer.app_widget_signal_pick);
                getActivity().onBackPressed();
                return true;
            }
        });

        getLoaderManager().initLoader(0, savedInstanceState, this);
    }

    @Override
    public Loader<List<AppWidgetProviderInfo>> onCreateLoader(int id, Bundle args) {
        return new WidgetListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<AppWidgetProviderInfo>> loader, List<AppWidgetProviderInfo> data) {
        viewAdapter.setData(data);
        viewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<AppWidgetProviderInfo>> loader) {
        viewAdapter.setData(null);
        viewAdapter.notifyDataSetChanged();
    }
}
