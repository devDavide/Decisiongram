package org.decisiongram.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.decisiongram.R;
import org.decisiongram.data.DBBean;
import org.decisiongram.data.Option;
import org.decisiongram.data.DecisiongramException;
import org.decisiongram.data.TextOption;
import org.decisiongram.ui.DefaultTextWatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by davide on 23/12/15.
 */
public class OptionsAdapter extends ArrayAdapter<Option> {
    private static final int LAYOUT_RES_ID = R.layout.item_new_option_list;

    private final LinkedList<TextOption> options;
    private final LayoutInflater inflater;
    private final boolean editable;
    private List<TextOption> deletedOptions;


    public OptionsAdapter(Context context) {
        this(context, new LinkedList<TextOption>(), true);
    }

    public OptionsAdapter(Context context, List<TextOption> options, boolean editable){
        super(context, LAYOUT_RES_ID);
        this.options = new LinkedList<>(options);
        this.deletedOptions  = new ArrayList<>();
        this.editable = editable;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    /**
     * @return the list of the selected options
     * @throws DecisiongramException if some value is invalid
     */
    public List<Option> getNewOptions() throws DecisiongramException {
        Set<String> titleSet = new HashSet<>();
        // check for duplicated titles
        for(Option o : options){
            if (titleSet.contains(o.getTitle())){
                throw new DecisiongramException(getContext().getString(R.string.titleAlreadyPresent,o.getTitle()));
            }
            titleSet.add(o.getTitle());
        }

        List<Option> out = new ArrayList<Option>();
        for (int i = 0; i < options.size(); i++) {
            TextOption opt = options.get(i);
            String title = opt.getTitle();
            if (title == null || title.trim().isEmpty()) {
                if (i == 0)
                    throw new DecisiongramException(getContext().getString(R.string.emptyTitleOnLastOption));
                else
                    throw new DecisiongramException(getContext().getString(R.string.emptyTitleOnOption, i + 1));
            }
            if (opt.getId() == DBBean.ID_NOT_SET)
                out.add(opt);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        if (position == 0) {
            rowView = inflater.inflate(R.layout.item_add_new_option_list, parent, false);
            Button buttonAdd = (Button) rowView.findViewById(R.id.new_option_add_button);
            buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextOption first = options.size() == 0 ? null : options.getFirst();
                    if (first != null && (first.getTitle() == null || first.getTitle().isEmpty())){
                        Toast.makeText(getContext(), R.string.pleaseSelectATitleForOption, Toast.LENGTH_LONG).show();
                    } else {
                        options.addFirst(new TextOption());
                        notifyDataSetChanged();
                    }
                }
            });
            buttonAdd.setEnabled(editable);
        } else {
            // Create view for item
            rowView = inflater.inflate(LAYOUT_RES_ID, parent, false);
            EditText edTitle = (EditText) rowView.findViewById(R.id.new_option_ed_title);
            final EditText edLongDescription = (EditText) rowView.findViewById(R.id.new_option_ed_long_description);
            ImageButton deleteItem = (ImageButton) rowView.findViewById(R.id.new_option_delete_button);

            // Set data
            final int optionPos = position - 1;
            final TextOption o = options.get(optionPos);
            final boolean alreadyOnDB = o.getId() != DBBean.ID_NOT_SET;

            // Register listener
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextOption optionToDelete = options.get(optionPos);
                    if (alreadyOnDB) {
                        deletedOptions.add(optionToDelete);
                    }
                    options.remove(optionPos);
                    notifyDataSetChanged();
                }
            });
            edTitle.addTextChangedListener(new DefaultTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    o.setTitle(s.toString());
                }
            });
            edLongDescription.addTextChangedListener(new DefaultTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    o.setNotes(s.toString());
                    Linkify.addLinks(edLongDescription, Linkify.ALL);
                }
            });

            edTitle.setText(o.getTitle());
            edLongDescription.setText(o.getNotes());
            // make grey the lines that are already present and therefore not editable
            if(alreadyOnDB) {
                edTitle.setTextColor(Color.GRAY);
                edLongDescription.setTextColor(Color.GRAY);
                if (o.getNotes() == null || o.getNotes().isEmpty()) {
                    edLongDescription.setText(" ");
                }
            }

            boolean enableFields = !alreadyOnDB && editable;
            edTitle.setEnabled(enableFields);
            edLongDescription.setEnabled(enableFields);
            deleteItem.setVisibility(editable ? View.VISIBLE : View.INVISIBLE);
            if (!enableFields){
                edTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), R.string.titleNotEditable,Toast.LENGTH_SHORT).show();
                    }
                });
                edLongDescription.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), R.string.longDescNotEditable,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (o.getTitle() == null || o.getTitle().isEmpty())
                edTitle.requestFocus();
        }

        return rowView;
    }
}

