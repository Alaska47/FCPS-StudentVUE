package com.akotnana.fcpsstudentvue.fragments.gradebook;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.akotnana.fcpsstudentvue.R;
import com.akotnana.fcpsstudentvue.utils.BackendUtils;
import com.akotnana.fcpsstudentvue.utils.DataStorage;
import com.akotnana.fcpsstudentvue.utils.VolleyCallback;
import com.akotnana.fcpsstudentvue.utils.adapters.RVAdapterGrade;
import com.akotnana.fcpsstudentvue.utils.cards.GradeCourseCard;
import com.akotnana.fcpsstudentvue.utils.gson.Course;
import com.akotnana.fcpsstudentvue.utils.gson.Quarter;
import com.akotnana.fcpsstudentvue.utils.gson.User;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GradeBookQFragment extends Fragment {

    private static final String TAG = "GradeBookQFragment";
    int quarterIndex;
    public String quarterName;
    public String semesterName;
    public static String grades;
    private List<GradeCourseCard> gradesCards;
    private RecyclerView rv;
    private RVAdapterGrade adapter;

    public boolean hasAlreadyLoaded = false;

    public int visibleFragment = -1;

    public GradeBookQFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            //Log.d(TAG, "bundle not NULL");
            this.quarterIndex = bundle.getInt("index", 0);
            if (quarterIndex < 2) {
                this.semesterName = "S1";
            } else {
                this.semesterName = "S2";
            }
            switch (quarterIndex) {
                case 0:
                    quarterName = "Q1";
                    break;
                case 1:
                    quarterName = "Q2";
                    break;
                case 2:
                    quarterName = "Q3";
                    break;
                case 3:
                    quarterName = "Q4";
                    break;
                default:
                    break;
            }

            /*
            initializeData();
            initializeAdapter();
            */
        }

        //Log.d(TAG, "OnCreate called");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh: {
                Log.i("GradeBookQFragment", "Save from fragment " + quarterIndex);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_grade_book_q, container, false);

        rv = (RecyclerView) v.findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        initializeAdapter();
        initializeData();

        return v;

    }

    @Override
    public void onResume() {
        super.onResume();
        initializeData();
    }

    private void initializeAdapter() {
        adapter = new RVAdapterGrade(gradesCards);
        rv.setAdapter(adapter);
    }

    private void initializeData() {
        Log.d(TAG, "SelectedQuarter from " + quarterName + " : " + new DataStorage(getContext()).getData("selectedQuarter"));
        if (Integer.parseInt(new DataStorage(getContext()).getData("selectedQuarter")) == quarterIndex) {
            new DataStorage(getContext()).storeData("selectedQuarter", "-1", false);
            Log.d(TAG, quarterName + " is loading what is already loaded now!");
            gradesCards = new ArrayList<>();
            Gson gson = null;
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Quarter quarter = gson.fromJson(new DataStorage(getContext()).getData("GradeBook"), Quarter.class);
            Course[] courses = quarter.getCourses();
            for (Course course : courses) {
                gradesCards.add(new GradeCourseCard(course.getPeriodNumber(), course.getCourseName(), course.getTeacher(), course.getRoomNumber(), quarterName, semesterName, course.getGradePercentage(), "N/A", "N/A"));
            }
            hasAlreadyLoaded = true;
            initializeAdapter();
        } else {
            Log.d(TAG, quarterName + " has nothing to load!");
            gradesCards = new ArrayList<>();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(quarterName != null) {
                this.visibleFragment = Integer.parseInt(String.valueOf(quarterName.charAt(1))) - 1;
                Log.d(TAG, quarterName);
            }
            Log.d(TAG, "The index of the visible fragment is: " + this.visibleFragment + " called from fragment " + quarterIndex);
            if(getContext() != null) {
                //Log.d(TAG, Integer.parseInt(new DataStorage(getContext()).getData("selectedQuarter")) + "");
            }
            if(this.visibleFragment == quarterIndex && Integer.parseInt(new DataStorage(getContext()).getData("selectedQuarter")) == -1 && !hasAlreadyLoaded) {
                Log.d(TAG, quarterName + " is loading new now!");
                gradesCards = new ArrayList<>();
                Gson gson = null;
                try {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gson = gsonBuilder.create();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Loading...");
                progressDialog.show();
                final Gson finalGson = gson;
                BackendUtils.doPostRequest("/grades/quarter/" + quarterIndex, new HashMap<String, String>() {{
                }}, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d(TAG, result);
                        Quarter quarter = finalGson.fromJson(result, Quarter.class);
                        Course[] courses = quarter.getCourses();
                        for (Course course : courses) {
                            gradesCards.add(new GradeCourseCard(course.getPeriodNumber(), course.getCourseName(), course.getTeacher(), course.getRoomNumber(), quarterName, semesterName, course.getGradePercentage(), "N/A", "N/A"));
                        }
                        progressDialog.dismiss();
                        initializeAdapter();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        progressDialog.dismiss();
                    }
                }, getContext());

            }

        } else {
            gradesCards = new ArrayList<>();
            if(adapter != null)
                initializeAdapter();
            hasAlreadyLoaded = false;
        }

    }
}