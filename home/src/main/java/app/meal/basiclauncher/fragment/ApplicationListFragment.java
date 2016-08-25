package app.meal.basiclauncher.fragment;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.List;

import app.meal.basiclauncher.MainLauncherActivity;
import app.meal.basiclauncher.R;
import app.meal.basiclauncher.SettingsActivity;
import app.meal.basiclauncher.helper.ApplicationData;
import app.meal.basiclauncher.helper.ApplicationListAdapter;
import app.meal.basiclauncher.helper.ApplicationListLoader;

public class ApplicationListFragment extends Fragment implements LoaderCallbacks<List<ApplicationData>> {

    private ApplicationListAdapter viewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_application_list, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        viewAdapter = new ApplicationListAdapter(getActivity(), R.layout.item_application_list, R.id.applicationListItemLabel, R.id.applicationListItemIcon);

        View settingsButton = rootView.findViewById(R.id.launcherSettings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });

        GridView gridView = (GridView) rootView.findViewById(R.id.applicationList);
        gridView.setAdapter(viewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = viewAdapter.getItem(i).getIntent();
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), viewAdapter.getItem(i).getPackageName(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.startDrag(
                        ClipData.newPlainText(getString(R.string.copy_action)+"."+getActivity().getPackageName(), null),
                        new View.DragShadowBuilder(),//new View.DragShadowBuilder(view),
                        view, 0);
                ((MainLauncherActivity) getActivity()).closeDrawer();
                return true;
            }
        });

        getLoaderManager().initLoader(0, savedInstanceState, this);
    }

    @Override
    public Loader<List<ApplicationData>> onCreateLoader(int i, Bundle bundle) {
        return new ApplicationListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ApplicationData>> loader, List<ApplicationData> data) {
        viewAdapter.setData(data);
        viewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<ApplicationData>> loader) {
        viewAdapter.setData(null);
        viewAdapter.notifyDataSetChanged();
    }
}
