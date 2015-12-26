package org.pollgram.decision.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.pollgram.R;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.TextOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davide on 23/12/15.
 */
public class NewOptionsAdapter extends ArrayAdapter<Option> {
    private static final int LAYOUT_RES_ID = R.layout.item_new_option_list;

    private final List<TextOption> options;
    private final LayoutInflater inflater;

    public NewOptionsAdapter(Context context) {
        super(context, LAYOUT_RES_ID);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        options = new ArrayList<>();
        // put one first empty option
        options.add(new TextOption());
    }

    public List<Option> getOptions() {
        List<Option> out = new ArrayList<Option>();
        for (TextOption to : options)
            out.add(to);
        return out;
    }

    @Override
    public int getCount() {
        return options.size() + 1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView;
        if (position == getCount() - 1) {
            rowView = inflater.inflate(R.layout.item_add_new_option_list, parent, false);
            Button buttonAdd = (Button) rowView.findViewById(R.id.new_option_add_button);
            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (options.get(position-1).getTitle() == null){
                        Toast.makeText(getContext(), R.string.pleaseSelectATitleForOption, Toast.LENGTH_LONG).show();
                    } else {
                        options.add(new TextOption());
                        notifyDataSetChanged();
                    }
                }
            });
        } else {
            // Create view for item
            rowView = inflater.inflate(LAYOUT_RES_ID, parent, false);
            EditText edTitle = (EditText) rowView.findViewById(R.id.new_option_ed_title);
            EditText edLongDescription = (EditText) rowView.findViewById(R.id.new_option_ed_long_description);
            ImageButton deleteItem = (ImageButton) rowView.findViewById(R.id.new_option_delete_button);

            // Set data
            final TextOption o = options.get(position);
            edTitle.setText(o.getTitle());
            edLongDescription.setText(o.getLongDescription());

            // Register listener
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    options.remove(position);
                    notifyDataSetChanged();
                }
            });
            edTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    o.setTitle(s.toString());
                }
            });
            edLongDescription.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    o.setLongDescription(s.toString());
                }
            });
        }

        return rowView;
    }
}

