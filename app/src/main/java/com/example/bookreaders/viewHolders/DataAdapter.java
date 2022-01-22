package com.example.bookreaders.viewHolders;

import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookreaders.Interfaces.ItemClickListener;
import com.example.bookreaders.R;

public class DataAdapter extends RecyclerView.ViewHolder  implements View.OnClickListener
{
    public TextView nameOfBook,nameOfAuthor;
    public ImageView imageOfBook;
    private ItemClickListener itemClickListener;

    public DataAdapter(@NonNull View itemView) {
        super(itemView);
        nameOfBook = itemView.findViewById(R.id.header_single_row);
        nameOfAuthor=itemView.findViewById(R.id.footer_single_row);
        imageOfBook= itemView.findViewById(R.id.bookImage_single_row);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


}
