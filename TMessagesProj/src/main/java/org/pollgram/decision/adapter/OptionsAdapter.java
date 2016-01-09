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
import org.pollgram.decision.data.DBBean;
import org.pollgram.decision.data.Option;
import org.pollgram.decision.data.PollgramException;
import org.pollgram.decision.data.TextOption;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by davide on 23/12/15.
 */
public class OptionsAdapter extends ArrayAdapter<Option> {
    private static final int LAYOUT_RES_ID = R.layout.item_new_option_list;

    private final List<TextOption> options;
    private final LayoutInflater inflater;
    private int lastIdx;
    private final boolean editable;
    private List<TextOption> deletedOptions;


    public OptionsAdapter(Context context) {
        this(context,new ArrayList<TextOption>(),true);
        // put one first empty option
        options.add(new TextOption());
    }

    public OptionsAdapter(Context context, List<TextOption> options, boolean editable){
        super(context, LAYOUT_RES_ID);
        this.options = options;
        this.deletedOptions  = new ArrayList<>();
        this.lastIdx = options.size() -1;
        this.editable =editable;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    /**
     * @return the list of the selected options
     * @throws PollgramException if some value is invalid
     */
    public List<Option> getOptions() throws PollgramException{
        Set<String> titleSet = new HashSet<>();
        List<Option> out = new ArrayList<Option>();

        // check for duplicated titles
        for(Option o : options){
            if (titleSet.contains(o.getTitle())){
                throw new PollgramException(getContext().getString(R.string.titleAlreadyPresent,o.getTitle()));
            }
            titleSet.add(o.getTitle());
        }

        for (int i = lastIdx + 1 ; i<options.size() ; i++)
            out.add(options.get(i));

        for (int i = 0; i < out.size(); i++) {
            String title = out.get(i).getTitle();
            if (title == null || title.trim().isEmpty()) {
                if (i == out.size() -1)
                    throw new PollgramException(getContext().getString(R.string.emptyTitleOnLastOption));
                else
                    throw new PollgramException(getContext().getString(R.string.emptyTitleOnOption, i + 1));
            }
        }
        return out;
    }

    public List<Option> getDeletedOptions() {
        List<Option> out = new ArrayList<Option>();
        for (TextOption to : deletedOptions)
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
            buttonAdd.setEnabled(editable);
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
            // make grey the lines that are already present and therefore not editable
            if(o.getId() != DBBean.ID_NOT_SET) {
//                rowView.setBackgroundColor(Color.LTGRAY);
                // prevent EditText hint to be showed for existing decision with not long description
                if (o.getLongDescription() != null || o.getLongDescription().isEmpty()) {
                    edLongDescription.setText(" ");
                }
            }


            // Register listener
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position <= lastIdx) {
                        lastIdx--;
                        deletedOptions.add(options.get(position));
                    }
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

            boolean enableFields = position > lastIdx && editable;
            edTitle.setEnabled(enableFields);
            edLongDescription.setEnabled(enableFields);
            deleteItem.setVisibility(editable ? View.VISIBLE : View.INVISIBLE);

        }

        return rowView;
    }
}

