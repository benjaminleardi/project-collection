package edu.illinois.cs.cs124.ay2025.mp.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.illinois.cs.cs124.ay2025.mp.R
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SummaryListAdapter(
    private var summaries: List<Summary>,
    private val activity: Activity,
    private val onClickCallback: ((Summary) -> Any)? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setSummaries(setSummaries: List<Summary>) {
        summaries = setSummaries
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_summary, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val summary = summaries[position]

        val title: TextView = holder.itemView.findViewById(R.id.title)
        val dateTimeLocation: TextView = holder.itemView.findViewById(R.id.dateTimeLocation)
        val layout: LinearLayout = holder.itemView.findViewById(R.id.layout)

        title.text = summary.title
        title.setTextColor(activity.getColor(R.color.darkTextColor))

        val dateTimeLocationText = formatDateTimeLocation(summary)
        dateTimeLocation.text = dateTimeLocationText

        layout.setBackgroundColor(activity.getColor(android.R.color.white))

        if (onClickCallback != null) {
            layout.setOnClickListener { onClickCallback(summary) }
        }
    }

    private fun formatDateTimeLocation(summary: Summary): String {
        return try {
            val start = ZonedDateTime.parse(summary.start)

            val date = start.format(DateTimeFormatter.ofPattern("MMM d"))

            val time = start.format(DateTimeFormatter.ofPattern("h:mm a"))

            var location = summary.location
            if (location.isEmpty()) {
                return "$date • $time"
            }
            if (location.length > MAX_LOCATION_LENGTH) {
                location = location.substring(0, TRUNCATED_LOCATION_LENGTH) + "..."
            }

            "$date • $time • $location"
        } catch (_: Exception) {
            val location = summary.location
            location.ifEmpty { "" }
        }
    }

    override fun getItemCount(): Int = summaries.size
}

private const val MAX_LOCATION_LENGTH = 30

private const val TRUNCATED_LOCATION_LENGTH = 27
