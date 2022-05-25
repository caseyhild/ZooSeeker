package com.example.zooseeker_cse_110_team_27;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchListActivity extends AppCompatActivity implements SearchListAdapter.ItemCallback{
    public RecyclerView recyclerView;
    private SearchView searchView;
    private SearchListViewModel viewModel;
    private Button addExhibitButton;
    private Button planRouteButton;
    private TextView deleteButton;
    private ArrayList<Exhibit> exhibits;
    private ArrayList<Exhibit> displayedExhibits;
    private List<SearchListItem> exhibitsinList;
    private Map<String, String> exhibitIdMap;
    private SearchListAdapter adapter;
    private TextView numExhibits;
    private Map<String,String> exhibitTagMap;
    private Map<String,String> selectedMap;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        viewModel = new ViewModelProvider(this).get(SearchListViewModel.class);

        Intent intent = getIntent();
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }

        adapter = new SearchListAdapter(this);
        //adapter.setOnDeleteButtonClicked(viewModel::deleteSearchExhibit);
        adapter.setHasStableIds(true);
        adapter.setOnCheckBoxClickedHandler(viewModel::toggleCompleted);
        viewModel.getSearchListItems().observe(this, adapter::setSearchListItems);
        recyclerView = findViewById(R.id.search_list_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        exhibits = (ArrayList<Exhibit>) Exhibit.loadJSONForSearching(this, "sample_node_info.json");
        displayedExhibits = new ArrayList<>(exhibits);
        exhibitTagMap = Exhibit.getSearchMap(exhibits);
        exhibitIdMap = Exhibit.getIdMap(exhibits);
        selectedMap = new HashMap<>();
        populateSelectedMap();

        for(String s : selectedMap.keySet()) { Log.d("key",s); }

        this.searchView = this.findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener(){
                    @Override
                    public boolean onQueryTextChange(String newText){
                        //text changes, search exhibitTagMap, update list_view
                        Log.d("tag",newText);
                        saveSelected();
                        updateExhibitList(newText);
                        populateDisplay();
                        return false;
                    }
                    @Override
                    public boolean onQueryTextSubmit(String query){
                        //perform the final search
                        return false;
                    }
                }
        );

        this.planRouteButton = this.findViewById(R.id.plan_route_btn);
        planRouteButton.setOnClickListener(this::onPlanClicked);

        this.numExhibits = this.findViewById(R.id.num_exhibits_view);

        updateTextView();
        saveSelected();
        populateDisplay();
    }

    private void doMySearch(String query) {
        //TODO
        //use query to check the exhibit tag map for matches, tell list_view
        //to display matches
    }

    private void onPlanClicked(View view) {
        Intent intent = new Intent(SearchListActivity.this, PlanRouteActivity.class);
        ArrayList<String> passExhibitNames = new ArrayList<>();
        for (SearchListItem e : exhibitsinList) {
            if(e.selected) {
                String id = exhibitIdMap.get(e.exhibitName);
                passExhibitNames.add(id);
            }
        }
        if(passExhibitNames.size() != 0) {
            intent.putExtra("key", passExhibitNames);
            startActivity(intent);
        }
    }

    @Override
    public void updateTextView() {
        String update = adapter.getSelected() + "";
        numExhibits.setText(update);
        saveSelected();
    }

    public void getExhibitsinList(List<SearchListItem> exhibitList) {
        exhibitsinList = exhibitList;
    }

    public List<SearchListItem> returnExhibitList() {
        return exhibitsinList;
    }

    public void updateExhibitList(String filter) {
        displayedExhibits = new ArrayList<>();
        for(Exhibit e : exhibits) {
            Log.d("tag",e.name);
            if(e.name.toLowerCase().contains(filter.toLowerCase())) {
                Log.d("tag","contained");
                displayedExhibits.add(e);
            }
        }
        saveSelected();
    }

    public void populateDisplay() {
        clearDisplay();
        for(int i = 0; i < displayedExhibits.size(); i++)
        {
            Exhibit e = displayedExhibits.get(i);
            List<String> list = viewModel.getNames();
            if(!list.contains(e.name) && e.kind.equals("exhibit")) {
                if(selectedMap.get(e.name).equals("true"))
                    viewModel.createExhibit(e.name,true);
                else
                    viewModel.createExhibit(e.name,false);
            }
        }
        updateTextView();
    }

    public void populateSelectedMap() {
        for(Exhibit e : exhibits) {
            selectedMap.put(e.name,"false");
        }
    }

    public void saveSelected() {
        for(SearchListItem s : adapter.getList()) {
            if(s.selected)
                selectedMap.put(s.exhibitName, "true");
            else
                selectedMap.put(s.exhibitName, "false");
        }
    }

    public void clearDisplay() { viewModel.clearList(); }
}


