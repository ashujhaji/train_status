package io.tnine.trainstatus.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

import io.tnine.trainstatus.Models.ModelPnrStatus.Passenger;
import io.tnine.trainstatus.Models.ModelPnrStatus.PnrStatusModel;
import io.tnine.trainstatus.R;

public class PnrStatusAdapter extends RecyclerView.Adapter<PnrStatusAdapter.MyViewHolder> {
    List<Passenger> pList;
    Context context;

    public PnrStatusAdapter(List<Passenger> pList, Context context){
        this.pList = pList;
        this.context = context;
    }
    @Override
    public PnrStatusAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pnr_status_item, parent, false);
        PnrStatusAdapter.MyViewHolder holder = new PnrStatusAdapter.MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(PnrStatusAdapter.MyViewHolder holder, int position) {
        if (pList.size()!=0) {
            holder.passen_count.setText(pList.get(position).getNo().toString());
            holder.book_status_count.setText(pList.get(position).getBookingStatus().toString());
            holder.curr_status_count.setText(pList.get(position).getCurrentStatus().toString());
        }
    }

    @Override
    public int getItemCount() {
        return pList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
    TextView passen_count, book_status_count, curr_status_count;
        public MyViewHolder(View itemView) {
            super(itemView);
            passen_count = (TextView)itemView.findViewById(R.id.passen_count);
            book_status_count = (TextView)itemView.findViewById(R.id.book_status_count);
            curr_status_count = (TextView)itemView.findViewById(R.id.curr_status_count);

        }
    }
}
