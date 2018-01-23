package com.dhis2.usescases.searchTrackEntity;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.usescases.searchTrackEntity.formHolders.ButtonFormHolder;
import com.dhis2.usescases.searchTrackEntity.formHolders.CoordinatesFormHolder;
import com.dhis2.usescases.searchTrackEntity.formHolders.EditTextFormHolder;
import com.dhis2.usescases.searchTrackEntity.formHolders.EditTextHolder;
import com.dhis2.usescases.searchTrackEntity.formHolders.FormViewHolder;
import com.dhis2.usescases.searchTrackEntity.formHolders.SpinnerHolder;
import com.dhis2.usescases.searchTrackEntity.formHolders.DateTimeFormHolder;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public class FormAdapter extends RecyclerView.Adapter<FormViewHolder> {

    private final int EDITTEXT = 0;
    private final int BUTTON = 1;
    private final int CHECKBOX = 2;
    private final int SPINNER = 3;
    private final int COORDINATES = 4;
    private final int TIME = 5;
    private final int DATE = 6;
    private final int DATETIME = 7;
    private final int AGEVIEW = 8;
    private int programData = 0;
    private SearchTEContractsModule.Presenter presenter;
    private List<TrackedEntityAttributeModel> attributeList;
    private ProgramModel programModel;

    FormAdapter(SearchTEContractsModule.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public FormViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding;
        FormViewHolder holder;
        switch (viewType) {
            case DATE:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_date_text, parent, false);
                holder = new DateTimeFormHolder(binding);
                break;
            case DATETIME:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_date_time_text, parent, false);
                holder = new DateTimeFormHolder(binding);
                break;
            case EDITTEXT:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_edit_text_custom, parent, false);
                holder = new EditTextFormHolder(binding);
                break;
            case BUTTON:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_button_text, parent, false);
                holder = new ButtonFormHolder(binding);
                break;
            case CHECKBOX:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_check_box, parent, false);
                holder = new EditTextHolder(binding);
                break;
            case SPINNER:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_spinner, parent, false);
                holder = new SpinnerHolder(binding);
                break;
            case COORDINATES:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_coordinates, parent, false);
                holder = new CoordinatesFormHolder(binding);
                break;
            case TIME:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_edit_text_custom, parent, false);
                holder = new DateTimeFormHolder(binding);
                break;
            case AGEVIEW:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_age_custom, parent, false);
                holder = new EditTextFormHolder(binding);
                break;
            default:
                binding = DataBindingUtil.inflate(inflater, R.layout.form_spinner, parent, false);
                holder = new SpinnerHolder(binding);
                break;
        }

        return holder;

    }

    @Override
    public void onBindViewHolder(FormViewHolder holder, int position) {
        if (position < programData) {
            ((DateTimeFormHolder) holder).bindProgramData(presenter, position == 0 ? programModel.enrollmentDateLabel() : programModel.incidentDateLabel(), position);
        } else {
            holder.bind(presenter, attributeList.get(position - programData));
        }
    }

    @Override
    public int getItemCount() {
        return attributeList != null ? attributeList.size() + programData : programData;
    }

    @Override
    public int getItemViewType(int position) {

        if (position < programData)
            return DATE;

        if (attributeList.get(position - programData).optionSet() != null)
            return SPINNER;
        else {
            switch (attributeList.get(position - programData).valueType()) {
                case AGE:
                    return AGEVIEW;
                case TEXT:
                case EMAIL:
                case LETTER:
                case NUMBER:
                case INTEGER:
                case USERNAME:
                case LONG_TEXT:
                case PERCENTAGE:
                case PHONE_NUMBER:
                case INTEGER_NEGATIVE:
                case INTEGER_POSITIVE:
                case INTEGER_ZERO_OR_POSITIVE:
                case UNIT_INTERVAL:
                    return EDITTEXT;
                case TIME:
                    return TIME;
                case DATE:
                    return DATE;
                case DATETIME:
                    return DATETIME;
                case FILE_RESOURCE:
                    return BUTTON;
                case COORDINATE:
                    return COORDINATES;
                case BOOLEAN:
                    return CHECKBOX;
                case TRUE_ONLY:
                case TRACKER_ASSOCIATE:
                case ORGANISATION_UNIT:
                case URL:
                default:
                    return EDITTEXT;
            }
        }

    }

    public void setList(List<TrackedEntityAttributeModel> modelList, ProgramModel programModel) {

        if (programModel != null) {
            this.programModel = programModel;
            programData = programModel.displayIncidentDate() ? 2 : 1;
        } else {
            programData = 0;
            List<TrackedEntityAttributeModel> modelListnew = new ArrayList<>();
            for (TrackedEntityAttributeModel attributeModel : modelList) {
                if (attributeModel.displayInListNoProgram())
                    modelListnew.add(attributeModel);
            }
            modelList = new ArrayList<>(modelListnew);
        }


        this.attributeList = modelList;
        notifyDataSetChanged();
    }
}
