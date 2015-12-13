package org.pollgram.decision.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pollgram.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

/**
 * Created by davide on 13/12/15.
 */
public class NewDecisionFragment extends BaseFragment {

    public NewDecisionFragment(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        // put here fields init
        return true;
    }

    @Override
    public View createView(Context context) {
        fragmentView = new SizeNotifierFrameLayout(context);
        SizeNotifierFrameLayout contentView = (SizeNotifierFrameLayout) fragmentView;
        LayoutInflater li = LayoutInflater.from(context);
        ViewGroup myView = (ViewGroup) li.inflate(R.layout.new_decision_layout, (ViewGroup) fragmentView);

        DrawView dv = new DrawView(getParentActivity(), 10, 4, 7);
        myView.addView(dv, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        return  fragmentView;
    }
}
