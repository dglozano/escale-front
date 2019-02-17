package com.dglozano.escale.ui.main.diet;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.ui.main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DietListAdapter extends RecyclerView.Adapter<DietListAdapter.DietViewHolder> {

    @Inject
    SimpleDateFormat simpleDateFormat;
    @BindDrawable(R.drawable.ic_diet_download)
    Drawable downloadIcon;
    @BindDrawable(R.drawable.ic_diet_cancel)
    Drawable cancelDownloadIcon;
    @BindDrawable(R.drawable.ic_diet_delete)
    Drawable deleteIcon;

    private List<Diet> mDietsList;

    @Inject
    public DietListAdapter(MainActivity mainActivity) {
        ButterKnife.bind(this, mainActivity);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public DietViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                    int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diet_row_item, parent, false);

        return new DietViewHolder(itemView);
    }

    public void setItems(List<Diet> dietList) {
        mDietsList = dietList;
        notifyDataSetChanged();
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull DietViewHolder holder, int position) {
        if (mDietsList != null) {
            final Diet diet = mDietsList.get(position);
            holder.mDietFileName.setText(diet.getFileName());
            holder.mDietStartDate.setText(String.format("Empezada el %shs.",
                    simpleDateFormat.format(diet.getStartDate())));
            switch (diet.getFileStatus()) {
                case NOT_DOWNLOADED:
                    holder.mDietBtnImageView.setImageDrawable(downloadIcon);
                    break;
                case DOWNLOADING:
                    holder.mDietBtnImageView.setImageDrawable(cancelDownloadIcon);
                    break;
                case DOWNLOADED:
                    holder.mDietBtnImageView.setImageDrawable(deleteIcon);
                    break;
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDietsList != null) {
            return mDietsList.size();
        } else return 0;
    }

    static class DietViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.diet_item_btn)
        ImageView mDietBtnImageView;
        @BindView(R.id.diet_item_file_name)
        TextView mDietFileName;
        @BindView(R.id.diet_item_start_date)
        TextView mDietStartDate;

        DietViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
