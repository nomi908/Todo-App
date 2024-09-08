package Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.databinding.ItemNotesBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesAdapter(
    private val fileList: MutableList<File>,
    private val onSelectionChanged: (Int) -> Unit,
    private val onItemClicked: (File) -> Unit // Callback for item click
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<File>()
    var isSelectionMode = false  // Public property for selection mode

    inner class ViewHolder(private val binding: ItemNotesBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val file = fileList[adapterPosition]

                // Handle selection mode
                if (isSelectionMode) {
                    if (selectedItems.contains(file)) {
                        selectedItems.remove(file)
                    } else {
                        selectedItems.add(file)
                    }
                    onSelectionChanged(selectedItems.size)
                    notifyItemChanged(adapterPosition) // Update only this item
                    // Deactivate selection mode if no items are selected
                    if (selectedItems.isEmpty()) {
                        clearSelection()
                    }
                } else {
                    // Handle normal item click
                    onItemClicked(file)
                }
            }

            binding.root.setOnLongClickListener {
                val file = fileList[adapterPosition]

                if (!isSelectionMode) {
                    // Enter selection mode
                    isSelectionMode = true
                    selectedItems.add(file)
                    onSelectionChanged(selectedItems.size)
                    notifyItemChanged(adapterPosition) // Update only this item
                    true // Return true to indicate the long click event was handled
                } else {
                    // If already in selection mode, toggle selection
                    if (selectedItems.contains(file)) {
                        selectedItems.remove(file)
                    } else {
                        selectedItems.add(file)
                    }
                    onSelectionChanged(selectedItems.size)
                    notifyItemChanged(adapterPosition) // Update only this item
                    true // Return true to indicate the long click event was handled
                }
            }
        }

        fun bind(file: File) {
            try {
                binding.rcvtitle.text = file.name

                // Check if the file exists before attempting to read it
                val fileContent = if (file.exists()) {
                    file.readText().take(20) + "..."
                } else {
                    "File not found"
                }
                binding.rcvdsp.text = fileContent

                binding.dateformat.text = "Last Modified: ${file.lastModified()}"
                val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(file.lastModified()))
                binding.dateformat.text = "Last Modified: $formattedDate"


                // Show or hide checkmark based on selection
                binding.checkbox.visibility = if (selectedItems.contains(file)) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Log.e("NotesAdapter", "Error binding file: ${file.name}", e)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = fileList[position]
        holder.bind(file)
    }

    fun getSelectedItems(): List<File> = selectedItems.toList()

    fun clearSelection() {
        if (isSelectionMode) {
            selectedItems.clear()
            isSelectionMode = false
            notifyDataSetChanged()  // Refresh the entire list
        }
    }
}
