package org.dhis2.data.forms.dataentry.fields;

import android.widget.ImageView;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.form.data.FieldUiModel;
import org.hisp.dhis.android.core.common.FeatureType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FormViewHolder extends RecyclerView.ViewHolder {

    private final ViewDataBinding binding;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        ImageView fieldSelected = binding.getRoot().findViewById(R.id.fieldSelected);
        if (fieldSelected != null) {
            ViewExtensionsKt.clipWithAllRoundedCorners(fieldSelected, ExtensionsKt.getDp(2));
        }
    }

    public void bind(FieldUiModel uiModel, FieldItemCallback callback) {
        FieldViewModel viewModel = (FieldViewModel) uiModel;
        FieldUiModel.Callback itemCallback = new FieldUiModel.Callback() {
            @Override
            public void mapRequest(@NotNull String coordinateFieldUid, @NotNull String featureType, @Nullable String initialCoordinates) {
                callback.onMapRequest(coordinateFieldUid, FeatureType.valueOfFeatureType(featureType), initialCoordinates);
            }

            @Override
            public void currentLocation(@NotNull String coordinateFieldUid) {
                callback.onCurrentLocationRequest(coordinateFieldUid);
            }

            @Override
            public void onNext() {
                callback.onNext(getLayoutPosition());
            }

            @Override
            public void showDialog(@NotNull String title, @Nullable String message) {
                callback.onShowDialog(title, message);
            }

        };
        viewModel.setCallback(itemCallback);

        binding.setVariable(BR.item, viewModel);
        binding.executePendingBindings();
    }

    public interface FieldItemCallback {
        void onShowDialog(String title, @Nullable String message);

        void onNext(int layoutPosition);

        void onMapRequest(@NotNull String coordinateFieldUid, @NotNull FeatureType featureType, @Nullable String initialCoordinates);

        void onCurrentLocationRequest(@NotNull String coordinateFieldUid);
    }
}
