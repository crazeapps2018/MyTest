package com.it.mytest.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.it.mytest.MainActivity.Companion.mediaPlayer
import com.it.mytest.R
import com.it.mytest.util.Utility

class SongsAdapter(
    private var onItemClickListener: OnItemClickListener,
    private val mList: ArrayList<Uri>
) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    private var mItemClickListener: OnItemClickListener? = null
    private var selectedPosition = -1

    init {
        this.mItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(songsModel: Uri)
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_song, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = mList[position]

        holder.title.text = Utility.getNameFromUri(holder.title.context, data)


        if (selectedPosition == position) {
            holder.playButton.setImageResource(R.drawable.ic_pause)
        } else {
            holder.playButton.setImageResource(R.drawable.ic_play)
        }


        holder.playButton.setOnClickListener {

            if (selectedPosition >= 0)
                notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    holder.playButton.setImageResource(R.drawable.ic_pause)
                } else {
                    holder.playButton.setImageResource(R.drawable.ic_play)

                }
            } else {
                holder.playButton.setImageResource(R.drawable.ic_pause)

            }

            mItemClickListener!!.onItemClick(data)
        }

    }


    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val playButton: ImageButton = itemView.findViewById(R.id.btnPlay)
        val title: TextView = itemView.findViewById(R.id.title)
    }
}

