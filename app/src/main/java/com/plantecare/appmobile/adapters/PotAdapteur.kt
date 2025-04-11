// app/src/main/java/com/plantecare/appmobile/adapters/PotAdapter.kt
package com.plantecare.appmobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.plantecare.appmobile.R
import com.plantecare.appmobile.models.PotResponse

class PotAdapter(
    context: Context,
    private val potList: List<PotResponse>,
    private val onItemClick: (PotResponse) -> Unit
) : ArrayAdapter<PotResponse>(context, 0, potList) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: inflater.inflate(R.layout.item_plante, parent, false)

        val currentPot = getItem(position) ?: return itemView

        // Récupération des vues exactement comme définies dans votre XML
        val potImageView = itemView.findViewById<ImageView>(R.id.imageViewPlant)
        val potNameTextView = itemView.findViewById<TextView>(R.id.textViewPlantName)
        val macAddressTextView = itemView.findViewById<TextView>(R.id.textViewMacAddress)

        // Définition des valeurs
        potNameTextView.text = currentPot.name
        macAddressTextView.text = currentPot.macAddress

        // Image par défaut pour tous les pots
        potImageView.setImageResource(R.drawable.icon_plante)

        // Gestion du clic sur l'élément
        itemView.setOnClickListener {
            onItemClick(currentPot)
        }

        return itemView
    }

    override fun getCount(): Int {
        return potList.size
    }

    override fun getItem(position: Int): PotResponse? {
        return if (position in potList.indices) potList[position] else null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}