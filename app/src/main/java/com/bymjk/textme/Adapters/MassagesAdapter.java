package com.bymjk.textme.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bymjk.textme.Models.Massage;
import com.bymjk.textme.R;
import com.bymjk.textme.databinding.ItemRecieveBinding;
import com.bymjk.textme.databinding.ItemSentBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

public class MassagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Massage> massages;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;
    String senderRoom ,receiverRoom;

    public MassagesAdapter(Context context, ArrayList<Massage> massages,String senderRoom,String receiverRoom) {
        this.context = context;
        this.massages = massages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent,parent,false);
            return new SentViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recieve,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Massage massage = massages.get(position);
        if(FirebaseAuth.getInstance().getUid() != null) {
            if (FirebaseAuth.getInstance().getUid().equals(massage.getSenderId())) {
                return ITEM_SENT;
            }
            else if(!FirebaseAuth.getInstance().getUid().equals(massage.getSenderId())){
                return ITEM_RECEIVE;
            }
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Massage massage = massages.get(position);

       /* int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if(holder.getClass() == SentViewHolder.class){
                SentViewHolder viewHolder = (SentViewHolder)holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else{
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);

            }
            massage.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("massages")
                    .child(massage.getMassageId()).setValue(massage);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("massages")
                    .child(massage.getMassageId()).setValue(massage);
            return true; // true is closing popup, false is requesting a new selection
        });*/

        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;

            if (massage.getMassage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.senderText.setVisibility(View.GONE);
                Glide.with(context)
                        .load(massage.getImageUrl())
                        .placeholder(R.drawable.placeholderimg2)
                        .into(viewHolder.binding.image);
            }

            viewHolder.binding.senderText.setText(massage.getMassage());

          /*  if(massage.getFeeling() >= 0){
//                massage.setFeeling(reactions[massage.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reactions[massage.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else{
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.senderText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });*/
        }
        else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            if (massage.getMassage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.recieverText.setVisibility(View.GONE);
                Glide.with(context)
                        .load(massage.getImageUrl())
                        .placeholder(R.drawable.placeholderimg2)
                        .into(viewHolder.binding.image);
            }

            viewHolder.binding.recieverText.setText(massage.getMassage());

         /*   if(massage.getFeeling() >= 0){
                viewHolder.binding.feeling.setImageResource(reactions[massage.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else{
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.recieverText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });*/
        }
    }

    @Override
    public int getItemCount() {
        return massages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{

        ItemSentBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        ItemRecieveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemRecieveBinding.bind(itemView);
        }
    }

}
