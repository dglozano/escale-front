package com.dglozano.escale.ui.main.diet.all;

import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Diet;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.web.DownloadService;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AllDietsListAdapter extends RecyclerView.Adapter<AllDietsListAdapter.DietViewHolder> {

    @Inject
    SimpleDateFormat simpleDateFormat;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @BindDrawable(R.drawable.ic_diet_download)
    Drawable downloadIcon;
    @BindDrawable(R.drawable.ic_diet_cancel)
    Drawable cancelDownloadIcon;
    @BindDrawable(R.drawable.ic_diet_delete)
    Drawable deleteIcon;

    private List<Diet> mDietsList;
    private DietClickListener mClickListener;
    private AllDietsViewModel mAllDietsViewModel;
    private Context mContext;

    @Inject
    public AllDietsListAdapter(MainActivity mainActivity, AllDietsViewModel allDietsViewModel) {
        this.mContext = mainActivity;
        this.mAllDietsViewModel = allDietsViewModel;
        ButterKnife.bind(this, mainActivity);
    }

    void setDietClickListener(DietClickListener dietClickListener) {
        this.mClickListener = dietClickListener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public DietViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diet_row_item, parent, false);
        return new DietViewHolder(itemView, mClickListener);
    }

    public void setItems(List<Diet> dietList) {
        /*if (mDietsList == null) {
            mDietsList = dietList;
            notifyItemRangeInserted(0, dietList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mDietsList.size();
                }

                @Override
                public int getNewListSize() {
                    return dietList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    Timber.d("New diet %s - Old diet %s", mDietsList.get(oldItemPosition).getId(), dietList.get(newItemPosition).getId());
                    return mDietsList.get(oldItemPosition).getId().equals(
                            dietList.get(newItemPosition).getId());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Diet newDiet = dietList.get(newItemPosition);
                    Diet oldDiet = mDietsList.get(oldItemPosition);
                    Timber.d("New diet %s - Status %s", newDiet.getFileName(), newDiet.getFileStatus());
                    Timber.d("New diet %s - Status %s", oldDiet.getFileName(), oldDiet.getFileStatus());
                    return newDiet.getId().equals(oldDiet.getId())
                            && newDiet.getFileStatus().equals(oldDiet.getFileStatus())
                            && newDiet.getFileName().equals(oldDiet.getFileName());
                }
            });
        result.dispatchUpdatesTo(this);*/
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
            if(position != 0) {
                // If it is not the current diet, then show the action button.
                switch (diet.getFileStatus()) {
                    case NOT_DOWNLOADED:
                        holder.mDietBtnImageView.setVisibility(View.VISIBLE);
                        holder.mProgressBar.setVisibility(View.INVISIBLE);
                        holder.mDietBtnImageView.setImageDrawable(downloadIcon);
                        break;
                    case DOWNLOADING:
                        holder.mDietBtnImageView.setVisibility(View.GONE);
                        holder.mProgressBar.setVisibility(View.VISIBLE);
                        holder.mDietBtnImageView.setImageDrawable(cancelDownloadIcon);
                        break;
                    case DOWNLOADED:
                        holder.mDietBtnImageView.setVisibility(View.VISIBLE);
                        holder.mProgressBar.setVisibility(View.INVISIBLE);
                        holder.mDietBtnImageView.setImageDrawable(deleteIcon);
                        break;
                }
            } else {
                holder.mProgressBar.setVisibility(
                        diet.getFileStatus().equals(Diet.FileStatus.DOWNLOADING) ? View.VISIBLE : View.GONE);
                holder.mDietBtnImageView.setVisibility(View.GONE);
            }

            holder.mDietBtnImageView.setOnClickListener(dietHolderDownloadBtnListener(diet));
        }
    }

    @NonNull
    private View.OnClickListener dietHolderDownloadBtnListener(Diet diet) {
        return v -> {
            switch (diet.getFileStatus()) {
                case NOT_DOWNLOADED:
                    startDownload(diet);
                    break;
                case DOWNLOADED:
                    mAllDietsViewModel.deleteDownload(diet);
                    break;
                case DOWNLOADING:
                    // TODO: add cancel download?
                    break;
            }
        };
    }

    private void startDownload(Diet diet) {
        Intent startIntent = new Intent(mContext,
                DownloadService.class);
        startIntent.putExtra("diet-uuid", diet.getId());
        diet.setFileStatus(Diet.FileStatus.DOWNLOADING);
        mAllDietsViewModel.updateDiet(diet);
        mContext.startService(startIntent);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDietsList != null) {
            return mDietsList.size();
        } else return 0;
    }

    public interface DietClickListener {

        void onClick(Diet diet);
    }

    public class DietViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.diet_item_btn)
        ImageView mDietBtnImageView;
        @BindView(R.id.diet_item_file_name)
        TextView mDietFileName;
        @BindView(R.id.diet_item_start_date)
        TextView mDietStartDate;
        @BindView(R.id.progress_bar_diet_item)
        ProgressBar mProgressBar;

        DietClickListener mListener;

        DietViewHolder(View v, DietClickListener listener) {
            super(v);
            ButterKnife.bind(this, v);
            mListener = listener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            RecyclerView rv = (RecyclerView) v.getParent();
            int itemPosition = rv.getChildLayoutPosition(v);
            Diet diet = mDietsList.get(itemPosition);
            mListener.onClick(diet);
        }
    }
}
