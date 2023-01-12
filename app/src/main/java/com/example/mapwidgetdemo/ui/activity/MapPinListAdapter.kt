package com.example.mapwidgetdemo.ui.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.ui.activity.MapPinListAdapter.ImageViewHolder
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel


class MapPinListAdapter(val mcontext: Context, myViewClickListener: MyViewCLickedListener) :

    RecyclerView.Adapter<ImageViewHolder?>() {
    var context: Context? = null
    var alStoreImages: ArrayList<MarkerModel?> = ArrayList()

    var listner: MyViewCLickedListener? = null

    init {
        this.listner = myViewClickListener
        this.context = mcontext
    }

    override fun onBindViewHolder(@NonNull holder: ImageViewHolder, position: Int) {
        holder.txtVideoName.text = alStoreImages!![position]?.videoname
    }


    inner class ImageViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var ourPlaces: AppCompatImageView
        var txtVideoName: AppCompatTextView
        var cardMain: CardView

        init {
            ourPlaces = itemView.findViewById(R.id.imgPin) as AppCompatImageView
            txtVideoName = itemView.findViewById(R.id.txtVideoName) as AppCompatTextView
            cardMain = itemView.findViewById(R.id.cardMain) as CardView
            cardMain.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listner?.onClick(v, adapterPosition)
        }
    }


    override fun getItemCount(): Int {
        return alStoreImages!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.row_pins, parent, false)
        return ImageViewHolder(view)
    }

    fun addAll(datalist: ArrayList<MarkerModel?>?) {
        alStoreImages.clear()
        alStoreImages.addAll(datalist!!)
        notifyDataSetChanged()
    }
}