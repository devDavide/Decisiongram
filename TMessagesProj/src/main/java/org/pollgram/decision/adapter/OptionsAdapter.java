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
import org.pollgram.decision.data.PollgramException;
import org.pollgram.decision.data.TextOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davide on 23/12/15.
 */
public class OptionsAdapter extends ArrayAdapter<Option> {
    private static final int LAYOUT_RES_ID = R.layout.item_new_option_list;

    private final List<TextOption> options;
    private final LayoutInflater inflater;
    private final EditMode mode;
    private final int lastIdx;

    private enum EditMode{
        /**
         * It is possible to add  new option ad to delete as well
         */
        NEW_DECISION,

        /**
         * no edit action is allowed, no delete, no add
         */
        READ_ONLY,

        /**
         * it is only possible to add new option, it is not possible to modify or delete the existing
         */
        ALLOW_ADD_NEW_OPTION;
    }

    public OptionsAdapter(Context context) {
        this(context,new ArrayList<TextOption>(), EditMode.NEW_DECISION);
        // put one first empty option
        options.add(new TextOption());
    }

    public OptionsAdapter(Context context, List<TextOption> options, boolean editable){
        this(context,options, editable ? EditMode.ALLOW_ADD_NEW_OPTION : EditMode.READ_ONLY);
    }

    private OptionsAdapter(Context context, List<TextOption> options, EditMode mode){
        super(context, LAYOUT_RES_ID);
        this.options = options;
        this.lastIdx = options.size() -1;
        this.mode =mode;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * @return the list of the selected options
     * @throws PollgramException if some value is invalid
     */
    public List<Option> getOptions() throws PollgramException{
        List<Option> out = new ArrayList<Option>();
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
            if (EditMode.READ_ONLY.equals(mode))
                buttonAdd.setEnabled(false);
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

            boolean enable;
            switch (mode){
                case NEW_DECISION:
                    enable = true;
                    break;
                case READ_ONLY:
                     enable = false;
                    break;
                case ALLOW_ADD_NEW_OPTION:
                    enable = position > lastIdx;
                    break;
                default:
                    enable = false;

            }
            edTitle.setEnabled(enable);
            edLongDescription.setEnabled(enable);
            deleteItem.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);

        }

        return rowView;
    }
}

